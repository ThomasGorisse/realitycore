package com.gorisse.thomas.realitycore.entity

import android.content.res.AssetManager
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.FilamentInstance
import com.gorisse.thomas.realitycore.scene.Scene
import com.gorisse.thomas.realitycore.component.*
import com.gorisse.thomas.realitycore.filament.*
import com.gorisse.thomas.realitycore.utils.useBuffer
import getWorldTransform
import setParent
import java.nio.Buffer


/**
 * A type of unique identifier used for an entity.
 */
typealias FilamentEntity = Int

/**
 * ## An element of a RealityCore scene to which you attach components that provide appearance and
 * behavior characteristics for the entity.
 *
 * You create and configure entities to embody objects that you want to place in the real world in
 * an AR app. You do this by adding `Entity` to the [Scene] instance associated with an
 * [com.gorisse.thomas.realitycore.ARView].
 *
 * RealityCore defines a few concrete subclasses of `Entity` that provide commonly used
 * functionality.
 * For example, you typically start by creating an instance of [AnchorEntity] to anchor your
 * content, and add the anchor to a scene's anchors collection. You might then instantiate a
 * ModelEntity to represent a physical object in the scene, and add that as a child entity to the
 * anchor. You can also create custom entities, and add them either to an anchor, or as children of
 * another entity.
 *
 * @constructor Creates a new entity.
 * @property id The stable identity of the entity associated with this instance.
 */
