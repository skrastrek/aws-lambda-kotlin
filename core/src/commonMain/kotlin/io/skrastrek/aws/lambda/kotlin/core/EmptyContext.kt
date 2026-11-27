package io.skrastrek.aws.lambda.kotlin.core

data object EmptyContext : Context {
    override val awsRequestId = ""
    override val logGroupName = ""
    override val logStreamName = ""
    override val functionName = ""
    override val functionVersion = ""
    override val invokedFunctionArn = ""
    override val remainingTimeInMillis = 0
    override val memoryLimitInMB = 0
    override val logger = LambdaLogger(::println)
}
