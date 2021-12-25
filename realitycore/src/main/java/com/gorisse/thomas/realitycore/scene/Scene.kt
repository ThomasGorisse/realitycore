package com.gorisse.thomas.realitycore.scene

import android.renderscript.Float3
import com.google.android.filament.Engine
import com.google.android.filament.IndirectLight
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.ar.core.Frame
import com.gorisse.thomas.realitycore.component.HasAnchoring
import com.gorisse.thomas.realitycore.component.Position
import com.gorisse.thomas.realitycore.entity.Entity
import com.gorisse.thomas.realitycore.entity.ModelEntity
import com.gorisse.thomas.realitycore.filament.ShapeResource
import com.gorisse.thomas.realitycore.filament.assetLoader
import com.gorisse.thomas.realitycore.filament.resourceLoader
import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass

typealias Publisher<T> = MutableSharedFlow<T>

/**
 * ### A container that holds the collection of entities rendered by an AR view.
 *
 * You don't create a `Scene` instance directly.
 * Instead, you get the one and only scene associated with a view from the [ARView.Scene] property of an [ARView] instance.
 * ```
 * ARView {
 *      Scene [
 *          AnchorEntity [
 *              Entity [
 *                  Entity,
 *                  Entity
 *              ],
 *              Entity
 *          ],
 *          AnchorEntity [
 *              Entity
 *          ]
 *      ]
 * }
 * ```
 * To add content to the view's scene, you first create and add one or more [AnchorEntity] instances to the [Scene.anchors] collection.
 * Anchors tell RealityCore how to pin virtual content to real world objects, like flat surfaces or images.
 * You then add a hierarchy of other [Entity] instances to each anchor to indicate the geometry and behaviors that RealityCore should render at a given anchor point.
 */
open class Scene(val engine: Engine) {

    /**
     * The Filament Scene. Not overridable cause final and no getEntities() yet so we wrap it.
     */
    //TODO : Waiting for Filament release. See https://github.com/google/filament/issues/3669
    internal val filamentScene: Scene = engine.createScene()

    // region Identifying the Scene

    /**
     * A name for the scene.
     */
    open val name: String get() = ""

    // endregion

    // region Adding and Removing Anchors

    private val anchorsFlow = MutableSharedFlow<List<HasAnchoring>>()

    /**
     * ### The collection of anchors contained in the scene.
     */
    var anchors: List<HasAnchoring> = listOf()
        set(value) {
            (field - value).asEntities.let { oldEntities ->
                oldEntities.forEach { it.scene = null }
                filamentScene.removeEntities(oldEntities.map { it.id }.toIntArray())
            }
            (value - field).asEntities.let { newEntities ->
                newEntities.forEach { it.scene = this }
                filamentScene.addEntities(newEntities.map { it.id }.toIntArray())
            }
            field = value
            anchorsFlow.tryEmit(value)
        }

    /**
     * ### Adds an anchor to the scene's list of anchors.
     *
     * @param anchor The anchor to add.
     */
    fun addAnchor(anchor: HasAnchoring) {
        anchors = anchors + anchor
    }

    /**
     * ### Removes the specified anchor from the scene.
     *
     * @param anchor The anchor to remove.
     */
    fun removeAnchor(anchor: HasAnchoring) {
        anchors = anchors - anchor
    }

    /**
     * ### The collection of entities contained in the scene.
     */
    //TODO : Use the filamentScene.getEntities() when ready.
    // See https://github.com/google/filament/issues/3669
    val entities = anchors.asEntities

    private val List<HasAnchoring>.asEntities get() = this.mapNotNull { it as? Entity }

    // endregion

    // region Finding Entities

    /**
     * ### Searches the scene’s anchor entity hierarchies for an entity with the given name.
     *
     * The [findEntity] method conducts a depth-first, recursive search over all of the scene's entities for one whose name property matches the given name.
     * The method returns the first match. [Entity.name] need not be unique.
     *
     * @param name The name of the entity for which to search.
     *
     * @return The first entity found with the given name, or `null` if none is found.
     */
    fun findEntity(name: String): Entity? =
        anchors.mapNotNull { (it as? Entity)?.findEntity(name) }.firstOrNull()

    // endregion

    // region Detecting Intersections

