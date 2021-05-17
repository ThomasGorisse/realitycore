package com.gorisse.thomas.realitycore.filament

import com.google.android.filament.Scene
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.FilamentInstance
import com.gorisse.thomas.realitycore.utils.extensionProperty

internal var FilamentAsset.cachedInstances: MutableList<FilamentInstance>? by extensionProperty(
    mutableListOf()
) { instances ->
    instances?.forEach { it.asset = this }
}

/**
 * The current in use instances of an asset created from [createInstance]
 */
val FilamentAsset.instances: MutableList<FilamentInstance> by extensionProperty(mutableListOf())

/**
 * Get a new instance of an asset.
 *
 * Populate instances whose resources are shared with the primary asset.
 */
fun FilamentAsset.createInstance(): FilamentInstance? {
    val instance: FilamentInstance? = cachedInstances?.firstOrNull().also {
        cachedInstances?.remove(it)
    } ?: assetLoader.createInstance(this)
    instance?.let { instances.add(it) }
    return instance
}

internal fun FilamentAsset.populateScene(scene: Scene) {
    var count = 0
    val readyRenderables = IntArray(128)
    val popRenderables = { count = popRenderables(readyRenderables); count != 0 }
    while (popRenderables()) {
        scene.addEntities(readyRenderables.take(count).toIntArray())
    }
    scene.addEntities(lightEntities)
}