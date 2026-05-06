package io.skrastrek.aws.lambda.kotlin.runtime

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import kotlin.jvm.optionals.getOrNull

private fun runtimeApiBaseUrl() = "http://${System.getenv("AWS_LAMBDA_RUNTIME_API")}/2018-06-01/runtime"

abstract class LambdaNativeRuntime(
    protected val handler: RequestStreamHandler,
    protected val runtimeApiBaseUrl: String,
) {
    protected val http: HttpClient = HttpClient.newHttpClient()

    fun start() =
        runBlocking {
            while (true) {
                val next =
                    withContext(IO) {
                        http.send(
                            HttpRequest.newBuilder().uri(URI.create("$runtimeApiBaseUrl/invocation/next")).GET().build(),
                            BodyHandlers.ofInputStream(),
                        )
                    }
                val requestId = next.headers().firstValue("Lambda-Runtime-Aws-Request-Id").get()
                val context = InvocationContext(requestId, next.headers())

                try {
                    sendResponse(requestId, next.body(), context)
                } catch (e: Exception) {
                    context.logger.log("Unhandled exception in Lambda handler: ${e.stackTraceToString()}")
                    sendError(requestId, e)
                }
            }
        }

    protected abstract suspend fun sendResponse(
        requestId: String,
        eventStream: InputStream,
        context: Context,
    )

    private suspend fun sendError(
        requestId: String,
        e: Exception,
    ) = withContext(IO) {
        val msg = (e.message ?: "Unknown error").replace("\\", "\\\\").replace("\"", "\\\"")
        http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$runtimeApiBaseUrl/invocation/$requestId/error"))
                .POST(BodyPublishers.ofString("""{"errorMessage":"$msg","errorType":"${e.javaClass.name}"}"""))
                .header("Content-Type", "application/json")
                .header("Lambda-Runtime-Function-Error-Type", "Unhandled")
                .build(),
            BodyHandlers.discarding(),
        )
    }

    class BufferedResponse(
        handler: RequestStreamHandler,
        runtimeApiBaseUrl: String = runtimeApiBaseUrl(),
    ) : LambdaNativeRuntime(handler, runtimeApiBaseUrl) {
        override suspend fun sendResponse(
            requestId: String,
            eventStream: InputStream,
            context: Context,
        ) {
            withContext(IO) {
                val output = ByteArrayOutputStream()
                handler.handleRequest(eventStream, output, context)
                http.send(
                    HttpRequest.newBuilder()
                        .uri(URI.create("${this@BufferedResponse.runtimeApiBaseUrl}/invocation/$requestId/response"))
                        .POST(BodyPublishers.ofByteArray(output.toByteArray()))
                        .header("Content-Type", "application/json")
                        .build(),
                    BodyHandlers.discarding(),
                )
            }
        }
    }

    class StreamResponse(
        handler: RequestStreamHandler,
        runtimeApiBaseUrl: String = runtimeApiBaseUrl(),
    ) : LambdaNativeRuntime(handler, runtimeApiBaseUrl) {
        override suspend fun sendResponse(
            requestId: String,
            eventStream: InputStream,
            context: Context,
        ) {
            coroutineScope {
                val pipedOutput = PipedOutputStream()
                val pipedInput = PipedInputStream(pipedOutput, 65536)
                launch(IO) {
                    pipedOutput.use { handler.handleRequest(eventStream, it, context) }
                }
                withContext(IO) {
                    http.send(
                        HttpRequest.newBuilder()
                            .uri(URI.create("${this@StreamResponse.runtimeApiBaseUrl}/invocation/$requestId/response"))
                            .POST(BodyPublishers.ofInputStream { pipedInput })
                            .header("Lambda-Runtime-Function-Response-Mode", "streaming")
                            .header("Content-Type", "application/vnd.awslambda.http-integration-response")
                            .build(),
                        BodyHandlers.discarding(),
                    )
                }
            }
        }
    }
}

private class InvocationContext(
    private val requestId: String,
    private val headers: HttpHeaders,
) : Context {
    override fun getAwsRequestId() = requestId

    override fun getLogGroupName() = System.getenv("AWS_LAMBDA_LOG_GROUP_NAME") ?: ""

    override fun getLogStreamName() = System.getenv("AWS_LAMBDA_LOG_STREAM_NAME") ?: ""

    override fun getFunctionName() = System.getenv("AWS_LAMBDA_FUNCTION_NAME") ?: ""

    override fun getFunctionVersion() = System.getenv("AWS_LAMBDA_FUNCTION_VERSION") ?: $$"$LATEST"

    override fun getInvokedFunctionArn() = headers.firstValue("Lambda-Runtime-Invoked-Function-Arn").getOrNull().orEmpty()

    override fun getMemoryLimitInMB() = System.getenv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE")?.toInt() ?: 128

    override fun getRemainingTimeInMillis(): Int {
        val deadlineMs = headers.firstValue("Lambda-Runtime-Deadline-Ms").getOrNull()?.toLong() ?: 0L
        return (deadlineMs - System.currentTimeMillis()).coerceAtLeast(0).toInt()
    }

    override fun getLogger() =
        object : LambdaLogger {
            override fun log(message: String) = println(message)

            override fun log(message: ByteArray) = println(message.decodeToString())
        }

    override fun getIdentity(): CognitoIdentity? = null

    override fun getClientContext(): ClientContext? = null
}
