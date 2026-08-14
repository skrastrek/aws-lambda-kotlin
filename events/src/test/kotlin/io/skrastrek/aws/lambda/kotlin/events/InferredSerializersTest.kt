package io.skrastrek.aws.lambda.kotlin.events

import com.amazonaws.services.lambda.runtime.Context
import io.skrastrek.aws.lambda.kotlin.core.RequestHandler
import io.skrastrek.aws.lambda.kotlin.core.handle
import io.skrastrek.aws.lambda.kotlin.core.json
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

/**
 * A handler for a library event type, written without the per-event interfaces and without naming
 * either serializer — the crux of what inference has to support.
 */
class InferredSerializersTest {
    @Test
    fun handles_an_sqs_event_without_declared_serializers() {
        val input = Utils::class.java.classLoader.getResourceAsStream("sqs-event-1.json")!!
        val output = ByteArrayOutputStream()

        FailEverySqsRecord.handle(input, output)

        val expected =
            BatchEventResponse(
                json
                    .decodeFromResource<SqsEvent>("sqs-event-1.json")
                    .records
                    .map { BatchItemFailure(it.messageId) },
            )

        assertEquals(json.encodeToString(expected), output.toString(Charsets.UTF_8))
    }
}

private object FailEverySqsRecord : RequestHandler<SqsEvent, BatchEventResponse> {
    override fun handle(
        input: SqsEvent,
        context: Context,
    ): BatchEventResponse = BatchEventResponse(input.records.map { BatchItemFailure(it.messageId) })
}
