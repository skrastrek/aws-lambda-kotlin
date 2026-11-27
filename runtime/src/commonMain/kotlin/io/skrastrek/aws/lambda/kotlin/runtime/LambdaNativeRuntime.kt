package io.skrastrek.aws.lambda.kotlin.runtime

import io.skrastrek.aws.lambda.kotlin.core.Context
import io.skrastrek.aws.lambda.kotlin.core.LambdaLogger
import io.skrastrek.aws.lambda.kotlin.core.RequestStreamHandler
import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestStreamHandler
import io.skrastrek.aws.lambda.kotlin.coroutines.asSuspending
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.readByteArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock

private fun runtimeApiBaseUrl() = "http://${getenv("AWS_LAMBDA_RUNTIME_API")}/2018-06-01/runtime"

/**
 * A custom runtime that polls the Lambda Runtime API and passes each event to [handler].
 *
 * Blocking handlers are accepted through the secondary constructors and run on IO. Suspending
 * handlers are used as-is, so an invocation that awaits IO releases its thread.
 */
sealed class LambdaNativeRuntime(
    protected val handler: SuspendingRequestStreamHandler,
    runtimeApiBaseUrl: String,
) {
    internal val client = RuntimeApiClient(runtimeApiBaseUrl)

    fun start(): Unit =
        runBlocking {
            while (true) {
                val invocation = client.next()
                val context = InvocationContext(invocation)
                try {
                    respond(invocation, context)
                } catch (e: Exception) {
                    context.logger.log("Unhandled exception in Lambda handler: ${e.stackTraceToString()}")
                    client.error(
                        invocation.requestId,
                        buildJsonObject {
                            put("errorMessage", e.message ?: "Unknown error")
                            put("errorType", e::class.qualifiedName ?: "Unknown")
                        }.toString(),
                    )
                }
            }
        }

    internal abstract suspend fun respond(
        invocation: Invocation,
        context: Context,
    )

    class BufferedResponse(
        handler: SuspendingRequestStreamHandler,
        runtimeApiBaseUrl: String = runtimeApiBaseUrl(),
    ) : LambdaNativeRuntime(handler, runtimeApiBaseUrl) {
        constructor(
            handler: RequestStreamHandler,
            runtimeApiBaseUrl: String = runtimeApiBaseUrl(),
        ) : this(handler.asSuspending(), runtimeApiBaseUrl)

        override suspend fun respond(
            invocation: Invocation,
            context: Context,
        ) {
            val output = Buffer()
            handler.handle(invocation.event(), output, context)
            client.respond(invocation.requestId, output.readByteArray())
        }
    }

    class StreamResponse(
        handler: SuspendingRequestStreamHandler,
        runtimeApiBaseUrl: String = runtimeApiBaseUrl(),
    ) : LambdaNativeRuntime(handler, runtimeApiBaseUrl) {
        constructor(
            handler: RequestStreamHandler,
            runtimeApiBaseUrl: String = runtimeApiBaseUrl(),
        ) : this(handler.asSuspending(), runtimeApiBaseUrl)

        override suspend fun respond(
            invocation: Invocation,
            context: Context,
        ) = client.respondStreaming(invocation.requestId) { handler.handle(invocation.event(), it, context) }
    }
}

internal class Invocation(
    val requestId: String,
    val deadlineMs: Long?,
    val invokedFunctionArn: String?,
    val body: ByteArray,
) {
    fun event() = Buffer().apply { write(body) }
}

/** The platform's HTTP client for the Lambda Runtime API. */
internal expect class RuntimeApiClient(
    baseUrl: String,
) {
    /** Long-polls for the next event. Throws if the response carries no request id. */
    suspend fun next(): Invocation

    suspend fun respond(
        requestId: String,
        body: ByteArray,
    )

    /** Posts the response with chunked encoding while [write] produces it. Rethrows [write]'s failure. */
    suspend fun respondStreaming(
        requestId: String,
        write: suspend (Sink) -> Unit,
    )

    suspend fun error(
        requestId: String,
        json: String,
    )
}

internal expect fun getenv(name: String): String?

private class InvocationContext(
    private val invocation: Invocation,
) : Context {
    override val awsRequestId get() = invocation.requestId
    override val logGroupName get() = getenv("AWS_LAMBDA_LOG_GROUP_NAME").orEmpty()
    override val logStreamName get() = getenv("AWS_LAMBDA_LOG_STREAM_NAME").orEmpty()
    override val functionName get() = getenv("AWS_LAMBDA_FUNCTION_NAME").orEmpty()
    override val functionVersion get() = getenv("AWS_LAMBDA_FUNCTION_VERSION") ?: $$"$LATEST"
    override val invokedFunctionArn get() = invocation.invokedFunctionArn.orEmpty()
    override val memoryLimitInMB get() = getenv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE")?.toInt() ?: 128
    override val remainingTimeInMillis
        get() = ((invocation.deadlineMs ?: 0L) - Clock.System.now().toEpochMilliseconds()).coerceAtLeast(0).toInt()
    override val logger = LambdaLogger(::println)
}
