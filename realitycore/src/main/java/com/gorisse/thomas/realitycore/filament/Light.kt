package com.gorisse.thomas.realitycore.filament

import com.google.android.filament.LightManager
import com.gorisse.thomas.realitycore.entity.Entity

open class Light(val entity: Entity) {
    val instance = lightManager.getInstance(entity)

    /**
     * Creates a light builder and set the light's {@link Type}.
     *
     * @param type [LightManager.Type] of Light object to create.
     */
    class Builder(type: LightManager.Type) : LightManager.Builder(type) {
        init {
            // Directional lights should have a different default intensity
            intensity(if (type == LightManager.Type.DIRECTIONAL) DEFAULT_DIRECTIONAL_INTENSITY else 2500.0f)
            castShadows(false)
            position(0.0f, 0.0f, 0.0f)
            direction(0.0f, 0.0f, -1.0f)
            color(1.0f, 1.0f, 1.0f)
            falloff(10.0f)
            spotLightCone(0.5f, 0.6f)
        }

        companion object {
            const val DEFAULT_DIRECTIONAL_INTENSITY = 420.0f
        }
    }
}

/**
 * Adds the Light component to an entity.
 *
 * <p>
 * If this component already exists on the given entity, it is first destroyed as if
 * {@link #destroy} was called.
 * </p>
 *
 * <b>warning:</b>
 * Currently, only 2048 lights can be created on a given Engine.
 *
 * @param engine Reference to the {@link Engine} to associate this light with.
 * @param entity Entity to add the light component to.
 */
fun LightManager.Builder.build(): Light {
    val entity = entityManager.create()
    build(filamentEngine, entity)
    return Light(entity)
        .also { ResourceManager.handleLight(it) }
}

fun Light.destroy() {
    ResourceManager.destroyLight(this)
}
