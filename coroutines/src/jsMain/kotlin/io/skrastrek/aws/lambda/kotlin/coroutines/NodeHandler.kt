package io.skrastrek.aws.lambda.kotlin.coroutines

import io.skrastrek.aws.lambda.kotlin.core.NodeLambdaContext
import io.skrastrek.aws.lambda.kotlin.core.asContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.js.Promise

/**
 * Handles an invocation from the Node.js Lambda runtime as a [Promise], which the runtime awaits.
 *
 * ```
 * @JsExport
 * fun handler(event: Any?, context: NodeLambdaContext): Promise<Any?> = MyHandler.handleNode(event, context)
 * ```
 */
@OptIn(DelicateCoroutinesApi::class)
fun SuspendingRequestStreamHandler.handleNode(
    event: Any?,
    context: NodeLambdaContext,
): Promise<Any?> =
    // GlobalScope: the runtime owns the invocation's lifetime and awaits the returned Promise.
    GlobalScope.promise {
        val output = Buffer()
        handle(Buffer().apply { writeString(JSON.stringify(event).unsafeCast<String?>() ?: "null") }, output, context.asContext())
        output.readString().takeIf { it.isNotEmpty() }?.let { JSON.parse<Any?>(it) }
    }
