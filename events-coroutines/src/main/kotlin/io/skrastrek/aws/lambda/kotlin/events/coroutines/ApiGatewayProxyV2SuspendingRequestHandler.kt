package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV2Event
import io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV2Result

/** Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.ApiGatewayProxyV2RequestHandler]. */
interface ApiGatewayProxyV2SuspendingRequestHandler : SuspendingRequestHandler<ApiGatewayProxyV2Event, ApiGatewayProxyV2Result> {
    override val deserializer get() = ApiGatewayProxyV2Event.serializer()
    override val serializer get() = ApiGatewayProxyV2Result.serializer()
}
