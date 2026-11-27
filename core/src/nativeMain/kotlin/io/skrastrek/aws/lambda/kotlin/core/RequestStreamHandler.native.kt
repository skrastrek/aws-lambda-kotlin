package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.io.Sink
import kotlinx.io.Source

actual fun interface RequestStreamHandler {
    actual fun handle(
        input: Source,
        output: Sink,
        context: Context,
    )
}
