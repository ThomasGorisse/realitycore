package com.gorisse.thomas.realitycore.entity

import com.gorisse.thomas.realitycore.filament.filamentEngine

typealias FilamentEntityInstance = Int

internal val Int.Companion.NULL get() = com.google.android.filament.EntityInstance.NULL

val FilamentEntityInstance?.safeNull : Int get() = this ?: com.google.android.filament.EntityInstance.NULL

fun FilamentEntityInstance.destroy() {
    val t : FilamentEntityInstance? = null
    t!!
    filamentEngine.destroyEntity(this)
}