package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.RequestHandler
import kotlinx.serialization.Serializable

/**
 * The V1 API Gateway customer authorizer event object as described - https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-lambda-authorizer-input.html
 * https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/APIGatewayCustomAuthorizerEvent.java
 */

interface ApiGatewayAuthorizerV1RequestHandler : RequestHandler<ApiGatewayAuthorizerV1Event, ApiGatewayAuthorizerV1Result> {
    override val deserializer get() = ApiGatewayAuthorizerV1Event.serializer()
    override val serializer get() = ApiGatewayAuthorizerV1Result.serializer()
}

@Serializable
data class ApiGatewayAuthorizerV1Event(
    val type: String,
    val methodArn: String,
    val resource: String,
    val path: String,
    val httpMethod: String,
    val headers: Map<String, String> = emptyMap(),
    val queryStringParameters: Map<String, String> = emptyMap(),
    val pathParameters: Map<String, String> = emptyMap(),
    val stageVariables: Map<String, String> = emptyMap(),
    val requestContext: RequestContext,
) {
    @Serializable
    data class RequestContext(
        val path: String,
        val accountId: String,
        val resourceId: String,
        val stage: String,
        val requestId: String,
        val identity: Identity,
        val resourcePath: String,
        val httpMethod: String,
        val apiId: String,
    )

    @Serializable
    data class Identity(
        val apiKey: String? = null,
        val sourceIp: String,
    )
}
