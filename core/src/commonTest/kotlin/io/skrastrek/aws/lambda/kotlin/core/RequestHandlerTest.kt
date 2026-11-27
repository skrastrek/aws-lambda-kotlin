package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class RequestHandlerTest {
    @Test
    fun handle_request() {
        val input = Buffer().apply { writeString(defaultJson.encodeToString("hello world")) }
        val output = Buffer()

        CapitalizeRequestHandler.handle(input, output)

        assertEquals(defaultJson.encodeToString("HELLO WORLD"), output.readString())
    }
}

private object CapitalizeRequestHandler : RequestHandler<String, String> {
    override val deserializer get() = String.serializer()
    override val serializer get() = String.serializer()

    override fun handle(
        input: String,
        context: Context,
    ): String = input.uppercase()
}
