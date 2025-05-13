package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.json
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class DynamoDbEventTest {
    @Test
    fun decode_1() {
        json.decodeFromResource<DynamoDbEvent>("dynamodb-event-1.json")
    }

    @Test
    fun decode_2() {
        json.decodeFromResource<DynamoDbEvent>("dynamodb-event-2.json")
    }
}
