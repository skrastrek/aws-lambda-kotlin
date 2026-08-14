package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

/**
 * The serializer pair a typed handler needs, independent of whether that handler blocks or suspends.
 *
 * Both `RequestHandler` and `SuspendingRequestHandler` extend this, which lets an event type state
 * its serializers once and mix them into either style:
 *
 * ```
 * interface DynamoDbEventSerializers : HandlerSerializers<DynamoDbEvent, BatchEventResponse> {
 *     override val deserializer get() = DynamoDbEvent.serializer()
 *     override val serializer get() = BatchEventResponse.serializer()
 * }
 *
 * interface DynamoDbEventRequestHandler :
 *     RequestHandler<DynamoDbEvent, BatchEventResponse>, DynamoDbEventSerializers
 * ```
 *
 * The shared supertype is what makes that compile. An unrelated interface declaring `deserializer`
 * and `serializer` would not: Kotlin reports "must override because it inherits multiple interface
 * methods for it" when the members come from two independent declarations. Routing both styles
 * through this one leaves exactly a single implementation to resolve to.
 */
interface HandlerSerializers<I : Any, O : Any> {
    val deserializer: DeserializationStrategy<I>
    val serializer: SerializationStrategy<O>
}
