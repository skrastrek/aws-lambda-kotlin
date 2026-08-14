package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV1Event
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV1Result

/** Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV1RequestHandler]. */
interface ApiGatewayAuthorizerV1SuspendingRequestHandler :
    SuspendingRequestHandler<ApiGatewayAuthorizerV1Event, ApiGatewayAuthorizerV1Result> {
    override val deserializer get() = ApiGatewayAuthorizerV1Event.serializer()
    override val serializer get() = ApiGatewayAuthorizerV1Result.serializer()
}
