package com.gorisse.thomas.realitycore.ar

import com.google.android.filament.utils.Float2
import com.google.ar.core.Anchor
import com.gorisse.thomas.realitycore.component.Transform

object ARCore {

    /**
     * ### A description of how virtual content can be anchored to the real world.
     *
     * @constructor Creates an anchoring component for a given target.
     * @property target The kind of real world object to which the anchor entity should anchor.
     */
    data class AnchoringComponent(val target: Target) {

        // region Creating the Anchor Component

        var anchor: Target.Anchor? = null

        /**
         * ### Creates an anchoring component with the given AR anchor.
         *
         * @param anchor An existing AR anchor to use.
         */
        constructor(anchor: Target.Anchor) : this(Target.Anchor(anchor.cloudAnchorId)) {
            this.anchor = anchor
        }

        fun clone() = if (anchor != null) AnchoringComponent(anchor!!) else AnchoringComponent(target)

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
}
