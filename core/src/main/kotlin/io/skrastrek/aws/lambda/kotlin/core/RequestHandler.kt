package io.skrastrek.aws.lambda.kotlin.core

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

interface RequestHandler<I : Any, O : Any> :
    RequestStreamHandler,
    HandlerSerializers<I, O> {
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

fun <I : Any, O : Any> RequestHandler<I, O>.handle(input: I): O = handle(input, EmptyContext)

fun <I : Any, O : Any> RequestHandler<I, O>.handle(
    input: InputStream,
    output: OutputStream,
) = handleRequest(input, output, EmptyContext)
