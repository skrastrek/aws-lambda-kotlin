package io.skrastrek.aws.lambda.kotlin.coroutines

import io.skrastrek.aws.lambda.kotlin.core.Context
import kotlinx.io.Sink
import kotlinx.io.Source

actual interface SuspendingRequestStreamHandler {
    actual suspend fun handle(
        input: Source,
        output: Sink,
        context: Context,
    )
}
