package io.skrastrek.aws.lambda.kotlin.coroutines

import io.skrastrek.aws.lambda.kotlin.core.NodeLambdaContext
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeHandlerTest {
    @Test
    fun `suspending handler resolves the promise with the parsed result`() =
        runTest {
            val context = js("({ awsRequestId: 'req-1' })").unsafeCast<NodeLambdaContext>()

            val result = CapitalizeRequestHandler.handleNode("hello world", context).await()

            assertEquals("HELLO WORLD", result)
        }
}
