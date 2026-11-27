package io.skrastrek.aws.lambda.kotlin.core

/** Information about the current invocation, available on every platform. */
interface Context {
    val awsRequestId: String
    val logGroupName: String
    val logStreamName: String
    val functionName: String
    val functionVersion: String
    val invokedFunctionArn: String
    val remainingTimeInMillis: Int
    val memoryLimitInMB: Int
    val logger: LambdaLogger
}

fun interface LambdaLogger {
    fun log(message: String)
}
