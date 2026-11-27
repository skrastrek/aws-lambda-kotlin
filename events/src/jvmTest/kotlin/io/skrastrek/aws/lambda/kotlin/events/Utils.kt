@file:OptIn(ExperimentalSerializationApi::class)

package io.skrastrek.aws.lambda.kotlin.events

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

class Utils

inline fun <reified T> Json.decodeFromResource(resource: String): T =
    decodeFromStream(Utils::class.java.classLoader.getResourceAsStream(resource)!!)
