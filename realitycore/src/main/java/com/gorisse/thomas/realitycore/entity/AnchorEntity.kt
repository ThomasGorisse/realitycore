package com.gorisse.thomas.realitycore.entity

import com.google.android.filament.utils.Float2
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.gorisse.thomas.realitycore.ar.ARCore
import com.gorisse.thomas.realitycore.component.*
import com.gorisse.thomas.realitycore.filament.Filament
import com.gorisse.thomas.realitycore.filament.entityManager

/**
 * ### An anchor that tethers virtual content to a real-world object in an AR session.
 *
 * You use an `AnchorEntity` instance as the root of an entity hierarchy, and add it to the [Scene.anchors] collection for a [Scene] instance.
 * This enables ARCore to place the anchor entity, along with all of its hierarchical descendants, into the real world.
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
 * In addition to the components the anchor entity inherits from the [Entity] class, the anchor entity also conforms to the [HasAnchoring] protocol, giving it an [AnchoringComponent] instance.
 * ```
 * AnchorEntity(
 *      transformComponent: HasTransform,
 *      synchronizationComponent: HasSynchronization,
 *      anchoringComponent: HasAnchoring)
 * ```
 * While you have only one anchor at the root of a given hierarchy of entities, you can have multiple anchors in the scene, each with its own hierarchy of entities.
 * For example, you could have one anchor that places a toy car on a horizontal surface, like a table, and another that ties informative text bubbles to an image, all in the same scene.
 *
 * @constructor Creates a new anchor entity.
 */
class AnchorEntity(
    id: FilamentEntity = entityManager.create(),
    transformComponent: HasTransform = Filament.TransformComponent(id),
    synchronizationComponent: HasSynchronization = SynchronizationComponent(id),
    override var anchoring: ARCore.AnchoringComponent
) : Entity(id, transformComponent, synchronizationComponent), HasAnchoring {

    // region Creating an Anchor

    /**
     * ### Creates an anchor entity targeting a particular kind of anchor.
     *
     * @param target The real world object the anchor should target.
     */
    constructor(target: ARCore.AnchoringComponent.Target) : this(anchoring = ARCore.AnchoringComponent(target))

    /**
     * ### Creates an anchor entity that uses an existing AR anchor.
     *
     * @param anchor An existing ARAnchor instance.
     */
    constructor(anchor: Anchor) : this(anchoring = ARCore.AnchoringComponent(anchor))

    /**
     * ### Creates an anchor entity that uses an existing AR anchor.
     *
     * @param planeAlignment The alignment of the plane to target, like [AnchoringComponent.Target.Alignment.horizontal] or [AnchoringComponent.Target.Alignment.vertical].
     * @param classification The classification of the target plane to look for, like [AnchoringComponent.Target.Classification.floor] or [AnchoringComponent.Target.Classification.ceiling].
     * @param minimumBounds The minimum size of the target plane.
     */
    constructor(
        planeAlignment: ARCore.AnchoringComponent.Target.Alignment,
        classification: ARCore.AnchoringComponent.Target.Classification = ARCore.AnchoringComponent.Target.Classification.any,
        minimumBounds: Float2 = Float2(0.0f, 0.0f)
    ) : this(
        anchoring = AnchoringComponent(
            target = AnchoringComponent.Target.Plane(
                planeAlignment,
                classification,
                minimumBounds
            )
        )
    )

    /**
     * ### Creates an anchor entity using the information about a real-world surface discovered using a [com.google.ar.core.Frame.hitTest].
     *
     * @param hitResult The hitTest hitResult.
     */
    constructor(hitResult: HitResult) : this(anchor = hitResult.createAnchor())

    /**
     * ### Creates an anchor entity with a target fixed at the given position in the scene.
     *
     * @param worldTransform The transform with which to initialize the world target.
     */
    constructor(worldTransform: Transform) : this(
        anchoring = ARCore.AnchoringComponent(
            target = ARCore.AnchoringComponent.Target.World(
                worldTransform
            )
        )
    )

    /**
     * ### Creates an anchor entity with a target fixed at the given position in the scene.
     *
     * @param worldPosition The position with which to initialize the world target.
     */
    constructor(worldPosition: Position) : this(worldTransform = Transform(position = worldPosition))

    // endregion

    // region Configuring the Anchor

    // endregion
}
