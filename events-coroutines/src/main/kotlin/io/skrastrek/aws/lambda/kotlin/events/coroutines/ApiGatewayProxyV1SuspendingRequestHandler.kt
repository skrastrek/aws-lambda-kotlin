package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV1Event
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV1Result

/** Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV1RequestHandler]. */
interface ApiGatewayProxyV1SuspendingRequestHandler : SuspendingRequestHandler<ApiGatewayProxyV1Event, ApiGatewayProxyV1Result> {
    override val deserializer get() = ApiGatewayProxyV1Event.serializer()
    override val serializer get() = ApiGatewayProxyV1Result.serializer()
}
