package io.skrastrek.aws.lambda.kotlin.events.coroutines

import io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler
import io.skrastrek.aws.lambda.kotlin.events.BatchEventResponse
import io.skrastrek.aws.lambda.kotlin.events.DynamoDbEvent
import io.skrastrek.aws.lambda.kotlin.events.DynamoDbEventSerializers

/**
 * Suspending counterpart to [io.skrastrek.aws.lambda.kotlin.events.DynamoDbEventRequestHandler].
 *
 * Serializers come from [DynamoDbEventSerializers], shared with the blocking interface.
 */
interface DynamoDbEventSuspendingRequestHandler :
    SuspendingRequestHandler<DynamoDbEvent, BatchEventResponse>,
    DynamoDbEventSerializers
