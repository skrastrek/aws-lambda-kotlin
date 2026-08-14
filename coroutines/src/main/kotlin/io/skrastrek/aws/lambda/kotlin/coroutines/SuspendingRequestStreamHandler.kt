package io.skrastrek.aws.lambda.kotlin.coroutines

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/**
 * Suspending counterpart to [RequestStreamHandler].
 *
 * `handle` is the name to implement across this library; `handleRequest` belongs to AWS. Here the
 * distinction is also forced: a `suspend fun handleRequest` with these parameters is read as an
 * attempt to override the inherited blocking one, which the compiler rejects with "suspend function
 * cannot override non-suspend function".
 *
 * Extending [RequestStreamHandler] keeps implementations deployable on the AWS managed Java
 * runtime, whose bootstrap resolves handlers by that exact type. On that path [handleRequest]
 * bridges into [handle] with [runBlocking], which is safe because Lambda serves one invocation per
 * thread. On the custom runtime, `LambdaNativeRuntime` calls [handle] directly and the bridge is
 * never used.
 */
interface SuspendingRequestStreamHandler : RequestStreamHandler {
    suspend fun handle(
        input: InputStream,
        output: OutputStream,
        context: Context,
    )

    override fun handleRequest(
        input: InputStream,
        output: OutputStream,
        context: Context,
    ) = runBlocking { handle(input, output, context) }
}

/**
 * Views this handler as a [SuspendingRequestStreamHandler].
 *
 * Returns the receiver unchanged when it already suspends, so callers never pay for a
 * [runBlocking] bridge that would pin a thread for the duration of an invocation. A blocking
 * handler is wrapped so its work runs on [IO].
 */
fun RequestStreamHandler.asSuspending(): SuspendingRequestStreamHandler =
    this as? SuspendingRequestStreamHandler ?: BlockingRequestStreamHandler(this)

private class BlockingRequestStreamHandler(
    private val delegate: RequestStreamHandler,
) : SuspendingRequestStreamHandler {
    override suspend fun handle(
        input: InputStream,
        output: OutputStream,
        context: Context,
    ) = withContext(IO) { delegate.handleRequest(input, output, context) }

    override fun handleRequest(
        input: InputStream,
        output: OutputStream,
        context: Context,
    ) = delegate.handleRequest(input, output, context)
}
