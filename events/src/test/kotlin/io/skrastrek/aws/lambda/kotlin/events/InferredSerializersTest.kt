package io.skrastrek.aws.lambda.kotlin.events

import com.amazonaws.services.lambda.runtime.Context
import io.skrastrek.aws.lambda.kotlin.core.RequestHandler
import io.skrastrek.aws.lambda.kotlin.core.handle
import io.skrastrek.aws.lambda.kotlin.core.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

/**
 * A handler for a library event type, written without the per-event interfaces and without naming
 * either serializer — the crux of what inference has to support.
 */
class InferredSerializersTest {
    @Test
    fun handles_an_sqs_event_with_contextual_serializers() {
        val output = ByteArrayOutputStream()

        ContextualSqsHandler.handle(Utils::class.java.classLoader.getResourceAsStream("sqs-event-1.json")!!, output)

        assertEquals(expectedFailures(), output.toString(Charsets.UTF_8))
    }

    @Test
    fun handles_an_sqs_event_without_declared_serializers() {
        val input = Utils::class.java.classLoader.getResourceAsStream("sqs-event-1.json")!!
        val output = ByteArrayOutputStream()

        FailEverySqsRecord.handle(input, output)

        assertEquals(expectedFailures(), output.toString(Charsets.UTF_8))
    }
}

private fun expectedFailures(): String =
    json.encodeToString(
        BatchEventResponse(
            json.decodeFromResource<SqsEvent>("sqs-event-1.json").records.map { BatchItemFailure(it.messageId) },
        ),
    )

private object FailEverySqsRecord : RequestHandler<SqsEvent, BatchEventResponse> {
    override fun handle(
        input: SqsEvent,
        context: Context,
    ): BatchEventResponse = BatchEventResponse(input.records.map { BatchItemFailure(it.messageId) })
}

/**
 * Serializers registered contextually rather than looked up. This is the reflection-free route for
 * handlers a GraalVM image build cannot see, so the wiring has to reach the inference.
 */
private object ContextualSqsHandler : RequestHandler<SqsEvent, BatchEventResponse> {
    override val json =
        Json(io.skrastrek.aws.lambda.kotlin.core.json) {
            serializersModule =
                SerializersModule {
                    contextual(SqsEvent::class, SqsEvent.serializer())
                    contextual(BatchEventResponse::class, BatchEventResponse.serializer())
                }
        }

    override fun handle(
        input: SqsEvent,
        context: Context,
    ): BatchEventResponse = BatchEventResponse(input.records.map { BatchItemFailure(it.messageId) })
}
