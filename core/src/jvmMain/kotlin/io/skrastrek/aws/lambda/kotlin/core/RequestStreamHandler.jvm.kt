package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.InputStream
import java.io.OutputStream
import com.amazonaws.services.lambda.runtime.Context as AwsContext
import com.amazonaws.services.lambda.runtime.RequestStreamHandler as AwsRequestStreamHandler

actual fun interface RequestStreamHandler : AwsRequestStreamHandler {
    actual fun handle(
        input: Source,
        output: Sink,
        context: Context,
    )

    /** AWS's entry point. Named by the platform contract, not by us — implement [handle] instead. */
    override fun handleRequest(
        input: InputStream,
        output: OutputStream,
        context: AwsContext,
    ) {
        val sink = output.asSink().buffered()
        handle(input.asSource().buffered(), sink, context.asContext())
        sink.flush()
    }
}
