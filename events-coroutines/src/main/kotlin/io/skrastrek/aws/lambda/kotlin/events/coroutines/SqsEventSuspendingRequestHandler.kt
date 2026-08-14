package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.BatchEventResponse
import io.skrastrek.aws.lambda.kotlin.events.SqsEvent

/** Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.SqsEventRequestHandler]. */
interface SqsEventSuspendingRequestHandler : SuspendingRequestHandler<SqsEvent, BatchEventResponse> {
    override val deserializer get() = SqsEvent.serializer()
    override val serializer get() = BatchEventResponse.serializer()
}
