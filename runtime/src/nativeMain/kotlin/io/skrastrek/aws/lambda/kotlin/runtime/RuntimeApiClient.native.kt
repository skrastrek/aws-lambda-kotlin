package io.skrastrek.aws.lambda.kotlin.runtime

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.readByteArray

internal actual class RuntimeApiClient actual constructor(
    private val baseUrl: String,
) {
    // requestTimeout = 0 disables the timeout: /invocation/next long-polls until an event arrives.
    private val http = HttpClient(CIO) { engine { requestTimeout = 0 } }

    actual suspend fun next(): Invocation {
        val response = http.get("$baseUrl/invocation/next")
        return Invocation(
            requestId = checkNotNull(response.headers["Lambda-Runtime-Aws-Request-Id"]) { "Missing Lambda-Runtime-Aws-Request-Id" },
            deadlineMs = response.headers["Lambda-Runtime-Deadline-Ms"]?.toLong(),
            invokedFunctionArn = response.headers["Lambda-Runtime-Invoked-Function-Arn"],
            body = response.bodyAsBytes(),
        )
    }

    actual suspend fun respond(
        requestId: String,
        body: ByteArray,
    ) {
        http.post("$baseUrl/invocation/$requestId/response") { setBody(ByteArrayContent(body, ContentType.Application.Json)) }
    }

    actual suspend fun respondStreaming(
        requestId: String,
        write: suspend (Sink) -> Unit,
    ) = coroutineScope {
        val chunks = Channel<ByteArray>(capacity = 16)
        // The handler writes through a blocking Sink, so it runs on IO and hands chunks over.
        launch(Dispatchers.IO) { ChannelSink(chunks).buffered().use { write(it) } }
        try {
            http.post("$baseUrl/invocation/$requestId/response") {
                header("Lambda-Runtime-Function-Response-Mode", "streaming")
                setBody(
                    object : OutgoingContent.WriteChannelContent() {
                        override val contentType = ContentType.parse("application/vnd.awslambda.http-integration-response")

                        override suspend fun writeTo(channel: ByteWriteChannel) {
                            for (chunk in chunks) {
                                channel.writeFully(chunk)
                                channel.flush()
                            }
                        }
                    },
                )
            }
        } finally {
            // Unblocks a handler still writing if the request ended early.
            chunks.cancel()
        }
        Unit
    }

    actual suspend fun error(
        requestId: String,
        json: String,
    ) {
        http.post("$baseUrl/invocation/$requestId/error") {
            header("Lambda-Runtime-Function-Error-Type", "Unhandled")
            setBody(TextContent(json, ContentType.Application.Json))
        }
    }
}

private class ChannelSink(
    private val chunks: SendChannel<ByteArray>,
) : RawSink {
    override fun write(
        source: Buffer,
        byteCount: Long,
    ) {
        chunks.trySendBlocking(source.readByteArray(byteCount.toInt())).getOrThrow()
    }

    override fun flush() = Unit

    override fun close() {
        chunks.close()
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun getenv(name: String): String? = platform.posix.getenv(name)?.toKString()
