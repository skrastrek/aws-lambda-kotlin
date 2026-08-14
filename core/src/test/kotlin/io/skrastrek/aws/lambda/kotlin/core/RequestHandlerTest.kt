package io.skrastrek.aws.lambda.kotlin.core

import com.amazonaws.services.lambda.runtime.Context
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

class RequestHandlerTest {
    @Test
    fun handle_request() {
        val input = ByteArrayInputStream(defaultJson.encodeToString("hello world").toByteArray())
        val output = ByteArrayOutputStream()

        CapitalizeRequestHandler.handle(input, output)

        assertEquals(defaultJson.encodeToString("HELLO WORLD"), output.toString(Charsets.UTF_8))
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
