package com.gorisse.thomas.realitycore.component

import com.google.android.filament.MaterialInstance
import com.google.android.filament.utils.Mat4
import com.gorisse.thomas.realitycore.utils.of

/**
 * ### An interface that provides meshes and materials to define the visual appearance of an entity.
 */
interface HasModel {

    /**
     * ### Changes whether or not the renderable casts shadows.
     */
    var isShadowCaster: Boolean

    /**
     * ### Changes whether or not the renderable can receive shadows.
     */
    var isShadowReceiver: Boolean

    /**
     * ### The unique material used by the model.
     */
    var material: MaterialInstance?
        get() = materials.firstOrNull()
        set(value) {
            materials = listOfNotNull(value)
        }

    /**
     * ### The materials used by the model.
     */
    var materials: List<MaterialInstance>
}