    /**
     * ### Performs a convex ray cast against all the geometry in the scene for a ray of a given origin, direction, and length.
     *
     * The method ignores entities that lack a [CollisionComponent].
     *
     * @param origin The origin of the ray relative to reference Entity.
     * @param direction The direction of the ray relative to reference Entity.
     * @param length The length of the ray relative to reference Entity.
     * @param query A query type.
     * @param mask A collision mask that you can use to prevent collisions with certain objects.
     * @param relativeTo An entity that defines the frame of reference.
     * The method returns results relative to this entity.
     * Set to null to use the world space origin `(0, 0, 0)`.
     *
     * @return An array of collision cast hit results.
     * Each hit indicates where the ray, starting at a given point and traveling in a given direction, hit a particular entity in the scene.
     *
     * The normal property on returned result objects contains the surface normal at the point of intersection with the entity's collision shape.
     */
    fun raycast(
        origin: Float3,
        direction: Float3,
        length: Float = 100.0f,
        query: CollisionCastQueryType = CollisionCastQueryType.All,
        mask: CollisionGroup = CollisionGroup.All,
        relativeTo: Entity? = null
    ): List<CollisionCastHit> {
        //TODO Collision
        return listOf()
    }

    /**
     * ### Performs a convex ray cast against all the geometry in the scene for a ray between two end points.
     *
     * The method ignores entities that lack a [CollisionComponent].
     *
     * @param fromStartPosition The start position of the ray relative to reference Entity.
     * @param toEndPosition The end position of the ray relative to reference Entity.
     * @param query A query type.
     * @param mask A collision mask that you can use to prevent collisions with certain objects.
     * @param relativeTo An entity that defines the frame of reference.
     * The method returns results relative to this entity.
     * Set to null to use the world space origin `(0, 0, 0)`.
     *
     * @return An array of collision cast hit results.
     * Each hit indicates where the ray, starting at startPosition and ending at endPosition, hit a particular entity in the scene.
     */
    fun raycast(
        fromStartPosition: Position,
        toEndPosition: Position,
        query: CollisionCastQueryType = CollisionCastQueryType.All,
        mask: CollisionGroup = CollisionGroup.All,
        relativeTo: Entity? = null
    ): List<CollisionCastHit> {
        //TODO Collision
        return listOf()
    }

    /**
     * ### Performs a convex shape cast against all the geometry in the scene.
     *
     * An array of collision cast hit results.
     * Each hit indicates where the convex shape, starting at a given point and traveling in a given direction, collides with entities in the scene.
     * To retrieve the hit entity from a returned [CollisionCastHit], use the [CollisionCastHit.entity] property.
     *
     * For objects that intersect the convex shape at its starting position and orientation, the returned collision cast hit result's position is `(0, 0, 0)` and the [CollisionCastHit.normal] points in the opposite direction of the sweep.
     *
     * @param convexShape The convex shape to cast.
     * @param fromPosition The starting position of convexShape relative to reference Entity.
     * @param fromOrientation The starting orientation of convexShape relative to reference Entity.
     * @param toPosition The ending position of convexShape relative to reference Entity.
     * @param toOrientation The ending orientation of convexShape relative to reference Entity.
     * @param query The query type.
     * @param mask A collision mask that you can use to prevent collisions with certain objects.
     * @param relativeTo An entity that defines the frame of reference.
     * The method returns results relative to this entity.
     * Set to null to use the world space origin `(0, 0, 0)`.
     *
     * @return An array of collision cast hit results.
     * Each hit indicates where the convex shape, starting at a given point and traveling in a given direction, collides with entities in the scene.
     * To retrieve the hit entity from a returned [CollisionCastHit], use the [CollisionCastHit.entity] property.
     *
     * For objects that intersect the convex shape at its starting position and orientation, the returned collision cast hit result's [CollisionCastHit.position] is `(0, 0, 0)` and the [CollisionCastHit.normal] points in the opposite direction of the sweep.
     */
    fun convexCast(
        convexShape: ShapeResource,
        fromPosition: Position,
        fromOrientation: Float3,
        toPosition: Position,
        toOrientation: Float3,
        query: CollisionCastQueryType = CollisionCastQueryType.All,
        mask: CollisionGroup = CollisionGroup.All,
        relativeTo: Entity? = null
    ): List<CollisionCastHit> {
        //TODO Collision
        return listOf()
    }

