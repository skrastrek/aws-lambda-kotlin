package io.skrastrek.aws.lambda.kotlin.coroutines

import com.amazonaws.services.lambda.runtime.Context
import io.skrastrek.aws.lambda.kotlin.core.EmptyContext
import io.skrastrek.aws.lambda.kotlin.core.SerializerInference
import io.skrastrek.aws.lambda.kotlin.core.json
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.core.RequestHandler].
 *
 * This is a sibling of the blocking hierarchy rather than a subtype of it: inheriting the blocking
 * `handle(I, Context)` would conflict with the suspending one declared here.
 *
 * Decoding and encoding are confined to [IO] so the handler stays correct whichever dispatcher the
 * caller uses, while [handle] itself runs on the caller's context.
 */
interface SuspendingRequestHandler<I : Any, O : Any> : SuspendingRequestStreamHandler {
    /**
     * Reads [I] off the wire. Defaults to the serializer for whatever [I] is bound to by the
     * implementing class, so `SuspendingRequestHandler<Foo, Bar>` needs no declaration here.
     * Override to use a different strategy, or when the binding is not concrete — see
     * [SerializerInference].
     */
    @Suppress("UNCHECKED_CAST")
    val deserializer: DeserializationStrategy<I>
        get() =
            SerializerInference
                .serializersOf(this, SuspendingRequestHandler::class.java)[0] as DeserializationStrategy<I>

    /** Writes [O] to the wire. Inferred like [deserializer]. */
    @Suppress("UNCHECKED_CAST")
    val serializer: SerializationStrategy<O>
        get() =
            SerializerInference
                .serializersOf(this, SuspendingRequestHandler::class.java)[1] as SerializationStrategy<O>

    suspend fun handle(
        input: I,
        context: Context,
    ): O

    override suspend fun handle(
        input: InputStream,
        output: OutputStream,
        context: Context,
    ) {
        val decoded = withContext(IO) { input.jsonDecode() }
        val result = handle(decoded, context)
        withContext(IO) { result.jsonEncodeTo(output) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun InputStream.jsonDecode(): I = json.decodeFromStream(deserializer, this)

    @OptIn(ExperimentalSerializationApi::class)
    private fun O.jsonEncodeTo(output: OutputStream) = json.encodeToStream(serializer, this, output)
}

/**
 * A [SuspendingRequestHandler] built from [handle].
 *
 * The serializers come from the reified type arguments, which the serialization plugin resolves at
 * the call site — no reflection, unlike the inferred defaults on the interface. Use this for
 * handlers the custom runtime constructs itself; the AWS managed runtime resolves handlers by class
 * name and needs a named class implementing [SuspendingRequestHandler] instead.
 */
inline fun <reified I : Any, reified O : Any> SuspendingRequestHandler(
    crossinline handle: suspend (input: I, context: Context) -> O,
): SuspendingRequestHandler<I, O> =
    object : SuspendingRequestHandler<I, O> {
        override val deserializer = serializer<I>()
        override val serializer = serializer<O>()

        override suspend fun handle(
            input: I,
            context: Context,
        ): O = handle(input, context)
    }

suspend fun <I : Any, O : Any> SuspendingRequestHandler<I, O>.handle(input: I): O = handle(input, EmptyContext)

suspend fun <I : Any, O : Any> SuspendingRequestHandler<I, O>.handle(
    input: InputStream,
    output: OutputStream,
) = handle(input, output, EmptyContext)
