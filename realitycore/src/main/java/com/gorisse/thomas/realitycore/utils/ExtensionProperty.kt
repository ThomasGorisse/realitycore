package com.gorisse.thomas.realitycore.utils

import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified T:Any, reified V> extensionProperty(
    defaultValue: V,
    noinline onSet: T.(V) -> Unit = {}
) = SynchronizedFieldProperty<T, V>(defaultValue, onSet)

//inline fun <reified T, reified V : Any> extensionNullableProperty(
//    defaultValue: V?,
//    noinline onSet: T.(V?) -> Unit = {}
//) = SynchronizedNullableFieldProperty(defaultValue, onSet)

/**
 * Provides property delegation which behaves as if each [R] instance had a backing field of type [T] for that property.
 * Delegation can be defined at top level or inside a class, which will mean that the delegation is scoped to
 * instances of the class -- separate instances will see separate values of the delegated property.
 *
 * This implementation is thread-safe.
 *
 * This delegate does not allow `null` values, use [SynchronizedNullableFieldProperty] for a nullable equivalent.
 *
 * If the delegated property of an [R] instance is accessed but has not been initialized, [initializer] is called to
 * provide the initial value. The default [initializer] throws [IllegalStateException].
 */
class SynchronizedFieldProperty<T, V>(
    private val defaultValue: V,
    val onSet: T.(V) -> Unit = {}
) : ReadWriteProperty<T, V> {
    private val map = WeakHashMap<T, V>()

    override fun getValue(thisRef: T, property: KProperty<*>): V = synchronized(map) {
        map.getOrPut(thisRef, { defaultValue })
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        synchronized(map) {
            map[thisRef] = value
            onSet(thisRef, value)
        }
    }
}


/**
 * Provides property delegation which behaves as if each [R] instance had a backing field of type [T] for that property.
 * Delegation can be defined at top level or inside a class, which will mean that the delegation is scoped to
 * instances of the class -- separate instances will see separate values of the delegated property.
 *
 * This implementation is thread-safe.
 *
 * This delegate allows `null` values.
 *
 * If the delegated property of an [R] instance is accessed but has not been initialized, [initializer] is called to
 * provide the initial value. The default [initializer] returns `null`.
 */
class SynchronizedNullableFieldProperty<T, V : Any>(
    private val defaultValue: V?,
    val onSet: T.(V?) -> Unit = {}
) : ReadWriteProperty<T, V?> {
    private val map = WeakHashMap<T, V>()

    override fun getValue(thisRef: T, property: KProperty<*>): V? = synchronized(map) {
        map.getOrPut(thisRef, { defaultValue })
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V?) {
        synchronized(map) {
            map[thisRef] = value
            onSet(thisRef, value)
        }
    }
}