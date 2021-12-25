import com.google.android.filament.EntityInstance
import com.google.android.filament.TransformManager
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.transpose
import com.gorisse.thomas.realitycore.component.Transform
import com.gorisse.thomas.realitycore.entity.Entity
import com.gorisse.thomas.realitycore.entity.FilamentEntityInstance
import com.gorisse.thomas.realitycore.filament.transformInstance

/**
 * ### Returns the local transform of a transform component.
 *
 * @param entityInstance the [FilamentEntityInstance] of the transform component to query the local transform from.
 * @return the local transform of the component (i.e. relative to the parent). This always
 * returns the value set by setTransform().
 * @see [setTransform]
 */
fun TransformManager.getTransform(@com.google.android.filament.EntityInstance entityInstance: FilamentEntityInstance): Transform =
    Transform(Mat4.of(*getTransform(entityInstance, FloatArray(16))))

/**
 * ### Sets a local transform of a transform component.
 *
 * This operation can be slow if the hierarchy of transform is too deep, and this will be particularly bad when updating a lot of transforms.
 * In that case, consider using [TransformManager.openLocalTransformTransaction] / [TransformManager.commitLocalTransformTransaction].
 *
 * @param entityInstance the [FilamentEntityInstance] the [FilamentEntityInstance] of the transform component to set the local transform to.
 * @param localTransform the local transform (i.e. relative to the parent).
 * @see [getTransform]
 */
fun TransformManager.setTransform(
    @com.google.android.filament.EntityInstance entityInstance: FilamentEntityInstance,
    localTransform: Transform
) = setTransform(entityInstance, transpose(localTransform.matrix).toFloatArray())


/**
 * ### Re-parents an entity to a new one.
 *
 * @param entity The [Entity] of the transform component to re-parent
 * @param newParent The [Entity] of the new parent transform.
 * It is an error to re-parent an entity to a descendant and will cause
 * undefined behaviour.
 */
fun TransformManager.setParent(entity: Entity, newParent: Entity?) =
    setParent(entity.id.transformInstance, newParent?.id?.transformInstance ?: EntityInstance.NULL)

/**
 * ### Returns the world transform of a transform component.
 *
 * @param entity the [Entity] of the transform component to query the world transform from.
 * @return The world transform of the component (i.e. relative to the root).
 * This is the composition of this component's local transform with its parent's world transform.
 */
fun TransformManager.getWorldTransform(entity: Entity): Transform =
    Transform(Mat4.of(*getWorldTransform(entity.id.transformInstance, FloatArray(16))))