open class Entity(
    val id: FilamentEntity = entityManager.create(),
    transformComponent: HasTransform = Filament.TransformComponent(id),
    synchronizationComponent: HasSynchronization = SynchronizationComponent(id)
) : HasTransform by transformComponent, HasHierarchy,
    HasSynchronization by synchronizationComponent {

    // region Creating an Entity

    /**
     * ### Duplicates an entity to create a new entity.
     *
     * All component data is cloned automatically.
     * If you clone an entity that stores custom data that's not part of a component, override the [didClone] method to copy that data manually.
     *
     * @param recursive A Boolean that you set to true to recursively copy all the children of the entity.
     * Otherwise, no descendants are copied.
     * @return The duplicate.
     */
    open fun clone(recursive: Boolean): Entity {
        val entity = Entity(
            entityManager.create(),
            transformComponent = Filament.TransformComponent(id).apply {

            })
        entity.didClone(this)
        return entity
    }

    /**
     * ### Tells a newly cloned entity that cloning is complete.
     *
     * This method clones all component data automatically.
     * When you clone an entity that stores custom data that's not part of a component, override the [didClone] method to copy that data manually after the clone finishes.
     *
     * @param source The entity from which the cloned entity was copied.
     */
    open fun didClone(source: Entity) {
    }

    // endregion

    // region Identifying an Entity

    /**
     * ### The scene that owns the entity.
     *
     * An entity belongs to a scene if the entity is part of a hierarchy that's rooted in the scene's [Scene.anchors] collection.
     *
     * The value of the property is `null` if the entity isn't currently attached to any scene.
     */
    var scene: Scene? = null
        internal set

    /**
     * ### The name of the entity.
     *
     * You can find an entity by name in a scene by calling the scene's [findEntity] method.
     * Or you can recursively search among the children of a given entity by calling the entity's [findEntity] method.
     *
     * Entity names are not guaranteed to be unique.
     * When you search by name, these methods return the first entity encountered with the given name.
     */
    var name: String = id.toString()

    /**
     * ### Recursively searches all descendant entities for one with the given name.
     *
     * The [findEntity] method conducts a depth-first, recursive search over all of the entity's descendants for one whose name property matches the given [name].
     * The method returns the first match. Entity names need not be unique.
     *
     * @param name The entity name for which to search.
     * @return An entity with the given name, or `null` if no entity is found.
     */
    fun findEntity(name: String): Entity? =
        children.mapNotNull { it.findEntity(name) }.firstOrNull() ?: takeIf { this.name == name }

    // endregion

    // region Getting State

    /**
     * ### A Boolean that you set to enable or disable the entity and its descendants.
     *
     * Set this value to `true` to enable the entity.
     * Unless an ancestor is disabled, the entity and all of its enabled descendants, up to the first that's disabled, report [isEnabledInHierarchy] of `true`.
     * If an ancestor is disabled, they all report `false`. The state of [isActive] for enabled entities is `true` if they are anchored, or `false` otherwise.
     *
     * If you disable an entity, it and all of its descendants become both disabled ([isEnabledInHierarchy] returns `false`) and inactive ([isActive] returns `false`), regardless of any other state.
     */
    var isEnabled = true
        set(value) {
            //TODO
        }

    /**
     * ### A Boolean that indicates whether the entity and all of its ancestors are enabled.
     *
     * The value of this property is `true` if the entity and all of its ancestors are enabled, regardless of anchor state.
     */
    val isEnabledInHierarchy: Boolean
        get() = isEnabled && parent?.isEnabledInHierarchy ?: true

    /**
     * ### A Boolean that indicates whether the entity is active.
     *
     * The value of this property is `true` if the entity is anchored in a scene, and it and all of its ancestors are enabled ([isEnabled] is set to `true`).
     * RealityCore doesn't simulate or render inactive entities.
     */
    val isActive: Boolean
        get() = isAnchored && isEnabledInHierarchy

    /**
     * ### A Boolean that indicates whether the entity is anchored.
     *
     * The value of this property is `true` if the entity is anchored in a scene.
     * An entity that isn't anchored becomes inactive ([isActive] returns `false`), meaning RealityCore doesn't render or simulate it.
     */
    val isAnchored: Boolean
        get() = this.scene != null

    // endregion

    // region Establishing a Hierarchy

    //TODO : Integrate Filament transformManager.getParent when ready
    // see https://github.com/google/filament/commit/fe94f935f106a7a8a899a3a44830be8d76e161ce
    final override var parent: Entity? = null
        private set(value) {
            field?.let { it.children -= this }
            field = value
            value?.let { it.children += this }
            transformManager.setParent(this, value)
        }

    override fun setParent(parent: Entity?, preservingWorldTransform: Boolean) {
        if (preservingWorldTransform) {
            this.transform = (this.transform relativeTo this.parent) relativeFrom parent
        }
        this.parent = parent
    }

    override fun removeFromParent(preservingWorldTransform: Boolean) {
        setParent(null, preservingWorldTransform)
    }

    override var children: List<Entity> = listOf()
    override fun addChild(entity: Entity, preservingWorldTransform: Boolean) {
        entity.setParent(this, preservingWorldTransform)
    }

    override fun removeChild(entity: Entity, preservingWorldTransform: Boolean) {
        if (entity.parent == this) {
            entity.removeFromParent(preservingWorldTransform)
        }
    }

    // endregion

    // region Positioning Entities in Space

    override val worldTransform: Transform
        get() = transformManager.getWorldTransform(this)

    // endregion

    // region Finding the Nearest Anchor

    /**
     * ### The nearest ancestor entity that can act as an anchor.
     *
     * This property returns null if no ancestor can act as an anchor.
     * An entity can act as an anchor if it adopts the HasAnchoring protocol.
     * Just because an ancestor can be anchored doesn't mean that it is.
     * Inspect the isAnchored property to see if an entity (or one of its ancestors) is anchored.
     */
    val anchor: HasAnchoring?
        get() = (this as? HasAnchoring)?.takeIf { isAnchored } ?: parent?.anchor

    // endregion

    // region Calculating the Collision Shape

    /**
     * ### Creates the shape used to detect collisions between two entities that have collision components.
     *
     * Call this method on entities that implements the [HasModel] and [HasCollision] interface to prepare a shape used for collision detection.
     * The method stores the shape in the entity's [CollisionComponent] instance.
     *
     * For non-model entities, the method has no effect.
     * Nevertheless, the method is defined for all entities so that you can call it on any entity, and have the calculation propagate recursively to all that entity's descendants.
     *
     * @param recursive A Boolean that you set to `true` to also generate the collision shapes for all descendants of the entity.
     */
    fun generateCollisionShapes(recursive: Boolean) {
        //TODO see : https://github.com/google/filament/discussions/3827#discussion-3329344
    }

    // endregion

    // region Playing Animation

    //TODO
//    /**
//     * ### The list of animations associated with the entity.
//     *
//     * When you import an entity from a file, for example by using the [load] method, the entity might contain associated animations.
//     * Any that RealityCore supports appear in the `availableAnimations` array.
//     *
//     * To play a particular animation resource from the list, call the [playAnimation] method.
//     * Alternatively, to play all animations with a given name, call the [playAnimation] method instead.
//     */
//    val availableAnimations = listOf<AnimationResource>()

    // endregion

    companion object {
    }
}

