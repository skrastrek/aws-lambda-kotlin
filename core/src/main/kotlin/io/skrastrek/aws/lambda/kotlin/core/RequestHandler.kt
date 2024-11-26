package io.skrastrek.aws.lambda.kotlin.core

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
val json =
    Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

interface RequestHandler<I : Any, O : Any> : RequestStreamHandler {
    val deserializer: DeserializationStrategy<I>
    val serializer: SerializationStrategy<O>

    fun handleRequest(
        input: I,
        context: Context,
    ): O

    override fun handleRequest(
        input: InputStream,
        output: OutputStream,
        context: Context,
    ) = handleRequest(input.jsonDecode(), context).jsonEncodeTo(output)

    @OptIn(ExperimentalSerializationApi::class)
    private fun InputStream.jsonDecode(): I = json.decodeFromStream(deserializer, this)

    @OptIn(ExperimentalSerializationApi::class)
    private fun O.jsonEncodeTo(output: OutputStream) = json.encodeToStream(serializer, this, output)
}

fun <I : Any, O : Any> RequestHandler<I, O>.handleRequest(input: I): O = handleRequest(input, EmptyContext)

fun <I : Any, O : Any> RequestHandler<I, O>.handleRequest(
    input: InputStream,
    output: OutputStream,
) = handleRequest(input, output, EmptyContext)
