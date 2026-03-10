package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.json
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class SqsEventTest {
    @Test
    fun decode_1() {
        json.decodeFromResource<SqsEvent>("sqs-event-1.json")
    }

    @Test
    fun decode_2() {
        json.decodeFromResource<SqsEvent>("sqs-event-2.json")
    }
}
