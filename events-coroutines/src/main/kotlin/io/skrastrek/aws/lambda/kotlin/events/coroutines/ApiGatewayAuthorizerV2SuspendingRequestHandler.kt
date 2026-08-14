package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerSimpleResult
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV2Event
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV2Serializers

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV2RequestHandler].
 *
 * Serializers come from [ApiGatewayAuthorizerV2Serializers], shared with the blocking interface.
 */
interface ApiGatewayAuthorizerV2SuspendingRequestHandler :
    SuspendingRequestHandler<ApiGatewayAuthorizerV2Event, ApiGatewayAuthorizerSimpleResult>,
    ApiGatewayAuthorizerV2Serializers
