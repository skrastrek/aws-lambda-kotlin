package io.skrastrek.aws.lambda.kotlin.events

import kotlin.collections.getValue

typealias DynamoDbAttributes = Map<String, AttributeValue>

fun DynamoDbAttributes.boolean(attributeName: String): Boolean = value(attributeName).bool!!

fun DynamoDbAttributes.string(attributeName: String): String = value(attributeName).s!!

fun DynamoDbAttributes.map(attributeName: String): DynamoDbAttributes = value(attributeName).m!!

fun DynamoDbAttributes.nullableString(attributeName: String): String? = nullableValue(attributeName)?.s

fun DynamoDbAttributes.nullableMap(attributeName: String): DynamoDbAttributes? = nullableValue(attributeName)?.m

private fun DynamoDbAttributes.value(attributeName: String): AttributeValue = getValue(attributeName)

private fun DynamoDbAttributes.nullableValue(attributeName: String): AttributeValue? = get(attributeName)
