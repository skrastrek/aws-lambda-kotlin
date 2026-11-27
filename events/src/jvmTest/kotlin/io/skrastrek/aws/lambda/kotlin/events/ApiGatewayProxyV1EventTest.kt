package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.defaultJson
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class ApiGatewayProxyV1EventTest {
    @Test
    fun decode_1() {
        defaultJson.decodeFromResource<ApiGatewayProxyV1Event>("api-gw-proxy-v1-event-1.json")
    }
}
