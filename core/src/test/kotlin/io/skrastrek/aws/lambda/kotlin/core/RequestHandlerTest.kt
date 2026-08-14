package io.skrastrek.aws.lambda.kotlin.core

import com.amazonaws.services.lambda.runtime.Context
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RequestHandlerTest {
    @Test
    fun handle_request() {
        assertEquals(json.encodeToString("HELLO WORLD"), CapitalizeRequestHandler.handleJson("hello world"))
    }

    @Test
    fun handle_request_with_inferred_serializers() {
        assertEquals(json.encodeToString("HELLO WORLD"), InferredCapitalizeRequestHandler.handleJson("hello world"))
    }

    @Test
    fun handle_request_with_inferred_serializers_through_a_base_class() {
        assertEquals(json.encodeToString(11), CountCharactersRequestHandler.handleJson("hello world"))
    }

    @Test
    fun handle_request_with_inferred_serializers_for_parameterized_types() {
        // Kotlin's declaration-site variance emits `List<? extends String>` into the signature.
        assertEquals(
            json.encodeToString(mapOf("hello" to 5, "world" to 5)),
            MeasureEachRequestHandler.handleJson(listOf("hello", "world")),
        )
    }

    @Test
    fun handle_request_from_factory() {
        val handler = RequestHandler<String, Int> { input, _ -> input.length }

        assertEquals(json.encodeToString(11), handler.handleJson("hello world"))
    }

    @Test
    fun inferring_serializers_fails_for_a_generic_handler() {
        val failure = assertFailsWith<IllegalStateException> { EchoRequestHandler<String>().handleJson("hello world") }

        assertContains(failure.message.orEmpty(), "leaves RequestHandler's type argument 'T' generic")
    }
}

private inline fun <reified I : Any, O : Any> RequestHandler<I, O>.handleJson(input: I): String {
    val output = ByteArrayOutputStream()
    handle(ByteArrayInputStream(json.encodeToString(input).toByteArray()), output)
    return output.toString(Charsets.UTF_8)
}

private object CapitalizeRequestHandler : RequestHandler<String, String> {
    override val deserializer get() = String.serializer()
    override val serializer get() = String.serializer()

    override fun handle(
        input: String,
        context: Context,
    ): String = input.uppercase()
}

private object InferredCapitalizeRequestHandler : RequestHandler<String, String> {
    override fun handle(
        input: String,
        context: Context,
    ): String = input.uppercase()
}

private object MeasureEachRequestHandler : RequestHandler<List<String>, Map<String, Int>> {
    override fun handle(
        input: List<String>,
        context: Context,
    ): Map<String, Int> = input.associateWith { it.length }
}

private abstract class MeasuringRequestHandler<I : Any> : RequestHandler<I, Int>

private object CountCharactersRequestHandler : MeasuringRequestHandler<String>() {
    override fun handle(
        input: String,
        context: Context,
    ): Int = input.length
}

private class EchoRequestHandler<T : Any> : RequestHandler<T, T> {
    override fun handle(
        input: T,
        context: Context,
    ): T = input
}
