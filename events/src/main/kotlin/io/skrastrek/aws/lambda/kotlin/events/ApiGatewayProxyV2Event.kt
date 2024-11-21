package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.RequestHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format
 * https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/APIGatewayV2HTTPEvent.java
 */

interface ApiGatewayProxyV2RequestHandler : RequestHandler<ApiGatewayProxyV2Event, ApiGatewayProxyV2Result> {
    override val deserializer get() = ApiGatewayProxyV2Event.serializer()
    override val serializer get() = ApiGatewayProxyV2Result.serializer()
}

@Serializable
data class ApiGatewayProxyV2Event(
    val version: String,
    val routeKey: String,
    val rawPath: String,
    val rawQueryString: String,
    val cookies: List<String> = emptyList(),
    val headers: Map<String, String> = emptyMap(),
    val queryStringParameters: Map<String, String> = emptyMap(),
    val requestContext: ApiGatewayEventRequestContextV2,
    val body: String? = null,
    val pathParameters: Map<String, String> = emptyMap(),
    val isBase64Encoded: Boolean,
    val stageVariables: Map<String, String> = emptyMap(),
)

@Serializable
data class ApiGatewayProxyV2Result(
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap(),
    val cookies: List<String> = emptyList(),
    val body: String? = null,
    val isBase64Encoded: Boolean = false,
)

@Serializable
data class ApiGatewayEventRequestContextV2(
    val accountId: String,
    val apiId: String,
    val authorizer: Authorizer? = null,
    val domainName: String,
    val domainPrefix: String,
    val http: Http,
    val requestId: String,
    val routeKey: String,
    val stage: String,
    val timeEpoch: Long,
) {
    @Serializable
    data class Authorizer(
        val jwt: Jwt? = null,
        val lambda: JsonObject? = null,
        val iam: Iam? = null,
    )

    @Serializable
    data class CognitoIdentity(
        val amr: List<String>,
        val identityId: String,
        val identityPoolId: String,
    )

    @Serializable
    data class Http(
        val method: String,
        val path: String,
        val protocol: String,
        val sourceIp: String,
        val userAgent: String,
    )

    @Serializable
    data class Iam(
        val accessKey: String,
        val accountId: String,
        val callerId: String,
        val cognitoIdentity: CognitoIdentity,
        val principalOrgId: String,
        val userArn: String,
        val userId: String,
    )

    @Serializable
    data class Jwt(
        val claims: Map<String, String> = emptyMap(),
        val scopes: List<String> = emptyList(),
    )
}
