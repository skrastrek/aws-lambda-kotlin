package io.skrastrek.aws.lambda.kotlin.coroutines

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import io.skrastrek.aws.lambda.kotlin.core.EmptyContext
import io.skrastrek.aws.lambda.kotlin.core.defaultJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class SuspendingRequestHandlerTest {
    @Test
    fun `suspending handle decodes, invokes and encodes`() =
        runTest {
            val input = ByteArrayInputStream(defaultJson.encodeToString("hello world").toByteArray())
            val output = ByteArrayOutputStream()

            CapitalizeRequestHandler.handle(input, output)

            assertEquals(defaultJson.encodeToString("HELLO WORLD"), output.toString(Charsets.UTF_8))
        }

    @Test
    fun `blocking bridge runs the suspending handler`() {
        val input = ByteArrayInputStream(defaultJson.encodeToString("hello world").toByteArray())
        val output = ByteArrayOutputStream()

        // The AWS-facing entry point: this is what the managed Java runtime invokes.
        CapitalizeRequestHandler.handleRequest(input, output, EmptyContext)

        assertEquals(defaultJson.encodeToString("HELLO WORLD"), output.toString(Charsets.UTF_8))
    }

    @Test
    fun `asSuspending returns a suspending handler unchanged`() {
        assertSame(CapitalizeRequestHandler, CapitalizeRequestHandler.asSuspending())
    }

    @Test
    fun `asSuspending wraps a blocking handler`() =
        runTest {
            val blocking = RequestStreamHandler { input, output, _ -> output.write(input.readBytes()) }
            val suspending = blocking.asSuspending()
            val output = ByteArrayOutputStream()

            assertNotSame<Any>(blocking, suspending)

            suspending.handle(ByteArrayInputStream("payload".toByteArray()), output, EmptyContext)

            assertEquals("payload", output.toString(Charsets.UTF_8))
        }

    @Test
    fun `an overridden json is honoured on the suspending path`() =
        runTest {
            val input = ByteArrayInputStream("""["a","b"]""".toByteArray())
            val output = ByteArrayOutputStream()

            PrettyPrintingHandler.handle(input, output)

            assertEquals("[\n    \"A\",\n    \"B\"\n]", output.toString(Charsets.UTF_8))
        }
}

private object PrettyPrintingHandler : SuspendingRequestHandler<List<String>, List<String>> {
    override val deserializer get() = ListSerializer(String.serializer())
    override val serializer get() = ListSerializer(String.serializer())

    override val json = Json(defaultJson) { prettyPrint = true }

    override suspend fun handle(
        input: List<String>,
        context: Context,
    ) = input.map { it.uppercase() }
}

private object CapitalizeRequestHandler : SuspendingRequestHandler<String, String> {
    override val deserializer get() = String.serializer()
    override val serializer get() = String.serializer()

    override suspend fun handle(
        input: String,
        context: Context,
    ): String {
        delay(1)
        return input.uppercase()
    }
}
