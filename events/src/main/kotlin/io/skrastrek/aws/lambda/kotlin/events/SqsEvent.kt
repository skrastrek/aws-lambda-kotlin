package io.skrastrek.aws.lambda.kotlin.events

import io.skrastrek.aws.lambda.kotlin.core.RequestHandler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface SqsEventRequestHandler : RequestHandler<SqsEvent, BatchEventResponse> {
    override val deserializer get() = SqsEvent.serializer()
    override val serializer get() = BatchEventResponse.serializer()
}

@Serializable
data class SqsEvent(
    @SerialName("Records")
    val records: List<SqsRecord>,
)

@Serializable
data class SqsRecord(
    val messageId: String,
    val receiptHandle: String,
    val body: String,
    val md5OfBody: String,
    val attributes: Attributes,
    val messageAttributes: MessageAttributes = emptyMap(),
    val eventSource: String,
    @SerialName("eventSourceARN")
    val eventSourceArn: String,
    val awsRegion: String,
) : BatchEventEntry {
    override val itemIdentifier = messageId
}

@Serializable
data class Attributes(
    @SerialName("ApproximateReceiveCount")
    val approximateReceiveCount: Int,
    @SerialName("SentTimestamp")
    val sentTimestamp: InstantAsEpochSeconds,
    @SerialName("SequenceNumber")
    val sequenceNumber: String? = null,
    @SerialName("MessageGroupId")
    val messageGroupId: String? = null,
    @SerialName("MessageDeduplicationId")
    val messageDeduplicationId: String? = null,
    @SerialName("SenderId")
    val senderId: String,
    @SerialName("ApproximateFirstReceiveTimestamp")
    val approximateFirstReceiveTimestamp: InstantAsEpochSeconds,
)

typealias MessageAttributes = Map<String, MessageAttribute>

@Serializable
data class MessageAttribute(
    val stringValue: String? = null,
    val stringListValues: List<String>? = null,
    val binaryListValues: List<String>? = null,
    val dataType: String,
)
