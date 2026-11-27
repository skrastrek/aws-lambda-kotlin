package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString

/** The `context` object the Node.js Lambda runtime passes to a handler. */
external interface NodeLambdaContext {
    val awsRequestId: String
    val logGroupName: String
    val logStreamName: String
    val functionName: String
    val functionVersion: String
    val invokedFunctionArn: String
    val memoryLimitInMB: String

    fun getRemainingTimeInMillis(): Int
}

/** Views the Node.js runtime's context as this library's [Context]. */
fun NodeLambdaContext.asContext(): Context = NodeContextAdapter(this)

/**
 * Handles an invocation from the Node.js Lambda runtime, which passes the event already parsed.
 *
 * The event goes through JSON text so decoding uses the handler's own [kotlinx.serialization.json.Json],
 * exactly as on the other platforms. Export a function that delegates here:
 *
 * ```
 * @JsExport
 * fun handler(event: Any?, context: NodeLambdaContext): Any? = MyHandler.handleNode(event, context)
 * ```
 */
fun RequestStreamHandler.handleNode(
    event: Any?,
    context: NodeLambdaContext,
): Any? {
    val output = Buffer()
    handle(jsonSource(event), output, context.asContext())
    return parseJsonOutput(output)
}

private fun jsonSource(event: Any?) = Buffer().apply { writeString(JSON.stringify(event).unsafeCast<String?>() ?: "null") }

/** An empty output becomes `undefined`, which Lambda returns as `null`. */
private fun parseJsonOutput(output: Buffer): Any? = output.readString().takeIf { it.isNotEmpty() }?.let { JSON.parse(it) }

private class NodeContextAdapter(
    private val delegate: NodeLambdaContext,
) : Context {
    override val awsRequestId get() = delegate.awsRequestId
    override val logGroupName get() = delegate.logGroupName
    override val logStreamName get() = delegate.logStreamName
    override val functionName get() = delegate.functionName
    override val functionVersion get() = delegate.functionVersion
    override val invokedFunctionArn get() = delegate.invokedFunctionArn
    override val remainingTimeInMillis get() = delegate.getRemainingTimeInMillis()
    override val memoryLimitInMB get() = delegate.memoryLimitInMB.toInt()
    override val logger = LambdaLogger(::println)
}
