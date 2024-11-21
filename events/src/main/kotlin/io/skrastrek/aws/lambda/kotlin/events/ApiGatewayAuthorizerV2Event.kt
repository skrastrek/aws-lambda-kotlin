package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.RequestHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The V2 API Gateway customer authorizer event object as described - https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-lambda-authorizer.html
 * https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/APIGatewayV2CustomAuthorizerEvent.java
 */

interface ApiGatewayAuthorizerV2RequestHandler : RequestHandler<ApiGatewayAuthorizerV2Event, ApiGatewayAuthorizerSimpleResult> {
    override val deserializer get() = ApiGatewayAuthorizerV2Event.serializer()
    override val serializer get() = ApiGatewayAuthorizerSimpleResult.serializer()
}

@Serializable
data class ApiGatewayAuthorizerV2Event(
    val version: String,
    val type: String,
    val routeArn: String,
    val identitySource: List<String>,
    val routeKey: String,
    val rawPath: String,
    val rawQueryString: String,
    val cookies: List<String> = emptyList(),
    val headers: Map<String, String> = emptyMap(),
    val queryStringParameters: Map<String, String> = emptyMap(),
    val requestContext: ApiGatewayEventRequestContextV2,
    val pathParameters: Map<String, String> = emptyMap(),
    val stageVariables: Map<String, String> = emptyMap(),
)

@Serializable
data class ApiGatewayAuthorizerSimpleResult(
    val isAuthorized: Boolean,
    val context: JsonObject? = null,
)
