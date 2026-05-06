package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.json
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class ApiGatewayProxyV1EventTest {
    @Test
    fun decode_1() {
        json.decodeFromResource<ApiGatewayProxyV1Event>("api-gw-proxy-v1-event-1.json")
    }
}
