package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV1Event
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV1Result
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV1Serializers

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV1RequestHandler].
 *
 * Serializers come from [ApiGatewayProxyV1Serializers], shared with the blocking interface.
 */
interface ApiGatewayProxyV1SuspendingRequestHandler :
    SuspendingRequestHandler<ApiGatewayProxyV1Event, ApiGatewayProxyV1Result>,
    ApiGatewayProxyV1Serializers
