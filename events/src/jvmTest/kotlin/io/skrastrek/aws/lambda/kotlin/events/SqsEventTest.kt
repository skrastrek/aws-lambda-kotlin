package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.defaultJson
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class SqsEventTest {
    @Test
    fun decode_1() {
        defaultJson.decodeFromResource<SqsEvent>("sqs-event-1.json")
    }

    @Test
    fun decode_2() {
        defaultJson.decodeFromResource<SqsEvent>("sqs-event-2.json")
    }
}
