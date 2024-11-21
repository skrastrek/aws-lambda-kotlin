package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.RequestHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

interface ApiGatewayProxyV1RequestHandler : RequestHandler<ApiGatewayProxyV1Event, ApiGatewayProxyV1Result> {
    override val deserializer get() = ApiGatewayProxyV1Event.serializer()
    override val serializer get() = ApiGatewayProxyV1Result.serializer()
}

@Serializable
data class ApiGatewayProxyV1Event(
    val resource: String,
    val path: String,
    val httpMethod: String,
    val headers: Map<String, String> = emptyMap(),
    val queryStringParameters: Map<String, String> = emptyMap(),
    val multiValueHeaders: Map<String, List<String>> = emptyMap(),
    val multiValueQueryStringParameters: Map<String, List<String>> = emptyMap(),
    val requestContext: ApiGatewayEventRequestContextV1,
    val body: String? = null,
    val pathParameters: Map<String, String> = emptyMap(),
    val isBase64Encoded: Boolean,
    val stageVariables: Map<String, String> = emptyMap(),
)

@Serializable
data class ApiGatewayProxyV1Result(
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap(),
    val multiValueHeaders: Map<String, List<String>> = emptyMap(),
    val body: String? = null,
    val isBase64Encoded: Boolean = false,
)

@Serializable
data class ApiGatewayEventRequestContextV1(
    val accountId: String,
    val apiId: String,
    val authorizer: JsonObject? = null,
    val domainName: String,
    val domainPrefix: String,
    val extendedRequestId: String,
    val httpMethod: String,
    val identity: Identity,
    val operationName: String? = null,
    val path: String,
    val protocol: String,
    val requestId: String,
    val requestTime: String,
    val requestTimeEpoch: Long,
    val resourceId: String? = null,
    val resourcePath: String,
    val stage: String,
) {
    @Serializable
    data class Identity(
        val accountId: String? = null,
        val accessKey: String? = null,
        val userArn: String? = null,
        val user: String? = null,
    )
}
