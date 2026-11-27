package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.core.Context
import io.skrastrek.aws.lambda.kotlin.coroutines.handle
import io.skrastrek.aws.lambda.kotlin.events.BatchEventResponse
import io.skrastrek.aws.lambda.kotlin.events.DynamoDbEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamoDbEventSuspendingRequestHandlerTest {
    @Test
    fun `decodes the event and encodes the batch response`() =
        runTest {
            val input = Buffer().apply { writeString("""{"Records":[]}""") }
            val output = Buffer()

            NoFailuresHandler.handle(input, output)

            assertEquals("""{"batchItemFailures":[]}""", output.readString())
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
