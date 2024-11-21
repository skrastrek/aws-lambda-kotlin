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
    ) {
        val request: I = readInput(input)
        val response: O = handleRequest(request, context)
        writeOutput(response, output)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readInput(input: InputStream): I = json.decodeFromStream(deserializer, input)

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeOutput(
        result: O,
        output: OutputStream,
    ) = json.encodeToStream(serializer, result, output)
}