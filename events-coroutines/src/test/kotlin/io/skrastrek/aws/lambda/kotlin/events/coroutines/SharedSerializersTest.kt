package io.skrastrek.aws.lambda.kotlin.events.coroutines

import com.amazonaws.services.lambda.runtime.Context
import io.skrastrek.aws.lambda.kotlin.core.handle
import io.skrastrek.aws.lambda.kotlin.coroutines.handle
import io.skrastrek.aws.lambda.kotlin.events.BatchEventResponse
import io.skrastrek.aws.lambda.kotlin.events.BatchItemFailure
import io.skrastrek.aws.lambda.kotlin.events.DynamoDbEvent
import io.skrastrek.aws.lambda.kotlin.events.DynamoDbEventRequestHandler
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The blocking and suspending interfaces for an event both mix in the same `…Serializers`
 * interface, so the serializer pair is declared once. This pins that down: drift between the two
 * would show up as differing output for identical input.
 */
class SharedSerializersTest {
    private val event = """{"Records":[]}"""
    private val expected = """{"batchItemFailures":[{"itemIdentifier":"id-1"}]}"""

    @Test
    fun `blocking handler encodes via the shared serializers`() {
        val output = ByteArrayOutputStream()

        Blocking.handle(ByteArrayInputStream(event.toByteArray()), output)

        assertEquals(expected, output.toString(Charsets.UTF_8))
    }

    @Test
    fun `suspending handler encodes identically`() =
        runTest {
            val output = ByteArrayOutputStream()

            Suspending.handle(ByteArrayInputStream(event.toByteArray()), output)

            assertEquals(expected, output.toString(Charsets.UTF_8))
        }
}

private val oneFailure = BatchEventResponse(listOf(BatchItemFailure("id-1")))

private object Blocking : DynamoDbEventRequestHandler {
    override fun handle(
        input: DynamoDbEvent,
        context: Context,
    ) = oneFailure
}

private object Suspending : DynamoDbEventSuspendingRequestHandler {
    override suspend fun handle(
        input: DynamoDbEvent,
        context: Context,
    ) = oneFailure
}
