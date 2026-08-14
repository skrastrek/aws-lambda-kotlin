package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.concurrent.ConcurrentHashMap

/**
 * Derives serializers from the type arguments a handler class fills in for a handler interface.
 *
 * This is what lets `RequestHandler<Foo, Bar>` be implemented without restating `Foo.serializer()`
 * and `Bar.serializer()`: a class that names its type arguments records them in its generic
 * signature, so they can be read back at runtime and handed to `kotlinx.serialization`.
 *
 * The arguments must be concrete at the implementing class. `class Handler<T> : RequestHandler<T,
 * Unit>` erases `T` and cannot be inferred — such a handler has to override the serializers, or be
 * built through one of the `RequestHandler { ... }` factories, whose reified type parameters resolve
 * at the call site instead.
 *
 * ### GraalVM
 *
 * Reading the generic signature works in a native image unaided, but finding a class's generated
 * serializer is reflective and needs registration. `RequestHandlerFeature`, shipped in this artifact
 * and picked up automatically by `native-image`, registers it for every handler it finds reachable.
 * Where that is not enough — a handler the analysis cannot see, or type arguments this cannot infer
 * — register the serializer as contextual instead and no reflection is involved at all:
 *
 * ```kotlin
 * override val json = Json(io.skrastrek.aws.lambda.kotlin.core.json) {
 *     serializersModule = SerializersModule { contextual(SqsEvent::class, SqsEvent.serializer()) }
 * }
 * ```
 */
object SerializerInference {
    private val cache = ConcurrentHashMap<Key, List<KSerializer<Any>>>()

    /**
     * Serializers for the type arguments [implementation] passes to [handlerInterface], in
     * declaration order. [serializersModule] is consulted for contextual serializers before falling
     * back to reflection. Resolution runs once per handler class; later calls are a map lookup.
     */
    fun serializersOf(
        implementation: Class<*>,
        handlerInterface: Class<*>,
        serializersModule: SerializersModule,
    ): List<KSerializer<Any>> =
        cache.computeIfAbsent(Key(implementation, handlerInterface, serializersModule)) { key ->
            typeArgumentsOf(key.implementation, key.handlerInterface).map { argument ->
                key.serializersModule.serializer(argument.concreteOrFail(key.implementation, key.handlerInterface))
            }
        }

    /**
     * The type arguments [implementation] passes to [handlerInterface], resolved through any
     * intermediate types. Exposed for `RequestHandlerFeature`, which needs the types at image build
     * time without instantiating the handler.
     */
    fun typeArgumentsOf(
        implementation: Class<*>,
        handlerInterface: Class<*>,
    ): List<Type> {
        fun search(
            type: Type,
            bindings: Map<TypeVariable<*>, Type>,
        ): List<Type>? {
            val raw: Class<*>
            val arguments: List<Type>
            when (type) {
                is Class<*> -> {
                    raw = type
                    arguments = emptyList()
                }

                is ParameterizedType -> {
                    raw = type.rawType as? Class<*> ?: return null
                    arguments = type.actualTypeArguments.map { it.substitute(bindings) }
                }

                else -> {
                    return null
                }
            }
            if (raw == handlerInterface) return arguments

            val inherited: Map<TypeVariable<*>, Type> =
                raw.typeParameters
                    .zip(arguments) { parameter, argument -> (parameter as TypeVariable<*>) to argument }
                    .toMap()
            return (listOfNotNull(raw.genericSuperclass) + raw.genericInterfaces)
                .firstNotNullOfOrNull { search(it, inherited) }
        }

        return search(implementation, emptyMap())
            ?: error("${implementation.name} does not implement ${handlerInterface.name}.")
    }

    private data class Key(
        val implementation: Class<*>,
        val handlerInterface: Class<*>,
        val serializersModule: SerializersModule,
    )

    /** Replaces type variables with what the subtype bound them to, as far as the bindings reach. */
    private fun Type.substitute(bindings: Map<TypeVariable<*>, Type>): Type =
        when (this) {
            is TypeVariable<*> -> bindings[this]?.substitute(bindings) ?: this
            is WildcardType -> upperBounds.firstOrNull()?.substitute(bindings) ?: this
            else -> this
        }

    private fun Type.concreteOrFail(
        implementation: Class<*>,
        handlerInterface: Class<*>,
    ): Type =
        also {
            if (it is TypeVariable<*>) {
                error(
                    "${implementation.name} leaves ${handlerInterface.simpleName}'s type argument '${it.name}' " +
                        "generic, so its serializer cannot be inferred. Override the serializers explicitly, or " +
                        "build the handler through the ${handlerInterface.simpleName} { ... } factory.",
                )
            }
        }
}
