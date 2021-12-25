package com.gorisse.thomas.realitycore.component

import com.gorisse.thomas.realitycore.filament.ShapeResource
import com.gorisse.thomas.realitycore.scene.CollisionGroup

/**
 * ### A component that gives an entity the ability to collide with other entities that also have collision components.
 *
 * @constructor Creates a collision component with the given collision shape, mode, and filter parameters.
 * @property shapes The collection of shapes that collectively define the outer dimensions of the associated entity for the purposes of collision detection.
 * @property mode The mode of the collision component.
 * @property filter A filter that limits the other entities with which the entity can collide.
 */
data class Collision(
    var shapes: List<ShapeResource>,
    var mode: Mode = Mode.Default,
    var filter: CollisionFilter = CollisionFilter.Default
) {

    /**
     * ### A mode that dictates how much collision data is collected for a given entity.
     */
    enum class Mode {
        /**
         * ### A default collision object.
         *
         * When two objects of this type collide, RealityCore computes the full contact details (contact points, normal vectors, penetration depths, and so on) and stores them in the contact set.
         */
        Default,

        /**
         * ### A trigger collision object.
         *
         * When a collision object of this type collides with any other object, RealityCore records that contact was made, but discards other details, like contact points, normal vectors, and so on.
         * This makes a trigger object more performant when all you need is a Boolean indicator that contact occurred.
         */
        Trigger
    }
}

/**
 * ### An interface used for ray casting and collision detection.
 */
interface HasCollision {

    /**
     * ### The collision component for an entity.
     */
    var collision: Collision?
}

/**
 * ### A set of masks that determine whether entities can collide during simulations.
 *
 * @constructor Creates a collision filter.
 * @property group The collision group or groups, stored as a bit mask, to which the entity belongs.
 * @property mask The collision group or groups, stored as a bitmask, with which the entity can collide.
 */
enum class CollisionFilter(var group: CollisionGroup, var mask: CollisionGroup) {
    /**
     * ### The default collision filter.
     *
     * Entities with a `default` collision filter have a [group] of [CollisionGroup.Default] and a [mask] of [CollisionGroup.All].
     */
    Default(CollisionGroup.Default, CollisionGroup.All),

    /**
     * ### A collision filter for an entity that collides with everything.
     *
     * The sensor collision filter is typically used by rays in ray casts, shapes in convex shape casts, and trigger volumes. It corresponds to a [group] and [mask] both set to [CollisionGroup.All].
     */
    Sensor(CollisionGroup.All, CollisionGroup.All)
}