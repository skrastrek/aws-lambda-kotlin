package io.skrastrek.aws.lambda.kotlin.coroutines

import io.skrastrek.aws.lambda.kotlin.core.Context
import io.skrastrek.aws.lambda.kotlin.core.EmptyContext
import io.skrastrek.aws.lambda.kotlin.core.RequestStreamHandler
import io.skrastrek.aws.lambda.kotlin.core.defaultJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspendingRequestHandlerTest {
    @Test
    fun `suspending handle decodes invokes and encodes`() =
        runTest {
            val input = Buffer().apply { writeString(defaultJson.encodeToString("hello world")) }
            val output = Buffer()

            CapitalizeRequestHandler.handle(input, output)

            assertEquals(defaultJson.encodeToString("HELLO WORLD"), output.readString())
        }

    @Test
    fun `asSuspending wraps a blocking handler`() =
        runTest {
            val blocking = RequestStreamHandler { input, output, _ -> input.transferTo(output) }
            val output = Buffer()

            blocking.asSuspending().handle(Buffer().apply { writeString("payload") }, output, EmptyContext)

            assertEquals("payload", output.readString())
        }

    @Test
    fun `an overridden json is honoured on the suspending path`() =
        runTest {
            val input = Buffer().apply { writeString("""["a","b"]""") }
            val output = Buffer()

            PrettyPrintingHandler.handle(input, output)

            assertEquals("[\n    \"A\",\n    \"B\"\n]", output.readString())
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

internal object CapitalizeRequestHandler : SuspendingRequestHandler<String, String> {
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
