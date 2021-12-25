package com.gorisse.thomas.realitycore.filament

import android.opengl.EGLContext
import android.view.Display
import com.google.android.filament.Box
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Material
import com.google.android.filament.filamat.MaterialBuilder
import com.google.android.filament.gltfio.*
import com.google.android.filament.utils.Utils
import com.gorisse.thomas.realitycore.component.*
import com.gorisse.thomas.realitycore.entity.*
import com.gorisse.thomas.realitycore.utils.OpenGL
import getTransform
import setTransform

object Filament {

    const val MIN_OPENGL_VERSION = 3.0f
    internal const val CACHED_INSTANCES_SIZE = 50

    private var referenceCount = 0

    lateinit var eglContext: EGLContext
    lateinit var engine: Engine
    lateinit var assetLoader: AssetLoader
    lateinit var resourceLoader: ResourceLoader

    internal val meshs = mutableSetOf<MeshResource>()
    internal val assets = mutableSetOf<FilamentAsset>()
    internal val materials = mutableSetOf<Material>()

    fun retain(): Engine {
        if (referenceCount == 0) {
            engine = create()
        }
        referenceCount++
        return engine
    }

    fun release() {
        referenceCount--
        if (referenceCount == 0) {
            destroy()
        }
    }

    fun create(
        normalizeSkinningWeights: Boolean = true,
        recomputeBoundingBoxes: Boolean = false
    ): Engine {
        eglContext = OpenGL.createEglContext()
        Utils.init()
        Gltfio.init()
        MaterialBuilder.init()
        val engine = Engine.create(eglContext)
        assetLoader =
            AssetLoader(engine, MaterialProvider(engine), entityManager)
        resourceLoader =
            ResourceLoader(engine, normalizeSkinningWeights, recomputeBoundingBoxes)
        return engine
    }

    fun destroy() {
        meshs.flatMap { it.instances }.forEach { engine.destroyEntity(it) }
        assets.forEach {
            it.releaseSourceData()
            assetLoader.destroyAsset(it)
            it.instances.forEach { instance -> engine.destroyEntity(instance.root) }
        }
        materials.forEach { engine.destroyMaterial(it) }

        MaterialBuilder.shutdown()
        engine.destroy()
        assetLoader.destroy()
        resourceLoader.asyncCancelLoad()
        resourceLoader.destroy()
        OpenGL.destroyEglContext(eglContext)
    }

    // region Positioning Entities in Space

    /**
     * ### Manipulate the scale, rotation, and position of the entity.
     *
     * @constructor All of these methods and properties come from implementation of the [HasTransform] interface.
     */
    class TransformComponent(
        @com.google.android.filament.Entity
        private val entity: FilamentEntity
    ) : HasTransform {

        val transformInstance @com.google.android.filament.EntityInstance get() = entity.transformInstance

        override var transform: Transform
            get() = transformManager.getTransform(transformInstance)
            set(value) = transformManager.setTransform(transformInstance, value)

        fun clone(@com.google.android.filament.Entity entity: FilamentEntity) : TransformComponent {
            return TransformComponent(entity).apply {
                this.transform = this@TransformComponent.transform
            }
        }
    }

    // endregion

    // region Configuring the Model

    /**
     * ### The model component for the model entity.
     */
    class ModelComponent(val asset: FilamentAsset) : HasModel {

        override val assetInstance : FilamentInstance = asset.createInstance()!!

        @com.google.android.filament.Entity
        private val entity: FilamentEntity = assetInstance.root

        val renderableInstance @com.google.android.filament.EntityInstance get() = entity.renderableInstance

        override var isShadowCaster
            get() = renderableManager.isShadowCaster(renderableInstance)
            set(value) = renderableManager.setCastShadows(renderableInstance, value)

        override var isShadowReceiver
            get() = renderableManager.isShadowReceiver(renderableInstance)
            set(value) = renderableManager.setReceiveShadows(renderableInstance, value)

        override var materials
            get() = (0..renderableManager.getPrimitiveCount(renderableInstance)).map {
                renderableManager.getMaterialInstanceAt(renderableInstance, it)
            }
            set(value) = value.forEachIndexed { index, material ->
                renderableManager.setMaterialInstanceAt(renderableInstance, index, material)
            }

        fun clone() : ModelComponent {
            return ModelComponent(asset).apply {
                this.isShadowCaster = this@ModelComponent.isShadowCaster
                this.isShadowReceiver = this@ModelComponent.isShadowReceiver
                this.materials = this@ModelComponent.materials.map { it.material.createInstance() }
            }
        }
    }

    // endregion

    // region Detecting Collisions

    /**
     * ### A component that gives an entity the ability to collide with other entities that also have collision components.
     */
    class CollisionComponent(
        @com.google.android.filament.Entity
        private val entity: FilamentEntity
    ) : HasCollision {

        val renderableInstance @com.google.android.filament.EntityInstance get() = entity.renderableInstance

        override var collision: Collision?
            get() = Collision(listOf(ShapeResource(Box().apply {
                renderableManager.getAxisAlignedBoundingBox(
                    renderableInstance,
                    this
                )
            })))
            set(value) {
                value?.shapes?.firstOrNull()?.let { shape ->
                    renderableManager.setAxisAlignedBoundingBox(
                        renderableInstance,
                        Box(
                            shape.center.x,
                            shape.center.y,
                            shape.center.z,
                            shape.halfExtent.x,
                            shape.halfExtent.y,
                            shape.halfExtent.z
                        )
                    )
                }
            }
    }

    // endregion
}

/**
 * Get the Filament Engine instance, creating it if necessary.
 *
 * @throws IllegalStateException
 */
val filamentEngine get() = Filament.engine

val assetLoader get() = Filament.assetLoader

val entityManager get() = EntityManager.get()

/**
 * The [com.google.android.filament.TransformManager] used by the [Engine]
 */
val transformManager get() = filamentEngine.transformManager

val resourceLoader get() = Filament.resourceLoader

/**
 * The [com.google.android.filament.LightManager] used by the [Engine]
 */
val lightManager get() = filamentEngine.lightManager

/**
 * ### Returns whether a particular [Entity] is associated with a component of this [TransformManager]
 *
 * @param entity an [Entity]
 * @return true if this [Entity] has a component associated with this manager
 */
val FilamentEntity.hasTransform: Boolean
    get() = transformManager.hasComponent(this)

/**
 * ### Gets an [FilamentEntityInstance] representing the transform component associated with the given [FilamentEntity].
 *
 * @param entity a [FilamentEntity]
 * @return a [FilamentEntityInstance], which represents the transform component associated with the [FilamentEntity]
 * @see [hasTransform]
 */
val FilamentEntity.transformInstance: FilamentEntityInstance
    @com.google.android.filament.EntityInstance
    get() = if (hasTransform) transformManager.getInstance(this) else FilamentEntity.NULL

/**
 * The [com.google.android.filament.RenderableManager] used by the [Engine]
 */
val renderableManager get() = filamentEngine.renderableManager

/**
 * Checks if the given entity already has a renderable component.
 */
val FilamentEntity.hasRenderable: Boolean
    get() = renderableManager.hasComponent(this)

/**
 * Gets a temporary handle that can be used to access the renderable state.
 */
val FilamentEntity.renderableInstance: FilamentEntityInstance
    @com.google.android.filament.EntityInstance
    get() = if (hasRenderable) renderableManager.getInstance(this) else FilamentEntity.NULL