package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.BatchEventResponse
import io.skrastrek.aws.lambda.kotlin.events.SqsEvent
import io.skrastrek.aws.lambda.kotlin.events.SqsEventSerializers

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.SqsEventRequestHandler].
 *
 * Serializers come from [SqsEventSerializers], shared with the blocking interface.
 */
interface SqsEventSuspendingRequestHandler :
    SuspendingRequestHandler<SqsEvent, BatchEventResponse>,
    SqsEventSerializers
