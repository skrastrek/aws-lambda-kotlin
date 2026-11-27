package io.skrastrek.aws.lambda.kotlin.coroutines

import com.amazonaws.services.lambda.runtime.Context
import io.skrastrek.aws.lambda.kotlin.core.defaultJson
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals

class AwsBridgeTest {
    @Test
    fun `blocking bridge runs the suspending handler`() {
        val input = ByteArrayInputStream(defaultJson.encodeToString("hello world").toByteArray())
        val output = ByteArrayOutputStream()

        // The AWS-facing entry point: this is what the managed Java runtime invokes.
        CapitalizeRequestHandler.handleRequest(input, output, awsContext)

        assertEquals(defaultJson.encodeToString("HELLO WORLD"), output.toString(Charsets.UTF_8))
    }
}

private val awsContext =
    Proxy.newProxyInstance(Context::class.java.classLoader, arrayOf(Context::class.java)) { _, _, _ -> null } as Context
