package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV2Event
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV2Result
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV2Serializers

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV2RequestHandler].
 *
 * Serializers come from [ApiGatewayProxyV2Serializers], shared with the blocking interface.
 */
interface ApiGatewayProxyV2SuspendingRequestHandler :
    SuspendingRequestHandler<ApiGatewayProxyV2Event, ApiGatewayProxyV2Result>,
    ApiGatewayProxyV2Serializers
