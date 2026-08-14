package io.skrastrek.aws.lambda.kotlin.coroutines

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import io.skrastrek.aws.lambda.kotlin.core.EmptyContext
import io.skrastrek.aws.lambda.kotlin.core.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
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
            val input = ByteArrayInputStream(json.encodeToString("hello world").toByteArray())
            val output = ByteArrayOutputStream()

            CapitalizeRequestHandler.handle(input, output)

            assertEquals(json.encodeToString("HELLO WORLD"), output.toString(Charsets.UTF_8))
        }

    @Test
    fun `blocking bridge runs the suspending handler`() {
        val input = ByteArrayInputStream(json.encodeToString("hello world").toByteArray())
        val output = ByteArrayOutputStream()

        // The AWS-facing entry point: this is what the managed Java runtime invokes.
        CapitalizeRequestHandler.handleRequest(input, output, EmptyContext)

        assertEquals(json.encodeToString("HELLO WORLD"), output.toString(Charsets.UTF_8))
    }

    @Test
    fun `serializers are inferred from the type arguments`() =
        runTest {
            val input = ByteArrayInputStream(json.encodeToString("hello world").toByteArray())
            val output = ByteArrayOutputStream()

            InferredCapitalizeRequestHandler.handle(input, output)

            assertEquals(json.encodeToString("HELLO WORLD"), output.toString(Charsets.UTF_8))
        }

    @Test
    fun `factory builds a handler from a lambda`() =
        runTest {
            val handler = SuspendingRequestHandler<String, Int> { input, _ -> input.length }
            val input = ByteArrayInputStream(json.encodeToString("hello world").toByteArray())
            val output = ByteArrayOutputStream()

            handler.handle(input, output)

            assertEquals(json.encodeToString(11), output.toString(Charsets.UTF_8))
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
}

private object InferredCapitalizeRequestHandler : SuspendingRequestHandler<String, String> {
    override suspend fun handle(
        input: String,
        context: Context,
    ): String {
        delay(1)
        return input.uppercase()
    }
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
