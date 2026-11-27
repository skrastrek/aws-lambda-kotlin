package io.skrastrek.aws.lambda.kotlin.runtime

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.asSink
import kotlinx.io.buffered
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import kotlin.jvm.optionals.getOrNull

internal actual class RuntimeApiClient actual constructor(
    private val baseUrl: String,
) {
    private val http: HttpClient = HttpClient.newHttpClient()

    actual suspend fun next(): Invocation =
        withContext(IO) {
            val response =
                http.send(
                    HttpRequest.newBuilder(URI.create("$baseUrl/invocation/next")).GET().build(),
                    BodyHandlers.ofByteArray(),
                )
            val headers = response.headers()
            Invocation(
                requestId = headers.firstValue("Lambda-Runtime-Aws-Request-Id").get(),
                deadlineMs = headers.firstValue("Lambda-Runtime-Deadline-Ms").getOrNull()?.toLong(),
                invokedFunctionArn = headers.firstValue("Lambda-Runtime-Invoked-Function-Arn").getOrNull(),
                body = response.body(),
            )
        }

    actual suspend fun respond(
        requestId: String,
        body: ByteArray,
    ) = post("invocation/$requestId/response", BodyPublishers.ofByteArray(body), "Content-Type" to "application/json")

    actual suspend fun respondStreaming(
        requestId: String,
        write: suspend (Sink) -> Unit,
    ) = coroutineScope {
        val pipedOutput = PipedOutputStream()
        val pipedInput = PipedInputStream(pipedOutput, 65536)
        // Stays on IO: PipedOutputStream.write blocks once the buffer fills, regardless of
        // whether the handler itself suspends.
        launch(IO) { pipedOutput.asSink().buffered().use { write(it) } }
        post(
            "invocation/$requestId/response",
            BodyPublishers.ofInputStream { pipedInput },
            "Lambda-Runtime-Function-Response-Mode" to "streaming",
            "Content-Type" to "application/vnd.awslambda.http-integration-response",
        )
    }

    actual suspend fun error(
        requestId: String,
        json: String,
    ) = post(
        "invocation/$requestId/error",
        BodyPublishers.ofString(json),
        "Content-Type" to "application/json",
        "Lambda-Runtime-Function-Error-Type" to "Unhandled",
    )

    private suspend fun post(
        path: String,
        body: BodyPublisher,
        vararg headers: Pair<String, String>,
    ) {
        withContext(IO) {
            val request = HttpRequest.newBuilder(URI.create("$baseUrl/$path")).POST(body)
            headers.forEach { (name, value) -> request.header(name, value) }
            http.send(request.build(), BodyHandlers.discarding())
        }
    }
}

internal actual fun getenv(name: String): String? = System.getenv(name)
