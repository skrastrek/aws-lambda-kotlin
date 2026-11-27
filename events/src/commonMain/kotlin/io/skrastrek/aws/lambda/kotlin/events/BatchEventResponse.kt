package io.skrastrek.aws.lambda.kotlin.events

import kotlinx.serialization.Serializable

@Serializable
data class BatchEventResponse(
    val batchItemFailures: List<BatchItemFailure>,
)

@Serializable
data class BatchItemFailure(
    val itemIdentifier: String,
)
