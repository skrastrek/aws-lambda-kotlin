package io.skrastrek.aws.lambda.kotlin.events

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias InstantAsEpochSeconds =
    @Serializable(with = InstantAsEpochSecondsSerializer::class)
    Instant

object InstantAsEpochSecondsSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("io.skrastrek.aws.lambda.kotlin.events.InstantAsEpochSeconds", PrimitiveKind.LONG)

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeLong(value.epochSeconds)
    }

    override fun deserialize(decoder: Decoder) = Instant.fromEpochSeconds(decoder.decodeLong())
}
