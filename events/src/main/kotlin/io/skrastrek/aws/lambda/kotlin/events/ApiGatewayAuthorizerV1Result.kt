@file:OptIn(ExperimentalSerializationApi::class)

package io.skrastrek.aws.lambda.kotlin.events

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiGatewayAuthorizerV1Result(
    val principalId: String,
    val policyDocument: IamPolicyDocument,
    val context: Map<String, String> = emptyMap(),
    val usageIdentifierKey: String? = null,
)

@Serializable
data class IamPolicyDocument(
    @SerialName("Version")
    @EncodeDefault
    val version: String = "2012-10-17",
    @SerialName("Statement")
    val statement: List<Statement>,
) {
    @Serializable
    data class Statement(
        @SerialName("Effect")
        val effect: Effect,
        @SerialName("Action")
        val actions: List<String>,
        @SerialName("Resource")
        val resources: List<String>,
    )

    enum class Effect {
        @SerialName("Allow")
        ALLOW,

        @SerialName("Deny")
        DENY,
    }
}
