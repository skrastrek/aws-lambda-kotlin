package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

/** The configuration every handler uses unless it overrides [HandlerSerializers.json]. */
@OptIn(ExperimentalSerializationApi::class)
val defaultJson =
    Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

/**
 * The serialization a typed handler needs, independent of whether that handler blocks or suspends.
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

    /**
     * The [Json] used to decode the event and encode the result. Defaults to [defaultJson].
     *
     * Override to supply a [kotlinx.serialization.modules.SerializersModule] — registering
     * contextual serializers explicitly is what keeps serializer lookup off the reflective path,
     * which a GraalVM native image cannot follow without extra configuration:
     *
     * ```
     * override val json = Json(defaultJson) {
     *     serializersModule = SerializersModule {
     *         contextual(ApiGatewayProxyV1Event::class, ApiGatewayProxyV1Event.serializer())
     *     }
     * }
     * ```
     *
     * `Json(defaultJson) { … }` copies the base configuration, but assigning `serializersModule`
     * *replaces* it rather than adding to it. To keep an existing module, combine them:
     * `serializersModule = defaultJson.serializersModule + SerializersModule { … }`.
     *
     * Because this lives on [HandlerSerializers], an event's `…Serializers` interface can set it
     * once and both the blocking and suspending handlers for that event pick it up.
     */
    val json: Json get() = defaultJson
}
