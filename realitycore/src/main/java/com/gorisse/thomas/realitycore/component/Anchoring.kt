package com.gorisse.thomas.realitycore.component

import com.google.android.filament.utils.Float2
import com.google.ar.core.Anchor

/**
 * ### A description of how virtual content can be anchored to the real world.
 *
 * @constructor Creates an anchoring component for a given target.
 * @property target The kind of real world object to which the anchor entity should anchor.
 */
data class AnchoringComponent(val target: Target) {

    // region Creating the Anchor Component

    var anchor: Anchor? = null

    /**
     * ### Creates an anchoring component with the given AR anchor.
     *
     * @param anchor An existing AR anchor to use.
     */
    constructor(anchor: Anchor) : this(Target.Anchor(anchor.cloudAnchorId)) {
        this.anchor = anchor
    }

    // endregion

    // region Setting a Target

    sealed class Target {

        /**
         * ### The alignment of real-world surfaces to seek as targets.
         */
        enum class Alignment {
            /** Horizontal surfaces. */
            horizontal,

            /** Vertical surfaces. */
            vertical,

            /** Surfaces of any alignment. */
            any
        }

        /**
         * ### Types of real-world surfaces to seek as targets.
         */
        enum class Classification {
            /** Look for walls. */
            wall,

            /** Look for floors. */
            floor,

            /** Look for ceilings. */
            ceiling,

            /** Look for tables. */
            table,

            /** Look for seats. */
            seat,

            /** Look for any kind of surface. */
            any,
        }

        /**
         * ### The camera.
         */
        object Camera : Target()

        /**
         * ### A fixed position in the scene.
         */
        data class World(val transform: Transform) : Target()

        /**
         * ### The AR anchor with a given identifier.
         */
        data class Anchor(val cloudAnchorId: String) : Target()

        /**
         * ### A surface.
         */
        data class Plane(
            val alignment: Alignment,
            val classification: Classification,
            val minimumBounds: Float2
        ) : Target()

        /**
         * ### An image.
         */
        data class Image(val group: String, val name: String) : Target()

        /**
         * ### A specific object.
         */
        data class Object(val group: String, val name: String) : Target()

        /**
         * ### The user's face.
         */
        object Face : Target()

        /**
         * ### The user's body.
         */
        object Body : Target()
    }

    // endregion
}

/**
 * ### An interface that enables anchoring of virtual content to a real-world object in an AR scene.
 */
interface HasAnchoring {

    // region Getting the Component

    /**
     * ### The component that describes how the virtual content is anchored to the real world.
     */
    var anchoring: AnchoringComponent

    /**
     * ### The identifier of the AR anchor to which the entity is anchored, or `null` if it isn't currently anchored.
     */
    val anchorIdentifier: String? get() = anchoring.anchor?.cloudAnchorId

    /**
     * ### Changes the entity’s anchoring, preserving either the world transform or the local transform.
     *
     * @param target Describes how the entity should be anchored in AR.
     * @param preservingWorldTransform A Boolean you set to `true` to preserve the current world space position, or `false` to use the position relative to the previous anchor for the new anchor.
     */
    fun reanchor(target: AnchoringComponent.Target, preservingWorldTransform: Boolean = true) {
        //TODO
    }
}

