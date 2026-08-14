package io.skrastrek.aws.lambda.kotlin.core

import com.amazonaws.services.lambda.runtime.Context
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * A handler can replace the [Json] used for its event and result. Registering serializers
 * explicitly through a module is what keeps lookup off the reflective path that a GraalVM native
 * image cannot follow.
 */
class HandlerJsonTest {
    private val payload = """{"payload":"hello"}"""

    @Test
    fun `overridden json supplies the serializers module`() {
        val output = ByteArrayOutputStream()

        ContextualHandler.handle(ByteArrayInputStream(payload.toByteArray()), output)

        assertEquals("""{"payload":"HELLO"}""", output.toString(Charsets.UTF_8))
    }

    @Test
    fun `the default json has no such serializer, so the override is load-bearing`() {
        assertFailsWith<SerializationException> {
            DefaultJsonHandler.handle(ByteArrayInputStream(payload.toByteArray()), ByteArrayOutputStream())
        }
    }
}

@Serializable
private data class Envelope(
    @Contextual val payload: Payload,
)

/** Deliberately not `@Serializable` — only reachable through a contextual registration. */
private data class Payload(
    val value: String,
)

private object PayloadSerializer : KSerializer<Payload> {
    override val descriptor = PrimitiveSerialDescriptor("Payload", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Payload,
    ) = encoder.encodeString(value.value)

    override fun deserialize(decoder: Decoder) = Payload(decoder.decodeString())
}

private object ContextualHandler : RequestHandler<Envelope, Envelope> {
    override val deserializer get() = Envelope.serializer()
    override val serializer get() = Envelope.serializer()

    override val json =
        Json(defaultJson) {
            serializersModule =
                SerializersModule {
                    contextual(Payload::class, PayloadSerializer)
                }
        }

    override fun handle(
        input: Envelope,
        context: Context,
    ) = Envelope(Payload(input.payload.value.uppercase()))
}

private object DefaultJsonHandler : RequestHandler<Envelope, Envelope> {
    override val deserializer get() = Envelope.serializer()
    override val serializer get() = Envelope.serializer()

    override fun handle(
        input: Envelope,
        context: Context,
    ) = input
}
