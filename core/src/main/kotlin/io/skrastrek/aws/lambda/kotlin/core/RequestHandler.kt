package io.skrastrek.aws.lambda.kotlin.core

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
val json =
    Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

interface RequestHandler<I : Any, O : Any> : RequestStreamHandler {
    /**
     * Reads [I] off the wire. Defaults to the serializer for whatever [I] is bound to by the
     * implementing class, so `RequestHandler<Foo, Bar>` needs no declaration here. Override to use a
     * different strategy, or when the binding is not concrete — see [SerializerInference].
     */
    @Suppress("UNCHECKED_CAST")
    val deserializer: DeserializationStrategy<I>
        get() = SerializerInference.serializersOf(this, RequestHandler::class.java)[0] as DeserializationStrategy<I>

    /** Writes [O] to the wire. Inferred like [deserializer]. */
    @Suppress("UNCHECKED_CAST")
    val serializer: SerializationStrategy<O>
        get() = SerializerInference.serializersOf(this, RequestHandler::class.java)[1] as SerializationStrategy<O>

    fun handle(
        input: I,
        context: Context,
    ): O

    /** AWS's entry point. Named by the platform contract, not by us — implement [handle] instead. */
    override fun handleRequest(
        input: InputStream,
        output: OutputStream,
        context: Context,
    ) = handle(input.jsonDecode(), context).jsonEncodeTo(output)

    @OptIn(ExperimentalSerializationApi::class)
    private fun InputStream.jsonDecode(): I = json.decodeFromStream(deserializer, this)

    @OptIn(ExperimentalSerializationApi::class)
    private fun O.jsonEncodeTo(output: OutputStream) = json.encodeToStream(serializer, this, output)
}

/**
 * A [RequestHandler] built from [handle].
 *
 * The serializers come from the reified type arguments, which the serialization plugin resolves at
 * the call site — no reflection, unlike the inferred defaults on the interface. Use this for
 * handlers the custom runtime constructs itself; the AWS managed runtime resolves handlers by class
 * name and needs a named class implementing [RequestHandler] instead.
 */
inline fun <reified I : Any, reified O : Any> RequestHandler(crossinline handle: (input: I, context: Context) -> O): RequestHandler<I, O> =
    object : RequestHandler<I, O> {
        override val deserializer = serializer<I>()
        override val serializer = serializer<O>()

        override fun handle(
            input: I,
            context: Context,
        ): O = handle(input, context)
    }

fun <I : Any, O : Any> RequestHandler<I, O>.handle(input: I): O = handle(input, EmptyContext)

fun <I : Any, O : Any> RequestHandler<I, O>.handle(
    input: InputStream,
    output: OutputStream,
) = handleRequest(input, output, EmptyContext)
