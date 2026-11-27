package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeHandlerTest {
    @Test
    fun `parsed event in, parsed result out`() {
        val event = js("({ name: 'world' })")

        val result: dynamic = GreetingHandler.handleNode(event, nodeContext)

        assertEquals("hello world from req-1 with 256 MB", result.greeting)
    }
}

internal val nodeContext: NodeLambdaContext =
    js("({ awsRequestId: 'req-1', memoryLimitInMB: '256', getRemainingTimeInMillis: function() { return 1000; } })")

@Serializable
private data class Greet(
    val name: String,
)

@Serializable
private data class Greeting(
    val greeting: String,
)

private object GreetingHandler : RequestHandler<Greet, Greeting> {
    override val deserializer get() = Greet.serializer()
    override val serializer get() = Greeting.serializer()

    override fun handle(
        input: Greet,
        context: Context,
    ) = Greeting("hello ${input.name} from ${context.awsRequestId} with ${context.memoryLimitInMB} MB")
}
