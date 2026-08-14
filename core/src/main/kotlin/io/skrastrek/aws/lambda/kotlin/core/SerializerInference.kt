package io.skrastrek.aws.lambda.kotlin.core

import kotlinx.serialization.KSerializer
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
 */
object SerializerInference {
    private val cache = ConcurrentHashMap<Key, List<KSerializer<Any>>>()

    /**
     * Serializers for the type arguments [handler] passes to [handlerInterface], in declaration
     * order. Reflection runs once per handler class; later calls are a map lookup.
     */
    fun serializersOf(
        handler: Any,
        handlerInterface: Class<*>,
    ): List<KSerializer<Any>> =
        cache.computeIfAbsent(Key(handler.javaClass, handlerInterface)) { (implementation, target) ->
            typeArgumentsOf(implementation, target).map { argument ->
                serializer(argument.concreteOrFail(implementation, target))
            }
        }

    private data class Key(
        val implementation: Class<*>,
        val handlerInterface: Class<*>,
    )

    /** The arguments [implementation] passes to [target], resolved through any intermediate types. */
    private fun typeArgumentsOf(
        implementation: Class<*>,
        target: Class<*>,
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
            if (raw == target) return arguments

            val inherited: Map<TypeVariable<*>, Type> =
                raw.typeParameters
                    .zip(arguments) { parameter, argument -> (parameter as TypeVariable<*>) to argument }
                    .toMap()
            return (listOfNotNull(raw.genericSuperclass) + raw.genericInterfaces)
                .firstNotNullOfOrNull { search(it, inherited) }
        }

        return search(implementation, emptyMap())
            ?: error("${implementation.name} does not implement ${target.name}.")
    }

    /** Replaces type variables with what the subtype bound them to, as far as the bindings reach. */
    private fun Type.substitute(bindings: Map<TypeVariable<*>, Type>): Type =
        when (this) {
            is TypeVariable<*> -> bindings[this]?.substitute(bindings) ?: this
            is WildcardType -> upperBounds.firstOrNull()?.substitute(bindings) ?: this
            else -> this
        }

    private fun Type.concreteOrFail(
        implementation: Class<*>,
        target: Class<*>,
    ): Type =
        also {
            if (it is TypeVariable<*>) {
                error(
                    "${implementation.name} leaves ${target.simpleName}'s type argument '${it.name}' generic, " +
                        "so its serializer cannot be inferred. Override the serializers explicitly, or build the " +
                        "handler through the ${target.simpleName} { ... } factory.",
                )
            }
        }
}
