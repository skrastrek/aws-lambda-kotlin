package io.skrastrek.aws.lambda.kotlin.events

interface BatchEventEntry {
    val itemIdentifier: String
}

fun BatchEventEntry.batchItemFailure() = BatchItemFailure(itemIdentifier)