    // endregion

    // region Getting and Sending Events

    /**
     * ### A publisher for the given event type in the scene.
     */
    inline fun <reified T : Event> Publisher(): Publisher<T> = MutableSharedFlow()

    inline fun <reified T : Event> Publisher<T>.subscribe(
        toEvent: KClass<T> = T::class,
        onSourceObject: Any? = null,
        handler: (T) -> Unit
    ) {
        //TODO
    }

    /**
     * ### Events the scene triggers.
     */
    class SceneEvents {
        /**
         * ### An event triggered once per frame interval that you can use to execute custom logic for each frame.
         *
         * @property scene ### The updated scene.
         * @property deltaTime The elapsed time since the last update in milliseconds.
         */
        data class Update(val scene: Scene, val deltaTime: Long) : Event

        /**
         * ### An event triggered when the anchored state of an anchoring entity changes.
         *
         * @property anchor ### The entity whose anchoring state changed.
         * @property isAnchored The current anchoring state of the entity.
         */
        data class AnchoredStateChanged(val anchor: HasAnchoring, val isAnchored: Boolean) : Event
    }
    // endregion

    /**
     * The [Skybox] is drawn last and covers all pixels not touched by geometry.
     * The [Skybox] to use to fill untouched pixels, or `null` to unset the [Skybox].
     */
    var skybox: Skybox? = filamentScene.skybox

    /**
     * The [IndirectLight] to use when rendering the `Scene`.
     *
     * Currently, a `Scene` may only have a single [IndirectLight].
     * This call replaces the current [IndirectLight].
     * The [IndirectLight] to use when rendering the `Scene` or `null` to unset.
     */
    var indirectLight: IndirectLight? = filamentScene.indirectLight

    /**
     * Callback that occurs for each display frame. Updates the scene and reposts itself to be called
     * by the choreographer on the next frame.
     */
    open fun doFrame(frame: Frame) {
        // Allow the resource loader to finalize textures that have become ready.
        resourceLoader.asyncUpdateLoad()

        // Add renderable entities to the scene as they become ready.
        entities.filter { it is ModelEntity }.forEach { it. }?.let { populateScene(it) }
    }
}

/**
 * ### A hit result of a collision cast.
 *
 * You get a collection of collision cast hits from either the [raycast] method, or the [convexCast] method.
 * Each hit indicates where the ray or the convex shape, starting at a given point and traveling in a given direction, hit a particular entity in the scene.
 *
 * The frame of reference for the position and normal of the hit depends on the reference entity parameter passed to the method that generated the hit.
 * Pass null as the reference to use world space.
 *
 * @property entity ### The entity that was hit.
 * @property position ### The position of the hit.
 * The frame of reference for this point depends on the reference entity used in the call to either the [raycast] method or the [convexCast] method that generated the hit.
 * @property normal ### The normal of the hit.
 * The frame of reference for this point depends on the reference entity used in the call to either the [raycast] method or the [convexCast] method that generated the hit.
 * @property distance ### The distance from the ray origin to the hit, or the convex shape travel distance.
 */
data class CollisionCastHit(
    val entity: Entity,
    val position: Position,
    val normal: Float3,
    val distance: Float
)

/**
 * ### The kinds of ray and convex shape cast queries that you can make.
 */
enum class CollisionCastQueryType {
    /**
     * ### Report the closest hit.
     *
     * If you only want to test if a hit occurs and don't care about which hit out of multiple possible hits is returned, use [CollisionCastQueryType.any] instead because it typically executes faster.
     */
    Nearest,

    /**
     * ### Report all hits sorted in ascending order by distance from the cast origin.
     */
    All,

    /**
     * ### Report one hit.
     *
     * This query type typically executes fastest, but doesn't guarantee anything about which hit it returns.
     * If you need the hit closest to the origin of the cast, use [CollisionCastQueryType.nearest] instead.
     */
    Any
}

/**
 * ### A bitmask used to define the collision group to which an entity belongs, and with which it can collide.
 */
enum class CollisionGroup(val values: Set<Int>) {
    /**
     * ### The default collision group for objects.
     */
    Default(0),

    /**
     * ### The collision group that represents all groups.
     */
    All(Default.values);

    constructor(value: Int) : this(setOf(value))
}