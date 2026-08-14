package io.skrastrek.aws.lambda.kotlin.coroutines

import com.amazonaws.services.lambda.runtime.Context
import io.skrastrek.aws.lambda.kotlin.core.EmptyContext
import io.skrastrek.aws.lambda.kotlin.core.HandlerSerializers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
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
interface SuspendingRequestHandler<I : Any, O : Any> :
    SuspendingRequestStreamHandler,
    HandlerSerializers<I, O> {
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

suspend fun <I : Any, O : Any> SuspendingRequestHandler<I, O>.handle(input: I): O = handle(input, EmptyContext)

suspend fun <I : Any, O : Any> SuspendingRequestHandler<I, O>.handle(
    input: InputStream,
    output: OutputStream,
) = handle(input, output, EmptyContext)
