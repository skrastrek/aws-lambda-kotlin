@file:OptIn(ExperimentalSerializationApi::class)

package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

interface RequestHandler<I : Any, O : Any> :
    RequestStreamHandler,
    HandlerSerializers<I, O> {
    fun handle(
        input: I,
        context: Context,
    ): O

    override fun handle(
        input: Source,
        output: Sink,
        context: Context,
    ) = json.encodeToSink(serializer, handle(json.decodeFromSource(deserializer, input), context), output)
}

fun <I : Any, O : Any> RequestHandler<I, O>.handle(input: I): O = handle(input, EmptyContext)

fun <I : Any, O : Any> RequestHandler<I, O>.handle(
    input: Source,
    output: Sink,
) = handle(input, output, EmptyContext)
