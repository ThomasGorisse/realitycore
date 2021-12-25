package com.gorisse.thomas.realitycore.entity

import android.content.res.AssetManager
import com.google.android.filament.Material
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.FilamentInstance
import com.google.ar.core.Frame
import com.gorisse.thomas.realitycore.component.*
import com.gorisse.thomas.realitycore.filament.*
import com.gorisse.thomas.realitycore.utils.useBuffer
import java.nio.Buffer

/**
 * ## A representation of a physical object that RealityCore renders and optionally simulates.
 *
 * Use one or more model entities to place physical objects in a scene.
 * In addition to the components they inherit from the [Entity] class, model entities have geometry, described by their [com.gorisse.thomas.realitycore.filament.Filament.ModelComponent].
 * Model entities acquire the model component by implementing the [com.gorisse.thomas.realitycore.component.HasModel] interface.
 * You specify meshes and materials to control how a model entity appears.
 * ```
 * ModelEntity(
 *      transformComponent: HasTransform,
 *      synchronizationComponent: HasSynchronization,
 *      modelComponent: HasComponent,
 *      collisionComponent: HasCollision,
 *      physicsBodyComponent: HasPhysicsBody,
 *      physicsMotionComponent : HasPhysicsMotion)
 * ```
 * Models respond to physics simulations because they conform to the [HasPhysics] protocol.
 * You give them mass and other physical properties with a [PhysicsBodyComponent] instance, and then apply forces or impulses.
 * The simulator uses a [PhysicsMotionComponent] to manage the linear and angular velocity of the object.
 * Alternatively, you can selectively circumvent the simulation to control position and velocity yourself.
 * Do this for a given model by setting its physics body [PhysicsBodyComponent.mode] to [PhysicsBodyMode.kinematic].
 *
 * Models can also collide with one another, and with other entities that implement the [HasCollision] interface.
 * The [CollisionComponent] provides parameters that let you manage which models collide with each other.
 * It also lets you control the collision shape, which for performance reasons, is typically simpler than the visual geometry.
 *
 * @constructor Creates a model entity.
 * @property id The stable identity of the entity associated with this instance.
 * @property modelComponent The model component for the model entity.
 */
class ModelEntity(
    id: FilamentEntity = entityManager.create(),
    transformComponent: HasTransform = Filament.TransformComponent(id),
    synchronizationComponent: HasSynchronization = SynchronizationComponent(id),
    val modelComponent: HasModel = Filament.ModelComponent(id),
    val collisionComponent: HasCollision = Filament.CollisionComponent(id)
) : Entity(id, transformComponent, synchronizationComponent), HasModel by modelComponent,
    HasCollision by collisionComponent {

    /**
     * ### Creates a model entity with a particular mesh and set of materials.
     *
     * @param mesh A mesh that defines the geometry of the model.
     * @param materials Material resources that define the appearance of the model.
     */
    constructor(
        mesh: MeshResource,
        materials: List<Material> = listOf()
    ) : this(id = mesh.createInstance()) {
        this.materials = materials.map { it.createInstance() }
    }

    /**
     * ### Creates a model entity with a particular mesh, set of materials, a composite collision shape, and mass.
     *
     * @param mesh A mesh that defines the geometry of the model.
     * @param materials Material resources that define the appearance of the model.
     * @param collisionShapes A collection of shape resources that define a composite collision shape.
     * @param mass The mass of the model in kilograms.
     */
    constructor(
        mesh: MeshResource,
        materials: List<Material> = listOf(),
        collisionShapes: List<ShapeResource>,
        mass: Float
    ) : this(mesh, materials) {
        //TODO
    }

    /**
     * ### Creates a model entity with a particular mesh, set of materials, a composite collision shape, and mass.
     *
     * @param mesh A mesh that defines the geometry of the model.
     * @param materials Material resources that define the appearance of the model.
     * @param collisionShape A collection of shape resources that define a composite collision shape.
     * @param mass The mass of the model in kilograms.
     */
    constructor(
        mesh: MeshResource,
        materials: List<Material> = listOf(),
        collisionShape: ShapeResource,
        mass: Float
    ) : this(mesh, materials, listOf(collisionShape), mass)

    internal fun doFrame(frame: Frame) {
    }
}

/**
 * ### Blocks your app while loading an entity from a file in the assets.
 *
 * @param assets Provides access to the application's raw asset files
 * @param fileName The base name with extension of the glb file to load from the assets folder.
 * @return The root entity in the loaded file, cast as a ModelEntity.
 */
@Throws(Exception::class)
fun Entity.Companion.loadModel(assets: AssetManager, fileName: String) : ModelEntity {
    val asset = assets.open(fileName).useBuffer { buffer ->
        loadModelGlb(buffer)
    }
    val instance = asset!!.createInstance()!!
    return ModelEntity(instance.root, modelComponent = Filament.ModelComponent(instance))
}
