package io.skrastrek.aws.lambda.kotlin.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/** Where blocking IO runs: `Dispatchers.IO` where it exists, the event loop on JS. */
internal expect val ioDispatcher: CoroutineDispatcher
