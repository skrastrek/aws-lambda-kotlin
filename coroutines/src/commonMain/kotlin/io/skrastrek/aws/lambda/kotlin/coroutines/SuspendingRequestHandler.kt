@file:OptIn(ExperimentalSerializationApi::class)

package io.skrastrek.aws.lambda.kotlin.coroutines

import io.skrastrek.aws.lambda.kotlin.core.Context
import io.skrastrek.aws.lambda.kotlin.core.EmptyContext
import io.skrastrek.aws.lambda.kotlin.core.HandlerSerializers
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.core.RequestHandler].
 *
 * Decoding and encoding are confined to the IO dispatcher so the handler stays correct whichever dispatcher the
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
        input: Source,
        output: Sink,
        context: Context,
    ) {
        val decoded = withContext(ioDispatcher) { json.decodeFromSource(deserializer, input) }
        val result = handle(decoded, context)
        withContext(ioDispatcher) { json.encodeToSink(serializer, result, output) }
    }
}

suspend fun <I : Any, O : Any> SuspendingRequestHandler<I, O>.handle(input: I): O = handle(input, EmptyContext)

suspend fun <I : Any, O : Any> SuspendingRequestHandler<I, O>.handle(
    input: Source,
    output: Sink,
) = handle(input, output, EmptyContext)
