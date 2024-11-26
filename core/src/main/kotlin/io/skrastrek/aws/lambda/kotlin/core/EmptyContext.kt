package io.skrastrek.aws.lambda.kotlin.core

import com.amazonaws.services.lambda.runtime.Context

data object EmptyContext : Context {
    override fun getAwsRequestId() = ""

    override fun getLogGroupName() = ""

    override fun getLogStreamName() = ""

    override fun getFunctionName() = ""

    override fun getFunctionVersion() = ""

    override fun getInvokedFunctionArn() = ""

    override fun getIdentity() = null

    override fun getClientContext() = null

    override fun getRemainingTimeInMillis() = 0

    override fun getMemoryLimitInMB() = 0

    override fun getLogger() = null
}
