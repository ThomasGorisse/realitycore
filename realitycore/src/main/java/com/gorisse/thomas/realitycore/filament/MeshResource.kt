package com.gorisse.thomas.realitycore.filament

import androidx.annotation.IntRange
import com.google.android.filament.Entity
import com.google.android.filament.RenderableManager
import com.gorisse.thomas.realitycore.entity.FilamentEntity

typealias MeshInstance = FilamentEntity

/**
 * ### A high-level representation of a collection of vertices and edges that define a shape.
 */
open class MeshResource(
    internal var builder: RenderableManager.Builder
) {
    var instances = mutableSetOf<MeshInstance>()

    @Entity
    fun createInstance(): MeshInstance = entityManager.create()
        .apply { builder.build(filamentEngine, this) }
        .also {
            instances.add(it)
        }

    class Builder(@IntRange(from = 1) count: Int = 1) : RenderableManager.Builder(count)
}

fun RenderableManager.Builder.build(): MeshResource {
    return MeshResource(this)
        .also { Filament.meshs.add(it) }
}