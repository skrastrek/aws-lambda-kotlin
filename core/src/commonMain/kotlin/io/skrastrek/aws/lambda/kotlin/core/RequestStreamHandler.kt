package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.io.Sink
import kotlinx.io.Source

/**
 * Handles a raw invocation: reads the event from [handle]'s `input` and writes the result to its
 * `output`. The caller flushes `output`.
 *
 * On the JVM this also implements AWS's `RequestStreamHandler`, so implementations stay deployable
 * on the managed Java runtime.
 */
expect fun interface RequestStreamHandler {
    fun handle(
        input: Source,
        output: Sink,
        context: Context,
    )
}
