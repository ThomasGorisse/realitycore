package com.gorisse.thomas.realitycore.component

import com.gorisse.thomas.realitycore.ar.ARCore

/**
 * ### An interface that enables anchoring of virtual content to a real-world object in an AR scene.
 */
interface HasAnchoring {

    // region Getting the Component

    /**
     * ### The component that describes how the virtual content is anchored to the real world.
     */
    var anchoring: ARCore.AnchoringComponent

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
    fun reanchor(target: ARCore.AnchoringComponent.Target, preservingWorldTransform: Boolean = true) {
        //TODO
    }
}

