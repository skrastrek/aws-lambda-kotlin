package io.skrastrek.aws.lambda.kotlin.core.graalvm

import io.skrastrek.aws.lambda.kotlin.core.SerializerInference
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeReflection
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Makes inferred serializers work in a GraalVM native image.
 *
 * A handler's type arguments can be read from its generic signature in a native image unaided, but
 * `kotlinx.serialization` then finds the generated serializer for those types reflectively — through
 * `Companion` fields and `$serializer` classes the analysis has no reason to keep. This registers
 * them for every handler the analysis finds reachable, so no consumer configuration is needed.
 *
 * `native-image` discovers this class through `META-INF/native-image/.../native-image.properties` in
 * this artifact. It runs only at image build time; the GraalVM SDK it compiles against is a
 * `compileOnly` dependency and is absent at runtime.
 */
class RequestHandlerFeature : Feature {
    override fun getDescription() = "Registers serializer lookup metadata for aws-lambda-kotlin request handlers"

    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        HANDLER_INTERFACES
            .mapNotNull { access.findClassByName(it) }
            .forEach { handlerInterface ->
                access.registerSubtypeReachabilityHandler(
                    { _, implementation -> register(implementation, handlerInterface) },
                    handlerInterface,
                )
            }
    }

    private fun register(
        implementation: Class<*>,
        handlerInterface: Class<*>,
    ) {
        if (implementation.isInterface || Modifier.isAbstract(implementation.modifiers)) return

        // A handler whose arguments cannot be inferred is not an error here: it either declares its
        // serializers explicitly, or fails at runtime with a message of its own.
        val arguments =
            runCatching { SerializerInference.typeArgumentsOf(implementation, handlerInterface) }
                .getOrElse { return }

        arguments.forEach { registerSerializerLookup(it) }
    }

    private fun registerSerializerLookup(type: Type) {
        when (type) {
            is Class<*> -> {
                registerSerializableClass(type)
            }

            is ParameterizedType -> {
                registerSerializerLookup(type.rawType)
                type.actualTypeArguments.forEach { registerSerializerLookup(it) }
            }

            else -> {
                Unit
            }
        }
    }

    /**
     * Registers the shapes `kotlinx.serialization` probes for: the `Companion` field on the class,
     * the `serializer` method on the companion, and the `INSTANCE` field of a generated
     * `$serializer` or a serializable object.
     *
     * Members are registered individually rather than only through the `registerAllDeclared*`
     * queries: those make a member visible to `getDeclaredMethods`, but invoking one that was never
     * registered itself still fails.
     */
    private fun registerSerializableClass(type: Class<*>) {
        RuntimeReflection.register(type)
        RuntimeReflection.registerAllDeclaredFields(type)
        RuntimeReflection.registerAllDeclaredClasses(type)
        RuntimeReflection.register(*type.declaredFields)

        type.declaredClasses.forEach { nested ->
            RuntimeReflection.register(nested)
            RuntimeReflection.registerAllDeclaredFields(nested)
            RuntimeReflection.registerAllDeclaredMethods(nested)
            RuntimeReflection.register(*nested.declaredFields)
            RuntimeReflection.register(*nested.declaredMethods)
        }
    }

    private companion object {
        val HANDLER_INTERFACES =
            listOf(
                "io.skrastrek.aws.lambda.kotlin.core.RequestHandler",
                "io.skrastrek.aws.lambda.kotlin.coroutines.SuspendingRequestHandler",
            )
    }
}
