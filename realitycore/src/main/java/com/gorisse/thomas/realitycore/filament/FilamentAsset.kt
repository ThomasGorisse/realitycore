package com.gorisse.thomas.realitycore.filament

import com.google.android.filament.Scene
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.FilamentInstance
import com.gorisse.thomas.realitycore.utils.extensionProperty
import java.nio.Buffer

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

/**
 * Consumes the contents of a glTF 2.0 file and produces a primary asset with one or more
 * instances.
 *
 * The given instance array must be sized to the desired number of instances. If successful,
 * this method will populate the array with secondary instances whose resources are shared with
 * the primary asset.
 */
fun loadModelGlb(buffer: Buffer, asyncLoadResources: Boolean = false): FilamentAsset? {
    val instances = arrayOfNulls<FilamentInstance>(Filament.CACHED_INSTANCES_SIZE)
    return assetLoader.createInstancedAsset(buffer, instances)?.also { asset ->
        if (asyncLoadResources) {
            resourceLoader.asyncBeginLoad(asset)
        } else {
            resourceLoader.loadResources(asset)
            resourceLoader.asyncUpdateLoad()
        }
        asset.cachedInstances = instances.filterNotNull().toMutableList()
        Filament.assets.add(asset)
    }
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