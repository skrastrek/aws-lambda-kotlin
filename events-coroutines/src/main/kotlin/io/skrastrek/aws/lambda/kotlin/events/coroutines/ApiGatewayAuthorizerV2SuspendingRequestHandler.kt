package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerSimpleResult
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV2Event

/** Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV2RequestHandler]. */
interface ApiGatewayAuthorizerV2SuspendingRequestHandler :
    SuspendingRequestHandler<ApiGatewayAuthorizerV2Event, ApiGatewayAuthorizerSimpleResult> {
    override val deserializer get() = ApiGatewayAuthorizerV2Event.serializer()
    override val serializer get() = ApiGatewayAuthorizerSimpleResult.serializer()
}
