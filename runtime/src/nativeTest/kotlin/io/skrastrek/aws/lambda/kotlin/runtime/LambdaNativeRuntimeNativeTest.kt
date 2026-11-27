package io.skrastrek.aws.lambda.kotlin.runtime

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.skrastrek.aws.lambda.kotlin.core.RequestStreamHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Exercises the Ktor-based Runtime API client against a local fake of the Runtime API. */
@OptIn(ExperimentalAtomicApi::class)
class LambdaNativeRuntimeNativeTest {
    private class Captured(
        val path: String,
        val headers: Map<String, String?>,
        val body: String,
    )

    /** Serves one event, captures the first POST, then stops the runtime by omitting the request id. */
    private fun invoke(
        event: String,
        runtime: (baseUrl: String) -> LambdaNativeRuntime,
    ): Captured =
        runBlocking {
            val captured = CompletableDeferred<Captured>()
            val served = AtomicBoolean(false)
            val capture: suspend ApplicationCall.(String) -> Unit = { path ->
                val headers =
                    listOf("Content-Type", "Lambda-Runtime-Function-Response-Mode", "Lambda-Runtime-Function-Error-Type")
                        .associateWith { request.headers[it] }
                captured.complete(Captured(path, headers, receiveText()))
                respond(HttpStatusCode.Accepted)
            }
            val server =
                embeddedServer(CIO, port = 0) {
                    routing {
                        get("/2018-06-01/runtime/invocation/next") {
                            if (served.compareAndSet(expectedValue = false, newValue = true)) {
                                call.response.header("Lambda-Runtime-Aws-Request-Id", "req-1")
                                call.respondText(event)
                            } else {
                                call.respond(HttpStatusCode.InternalServerError)
                            }
                        }
                        post("/2018-06-01/runtime/invocation/req-1/response") { call.capture("response") }
                        post("/2018-06-01/runtime/invocation/req-1/error") { call.capture("error") }
                    }
                }.startSuspend()
            val port = server.engine.resolvedConnectors().first().port

            val job = launch(Dispatchers.IO) { runCatching { runtime("http://127.0.0.1:$port/2018-06-01/runtime").start() } }
            captured.await().also {
                job.join()
                server.stopSuspend()
            }
        }

    private val echo = RequestStreamHandler { input, output, _ -> input.transferTo(output) }

    @Test
    fun `BufferedResponse posts the handler output`() {
        val captured = invoke("""{"action":"test"}""") { LambdaNativeRuntime.BufferedResponse(echo, it) }

        assertEquals("response", captured.path)
        assertEquals("""{"action":"test"}""", captured.body)
        assertEquals("application/json", captured.headers["Content-Type"])
    }

    @Test
    fun `StreamResponse streams the handler output`() {
        val captured = invoke("""{"action":"stream"}""") { LambdaNativeRuntime.StreamResponse(echo, it) }

        assertEquals("response", captured.path)
        assertEquals("""{"action":"stream"}""", captured.body)
        assertEquals("streaming", captured.headers["Lambda-Runtime-Function-Response-Mode"])
        assertEquals("application/vnd.awslambda.http-integration-response", captured.headers["Content-Type"])
    }

    @Test
    fun `handler exception is posted to the error endpoint`() {
        val captured =
            invoke("{}") {
                LambdaNativeRuntime.BufferedResponse(RequestStreamHandler { _, _, _ -> throw IllegalStateException("boom") }, it)
            }

        assertEquals("error", captured.path)
        assertTrue(""""errorMessage":"boom"""" in captured.body, captured.body)
        assertTrue(""""errorType":"kotlin.IllegalStateException"""" in captured.body, captured.body)
        assertEquals("Unhandled", captured.headers["Lambda-Runtime-Function-Error-Type"])
    }
}
