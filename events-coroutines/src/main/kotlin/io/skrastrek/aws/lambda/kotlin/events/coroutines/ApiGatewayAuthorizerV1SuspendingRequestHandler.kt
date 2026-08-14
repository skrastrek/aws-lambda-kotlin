package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV1Event
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV1Result
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV1Serializers

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.ApiGatewayAuthorizerV1RequestHandler].
 *
 * Serializers come from [ApiGatewayAuthorizerV1Serializers], shared with the blocking interface.
 */
interface ApiGatewayAuthorizerV1SuspendingRequestHandler :
    SuspendingRequestHandler<ApiGatewayAuthorizerV1Event, ApiGatewayAuthorizerV1Result>,
    ApiGatewayAuthorizerV1Serializers