/**
 * ### An interface that provides access to a parent entity and child entities.
 *
 * All entities automatically adopt this protocol because the [Entity] base class does.
 * This adoption gives all entities a collection of methods for managing the hierarchy.
 */
interface HasHierarchy {

    // region Managing the Parent

    /***
     * ### The parent entity.
     *
     * An entity has at most one parent entity.
     * If an entity isn't part of a hierarchy, or if it is a root entity, the parent property is null.
     * Use the [setParent] method to change an entity's parent.
     * Use the [removeFromParent] method to remove the parent.
     * These methods automatically update the corresponding children collections of the new and old parent.
     */
    val parent: Entity?

    /**
     * ### Attaches the entity as a child to the specified entity.
     *
     * Attaching an entity to a new parent automatically detaches it from its old parent.
     *
     * The [children] collections of both the old and new parent are automatically updated as well.
     *
     * @param parent The new parent entity. Use null to detach the entity from its current parent.
     * @param preservingWorldTransform A Boolean that you set to `true` to preserve the entity's world transform, or `false` to preserve its relative transform.
     * Use `true` when you want a model to keep its effective location and size within a scene.
     */
    fun setParent(parent: Entity?, preservingWorldTransform: Boolean = false)

    /**
     * ### Removes the entity from its current parent or from the scene if it is a root entity.
     *
     * This method behaves like the [setParent] method with a value of `null` for the `parent` parameter, except that method has no effect on root entities.
     * A root entity is one that is stored in a scene’s [anchors] collection.
     *
     * The [children] collections of any modified parent entities are automatically updated as well.
     *
     * @param preservingWorldTransform A Boolean that you set to `true` to preserve the entity's world transform, or `false` to preserve its relative transform.
     * Use `true` when you want a model to keep its effective location and size within a scene.
     */
    fun removeFromParent(preservingWorldTransform: Boolean = false)

    // endregion

    // region Managing Children

    /**
     * ### The child entities that the entity manages.
     *
     * An entity can have any number of child entities.
     *
     * Use the [addChild] method to add a child to an entity.
     * Use the [removeChild] method to remove one from an entity.
     * These methods automatically update the [parent] properties of the child entities.
     */
    var children: List<Entity>

    /**
     * ### Adds the given entity to the collection of child entities.
     *
     * The child’s [parent] property is automatically updated.
     *
     * @param entity The child entity to add.
     * @param preservingWorldTransform A Boolean that you set to `true` to preserve the entity's world transform, or `false` to preserve its relative transform.
     * Use `true` when you want a model to keep its effective location and size within a scene.
     */
    fun addChild(entity: Entity, preservingWorldTransform: Boolean = false)

    /**
     * ### Removes the given child from the entity.
     *
     * The child's [parent] property is automatically updated.
     *
     * @param entity The child entity to remove.
     * @param preservingWorldTransform A Boolean that you set to `true` to preserve the entity's world transform, or `false` to preserve its relative transform.
     * Use `true` when you want a model to keep its effective location and size within a scene.
     */
    fun removeChild(entity: Entity, preservingWorldTransform: Boolean = false)

    // endregion

    /**
     * ### The world transform (i.e. relative to the root) of a transform component.
     *
     * This is the composition of this component's local transform with its parent's world transform.
     * @see [getWorldTransform]
     */
    val worldTransform: Transform
}

//TODO
class SynchronizationComponent(
    private val entityId: FilamentEntity
) : HasSynchronization

