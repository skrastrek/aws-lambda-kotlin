package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.RequestHandler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface DynamoDbEventRequestHandler : RequestHandler<DynamoDbEvent, BatchEventResponse> {
    override val deserializer get() = DynamoDbEvent.serializer()
    override val serializer get() = BatchEventResponse.serializer()
}

@Serializable
data class DynamoDbEvent(
    @SerialName("Records")
    val records: List<DynamoDbStreamRecord>,
)

@Serializable
data class DynamoDbStreamRecord(
    @SerialName("eventSourceARN")
    val eventSourceArn: String,
    @SerialName("eventID")
    val eventId: String,
    val eventName: EventName,
    val eventVersion: String,
    val eventSource: String,
    val awsRegion: String,
    @SerialName("dynamodb")
    val dynamoDb: StreamRecord,
    val userIdentity: Identity? = null,
)

@Serializable
data class StreamRecord(
    @SerialName("ApproximateCreationDateTime")
    val approximateCreationDateTime: InstantAsEpochSeconds,
    @SerialName("Keys")
    val keys: Map<String, AttributeValue>,
    @SerialName("NewImage")
    val newImage: Map<String, AttributeValue>? = null,
    @SerialName("OldImage")
    val oldImage: Map<String, AttributeValue>? = null,
    @SerialName("SequenceNumber")
    val sequenceNumber: String,
    @SerialName("SizeBytes")
    val sizeBytes: Long,
    @SerialName("StreamViewType")
    val streamViewType: StreamViewType,
)

@Serializable
enum class EventName {
    INSERT,
    MODIFY,
    REMOVE,
}

@Serializable
enum class StreamViewType {
    KEYS_ONLY,
    NEW_IMAGE,
    NEW_AND_OLD_IMAGES,
    OLD_IMAGE,
}

@Serializable
data class AttributeValue(
    @SerialName("S")
    val s: String? = null,
    @SerialName("N")
    val n: String? = null,
    @SerialName("B")
    val b: ByteArray? = null,
    @SerialName("BOOL")
    val bool: Boolean? = null,
    @SerialName("SS")
    val ss: List<String>? = null,
    @SerialName("NS")
    val ns: List<String>? = null,
    @SerialName("BS")
    val bs: List<ByteArray>? = null,
    @SerialName("M")
    val m: Map<String, AttributeValue>? = null,
    @SerialName("L")
    val l: List<AttributeValue>? = null,
    @SerialName("NULL")
    val nullValue: Boolean? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttributeValue

        if (bool != other.bool) return false
        if (nullValue != other.nullValue) return false
        if (s != other.s) return false
        if (n != other.n) return false
        if (!b.contentEquals(other.b)) return false
        if (ss != other.ss) return false
        if (ns != other.ns) return false
        if (bs != other.bs) return false
        if (m != other.m) return false
        if (l != other.l) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bool?.hashCode() ?: 0
        result = 31 * result + (nullValue?.hashCode() ?: 0)
        result = 31 * result + (s?.hashCode() ?: 0)
        result = 31 * result + (n?.hashCode() ?: 0)
        result = 31 * result + (b?.contentHashCode() ?: 0)
        result = 31 * result + (ss?.hashCode() ?: 0)
        result = 31 * result + (ns?.hashCode() ?: 0)
        result = 31 * result + (bs?.hashCode() ?: 0)
        result = 31 * result + (m?.hashCode() ?: 0)
        result = 31 * result + (l?.hashCode() ?: 0)
        return result
    }

}

@Serializable
data class Identity(
    val principalId: String,
    val type: String,
)
