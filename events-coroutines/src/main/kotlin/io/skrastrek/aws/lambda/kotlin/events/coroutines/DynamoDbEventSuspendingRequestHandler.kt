package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.BatchEventResponse
import io.skrastrek.aws.lambda.kotlin.events.DynamoDbEvent

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.DynamoDbEventRequestHandler].
 *
 * The serializer bindings are restated rather than inherited from the blocking interface: the two
 * declare `handle(I, Context)` with and without `suspend`, which cannot coexist in one type.
 */
interface DynamoDbEventSuspendingRequestHandler : SuspendingRequestHandler<DynamoDbEvent, BatchEventResponse> {
    override val deserializer get() = DynamoDbEvent.serializer()
    override val serializer get() = BatchEventResponse.serializer()
}
