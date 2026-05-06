package io.skrastrek.aws.lambda.kotlin.runtime

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import java.net.InetSocketAddress
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LambdaNativeRuntimeTest {
    private lateinit var server: HttpServer
    private lateinit var runtimeScope: CoroutineScope
    private var port: Int = 0

    @BeforeTest
    fun setUp() {
        runtimeScope = CoroutineScope(Dispatchers.IO)
        server = HttpServer.create(InetSocketAddress(0), 0)
        server.start()
        port = server.address.port
    }

    @AfterTest
    fun tearDown() {
        server.stop(0)
        runtimeScope.cancel()
    }

    private fun base() = "http://localhost:$port/2018-06-01/runtime"

    private fun registerNextInvocation(
        requestId: String,
        eventBody: String,
    ) {
        val gate = Channel<Unit>(capacity = 1).apply { trySend(Unit) }
        server.createContext("/2018-06-01/runtime/invocation/next") { exchange ->
            if (gate.tryReceive().isSuccess) {
                exchange.responseHeaders.add("Lambda-Runtime-Aws-Request-Id", requestId)
                val bytes = eventBody.toByteArray()
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } else {
                // Missing request-id header causes NoSuchElementException in start(), stopping the loop
                exchange.sendResponseHeaders(500, -1)
                exchange.close()
            }
        }
    }

    private fun startRuntime(runtime: LambdaNativeRuntime) {
        runtimeScope.launch {
            try {
                runtime.start()
            } catch (_: Exception) {
            }
        }
    }

    @Test
    fun `BufferedResponse passes event body to handler and posts output to response endpoint`() =
        runTest {
            val requestId = "req-buffered-01"
            val eventBody = """{"action":"test"}"""
            val capturedBody = CompletableDeferred<String>()
            val capturedContentType = CompletableDeferred<String>()

            registerNextInvocation(requestId, eventBody)
            server.createContext("/2018-06-01/runtime/invocation/$requestId/response") { exchange ->
                capturedBody.complete(exchange.requestBody.readBytes().decodeToString())
                capturedContentType.complete(exchange.requestHeaders.getFirst("Content-Type"))
                exchange.sendResponseHeaders(202, -1)
                exchange.close()
            }

            startRuntime(
                LambdaNativeRuntime.BufferedResponse(
                    { input, output, _ -> output.write(input.readBytes()) },
                    base(),
                ),
            )

            assertEquals(eventBody, capturedBody.await())
            assertEquals("application/json", capturedContentType.await())
        }

    @Test
    fun `BufferedResponse posts to error endpoint on handler exception`() =
        runTest {
            val requestId = "req-buffered-02"
            val capturedErrorBody = CompletableDeferred<String>()
            val capturedErrorType = CompletableDeferred<String>()

            registerNextInvocation(requestId, "{}")
            server.createContext("/2018-06-01/runtime/invocation/$requestId/error") { exchange ->
                capturedErrorBody.complete(exchange.requestBody.readBytes().decodeToString())
                capturedErrorType.complete(exchange.requestHeaders.getFirst("Lambda-Runtime-Function-Error-Type"))
                exchange.sendResponseHeaders(202, -1)
                exchange.close()
            }

            startRuntime(
                LambdaNativeRuntime.BufferedResponse(
                    { _, _, _ -> throw RuntimeException("handler error") },
                    base(),
                ),
            )

            val body = capturedErrorBody.await()
            assertTrue(""""errorMessage":"handler error"""" in body)
            assertTrue(""""errorType":"java.lang.RuntimeException"""" in body)
            assertEquals("Unhandled", capturedErrorType.await())
        }

    @Test
    fun `StreamResponse passes event body to handler and posts output with streaming headers`() =
        runTest {
            val requestId = "req-stream-01"
            val eventBody = """{"action":"stream"}"""
            val capturedBody = CompletableDeferred<String>()
            val capturedResponseMode = CompletableDeferred<String>()
            val capturedContentType = CompletableDeferred<String>()

            registerNextInvocation(requestId, eventBody)
            server.createContext("/2018-06-01/runtime/invocation/$requestId/response") { exchange ->
                capturedBody.complete(exchange.requestBody.readBytes().decodeToString())
                capturedResponseMode.complete(exchange.requestHeaders.getFirst("Lambda-Runtime-Function-Response-Mode"))
                capturedContentType.complete(exchange.requestHeaders.getFirst("Content-Type"))
                exchange.sendResponseHeaders(202, -1)
                exchange.close()
            }

            startRuntime(
                LambdaNativeRuntime.StreamResponse(
                    { input, output, _ -> output.write(input.readBytes()) },
                    base(),
                ),
            )

            assertEquals(eventBody, capturedBody.await())
            assertEquals("streaming", capturedResponseMode.await())
            assertEquals("application/vnd.awslambda.http-integration-response", capturedContentType.await())
        }

    @Test
    fun `StreamResponse posts to error endpoint on handler exception`() =
        runTest {
            val requestId = "req-stream-02"
            val capturedErrorBody = CompletableDeferred<String>()
            val capturedErrorType = CompletableDeferred<String>()

            registerNextInvocation(requestId, "{}")
            server.createContext("/2018-06-01/runtime/invocation/$requestId/response") { exchange ->
                exchange.requestBody.skip(Long.MAX_VALUE)
                exchange.sendResponseHeaders(202, -1)
                exchange.close()
            }
            server.createContext("/2018-06-01/runtime/invocation/$requestId/error") { exchange ->
                capturedErrorBody.complete(exchange.requestBody.readBytes().decodeToString())
                capturedErrorType.complete(exchange.requestHeaders.getFirst("Lambda-Runtime-Function-Error-Type"))
                exchange.sendResponseHeaders(202, -1)
                exchange.close()
            }

            startRuntime(
                LambdaNativeRuntime.StreamResponse(
                    { _, _, _ -> throw RuntimeException("stream error") },
                    base(),
                ),
            )

            val body = capturedErrorBody.await()
            assertTrue(""""errorMessage":"stream error"""" in body)
            assertTrue(""""errorType":"java.lang.RuntimeException"""" in body)
            assertEquals("Unhandled", capturedErrorType.await())
        }
}
