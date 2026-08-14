package io.skrastrek.aws.lambda.kotlin.events.coroutines

import com.amazonaws.services.lambda.runtime.Context
import io.skrastrek.aws.lambda.kotlin.coroutines.handle
import io.skrastrek.aws.lambda.kotlin.events.BatchEventResponse
import io.skrastrek.aws.lambda.kotlin.events.DynamoDbEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamoDbEventSuspendingRequestHandlerTest {
    @Test
    fun `decodes the event and encodes the batch response`() =
        runTest {
            val input = ByteArrayInputStream("""{"Records":[]}""".toByteArray())
            val output = ByteArrayOutputStream()

            NoFailuresHandler.handle(input, output)

            assertEquals("""{"batchItemFailures":[]}""", output.toString(Charsets.UTF_8))
        }
}

private object NoFailuresHandler : DynamoDbEventSuspendingRequestHandler {
    override suspend fun handle(
        input: DynamoDbEvent,
        context: Context,
    ): BatchEventResponse {
        delay(1)
        return BatchEventResponse(emptyList())
    }
}
