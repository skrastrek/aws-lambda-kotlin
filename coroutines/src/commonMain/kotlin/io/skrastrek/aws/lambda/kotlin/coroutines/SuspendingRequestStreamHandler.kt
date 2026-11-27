package io.skrastrek.aws.lambda.kotlin.coroutines

import io.skrastrek.aws.lambda.kotlin.core.Context
import io.skrastrek.aws.lambda.kotlin.core.RequestStreamHandler
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.Source

/**
 * Suspending counterpart to [RequestStreamHandler].
 *
 * It is a sibling of [RequestStreamHandler] rather than a subtype: a suspending `handle` cannot
 * override the blocking one with the same parameters.
 *
 * On the JVM this also implements AWS's `RequestStreamHandler`, bridging into [handle] with
 * `runBlocking`, which keeps implementations deployable on the managed Java runtime. That is safe
 * because Lambda serves one invocation per thread. On the custom runtime, `LambdaNativeRuntime`
 * calls [handle] directly and the bridge is never used.
 */
expect interface SuspendingRequestStreamHandler {
    suspend fun handle(
        input: Source,
        output: Sink,
        context: Context,
    )
}

/** Views a blocking handler as a [SuspendingRequestStreamHandler] whose work runs on the IO dispatcher. */
fun RequestStreamHandler.asSuspending(): SuspendingRequestStreamHandler = BlockingRequestStreamHandler(this)

private class BlockingRequestStreamHandler(
    private val delegate: RequestStreamHandler,
) : SuspendingRequestStreamHandler {
    override suspend fun handle(
        input: Source,
        output: Sink,
        context: Context,
    ) = withContext(ioDispatcher) { delegate.handle(input, output, context) }
}
