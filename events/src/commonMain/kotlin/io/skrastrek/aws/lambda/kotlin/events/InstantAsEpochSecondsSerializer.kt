package io.skrastrek.aws.lambda.kotlin.events

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

typealias InstantAsEpochSeconds =
    @Serializable(with = InstantAsEpochSecondsSerializer::class)
    Instant

object InstantAsEpochSecondsSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("io.skrastrek.aws.lambda.kotlin.events.InstantAsEpochSeconds", PrimitiveKind.DOUBLE)

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeDouble(value.epochSeconds.toDouble())
    }

    override fun deserialize(decoder: Decoder) = Instant.fromEpochSeconds(decoder.decodeDouble().toLong())
}
