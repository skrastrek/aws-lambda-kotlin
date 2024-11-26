package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.serialization.builtins.serializer

interface EventHandler<I : Any> : RequestHandler<I, Unit> {
    override val serializer get() = Unit.serializer()
}
