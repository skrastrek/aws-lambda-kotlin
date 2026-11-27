package io.skrastrek.aws.lambda.kotlin.core

import com.amazonaws.services.lambda.runtime.Context as AwsContext

/** Views the managed Java runtime's context as this library's [Context]. */
fun AwsContext.asContext(): Context = AwsContextAdapter(this)

private class AwsContextAdapter(
    private val delegate: AwsContext,
) : Context {
    override val awsRequestId: String get() = delegate.awsRequestId
    override val logGroupName: String get() = delegate.logGroupName
    override val logStreamName: String get() = delegate.logStreamName
    override val functionName: String get() = delegate.functionName
    override val functionVersion: String get() = delegate.functionVersion
    override val invokedFunctionArn: String get() = delegate.invokedFunctionArn
    override val remainingTimeInMillis: Int get() = delegate.remainingTimeInMillis
    override val memoryLimitInMB: Int get() = delegate.memoryLimitInMB
    override val logger = LambdaLogger { delegate.logger?.log(it) ?: println(it) }
}
