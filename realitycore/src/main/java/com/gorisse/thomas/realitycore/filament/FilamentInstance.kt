package com.gorisse.thomas.realitycore.filament

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.text.TextUtils
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.FilamentInstance
import com.google.android.filament.utils.*
import com.google.ar.core.Frame
import com.google.ar.sceneform.animation.ModelAnimation
import com.google.ar.sceneform.animation.ModelAnimator
import com.gorisse.thomas.realitycore.entity.FilamentEntityInstance
import com.gorisse.thomas.realitycore.utils.extensionProperty
import java.util.*

private val kDefaultObjectPosition = Float3(0.0f, 0.0f, -4.0f)

/**
 * Get the original [FilamentAsset] which created this instance
 */
var FilamentInstance.asset: FilamentAsset? by extensionProperty(null)

/**
 * Gets a temporary handle that can be used to access the renderable state.
 */
internal val FilamentInstance.renderableInstance: FilamentEntityInstance
    get() = renderableManager.getInstance(root)


/**
 * Sets up a root transform on the current model to make it fit into a unit cube.
 *
 * @param centerPoint Coordinate of center point of unit cube, defaults to < 0, 0, -4 >
 */
fun FilamentInstance.transformToUnitCube(centerPoint: Float3 = kDefaultObjectPosition) {
    asset?.let { asset ->
        var center = asset.boundingBox.center.let { v -> Float3(v[0], v[1], v[2]) }
        val halfExtent = asset.boundingBox.halfExtent.let { v -> Float3(v[0], v[1], v[2]) }
        val maxExtent = 2.0f * max(halfExtent)
        val scaleFactor = 2.0f / maxExtent
        center -= centerPoint / scaleFactor
        val transform = scale(Float3(scaleFactor)) * translation(-center)
        this.transform = transpose(transform).toFloatArray()
    }
}

fun FilamentInstance.doFrame(frame: Frame) {
    updateAnimations(false)
    animator.updateBoneMatrices()
}

/**
 * Apply animations changes `if fore==true` or the animation has dirty values.
 *
 * @param force Update even if the animation time position didn't changed.
 * @return true if any animation update has been made.
 */
fun FilamentInstance.updateAnimations(force: Boolean): Boolean {
    var hasUpdate = false
    for (i in 0 until getAnimationCount()) {
        val animation = getAnimation(i)
        if (force || animation.isDirty()) {
            animator.applyAnimation(i, animation.getTimePosition())
            hasUpdate = true
        }
    }
    return hasUpdate
}

//Animations

val FilamentInstance.animations: MutableList<ModelAnimation> by extensionProperty(
    mutableListOf()
)

/**
 * Get the associated [ModelAnimation] at the given index or throw
 * an [IndexOutOfBoundsException].
 *
 * @param animationIndex Zero-based index for the animation of interest.
 */
fun FilamentInstance.getAnimation(animationIndex: Int) = animations[animationIndex]

/**
 * Returns the number of [ModelAnimation] definitions in the model.
 */
fun FilamentInstance.getAnimationCount(): Int = animations.size

/**
 * Called form the [ModelAnimation] when it dirty state changed.
 */
fun FilamentInstance.onModelAnimationChanged(animation: ModelAnimation) {
    if (applyAnimationChange(animation)) {
        animation.setDirty(false)
    }
}

/**
 * Occurs when a [ModelAnimation] has received any property changed.
 * <br></br>Depending on the returned value, the [ModelAnimation] will set his isDirty to false
 * or not.
 * <br></br>You can choose between applying changes on the [ObjectAnimator]
 * [android.view.Choreographer.FrameCallback] or use your own
 * [android.view.Choreographer] to handle an update/render update hierarchy.
 * <br></br>Time position should be applied inside a global [android.view.Choreographer] frame
 * callback to ensure that the transformations are applied in a hierarchical order.
 *
 * @return true is the changes have been applied/handled
 */
fun FilamentInstance.applyAnimationChange(animation: ModelAnimation): Boolean {
    return false
}

/**
 * Get the associated [ModelAnimation] by name or null if none exist with the given name.
 */
fun FilamentInstance.getAnimation(name: String): ModelAnimation? {
    val index = getAnimationIndex(name)
    return if (index != -1) getAnimation(index) else null
}

/**
 * Get the Zero-based index for the animation name of interest or -1 if not found.
 */
fun FilamentInstance.getAnimationIndex(name: String): Int {
    for (i in 0 until getAnimationCount()) {
        if (TextUtils.equals(getAnimation(i).name, name)) {
            return i
        }
    }
    return -1
}

/**
 * Get the name of the [ModelAnimation] at the Zero-based index
 *
 *
 * This name corresponds to the one defined and exported in the renderable asset.
 * Typically the Action names defined in the 3D creation software.
 *
 *
 * @return The string name of the [ModelAnimation],
 * `String.valueOf(animation.getIndex())`> if none was specified.
 */
fun FilamentInstance.getAnimationName(animationIndex: Int): String {
    return getAnimation(animationIndex).name
}

/**
 * Get the names of the [ModelAnimation]
 *
 *
 * This names correspond to the ones defined and exported in the renderable asset.
 * Typically the Action names defined in the 3D creation software.
 *
 *
 * @return The string name of the [ModelAnimation],
 * `String.valueOf(animation.getIndex())`> if none was specified.
 */
fun FilamentInstance.getAnimationNames(): List<String> {
    val names: MutableList<String> = ArrayList()
    for (i in 0 until getAnimationCount()) {
        names.add(getAnimation(i).name)
    }
    return names
}

/**
 * Return true if [.getAnimationCount] > 0
 */
fun FilamentInstance.hasAnimations(): Boolean {
    return getAnimationCount() > 0
}

/**
 * Sets the current position of (seeks) the animation to the specified time position in seconds.
 * This time should be
 *
 *
 * This method will apply rotation, translation, and scale to the Renderable that have been
 * targeted. Uses `TransformManager`
 *
 *
 * @param timePosition Elapsed time of interest in seconds.
 * Between 0 and the max value of [ModelAnimation.getDuration].
 * @see ModelAnimation.getDuration
 */
fun FilamentInstance.setAnimationsTimePosition(timePosition: Float) {
    for (i in 0 until getAnimationCount()) {
        getAnimation(i).setTimePosition(timePosition)
    }
}

/**
 * Sets the current position of (seeks) all the animations to the specified frame number according
 * to the [ModelAnimation.getFrameRate]
 *
 *
 * This method will apply rotation, translation, and scale to the Renderable that have been
 * targeted. Uses `TransformManager`
 *
 * @param framePosition Frame number on the timeline.
 * Between 0 and [ModelAnimation.getFrameCount].
 * @see ModelAnimation.getFrameCount
 */
fun FilamentInstance.setAnimationsFramePosition(framePosition: Int) {
    for (i in 0 until getAnimationCount()) {
        getAnimation(i).setFramePosition(framePosition)
    }
}

/**
 * Constructs and returns an [ObjectAnimator] for all [ModelAnimation]
 * of this object.
 * <h3>Don't forget to call [ObjectAnimator.start]</h3>
 *
 * @param repeat repeat/loop the animation
 * @return The constructed ObjectAnimator
 * @see ModelAnimator.ofAnimationTime
 */
fun FilamentInstance.animate(repeat: Boolean): ObjectAnimator {
    val animator: ObjectAnimator = ModelAnimator.ofAllAnimations(this)
    if (repeat) {
        animator.repeatCount = ValueAnimator.INFINITE
    }
    return animator
}

/**
 * Constructs and returns an [ObjectAnimator] for targeted [ModelAnimation] with a
 * given name of this object.
 * <br></br>**Don't forget to call [ObjectAnimator.start]**
 *
 * @param animationNames The string names of the animations.
 * <br></br>This name should correspond to the one defined and exported in
 * the model.
 * <br></br>Typically the action name defined in the 3D creation software.
 * [ModelAnimation.getName]
 * @return The constructed ObjectAnimator
 * @see ModelAnimator.ofAnimationTime
 */
fun FilamentInstance.animate(vararg animationNames: String): ObjectAnimator {
    return ModelAnimator.ofAnimation(this, *animationNames)
}

/**
 * Constructs and returns an [ObjectAnimator] for targeted [ModelAnimation] with a
 * a given index of this object.
 * <br></br>**Don't forget to call [ObjectAnimator.start]**
 *
 * @param animationIndexes Zero-based indexes for the animations of interest.
 * @return The constructed ObjectAnimator
 * @see ModelAnimator.ofAnimationTime
 */
fun FilamentInstance.animate(vararg animationIndexes: Int): ObjectAnimator {
    return ModelAnimator.ofAnimation(this, *animationIndexes)
}

/**
 * Constructs and returns an [ObjectAnimator] for a targeted [ModelAnimation] of
 * this object.
 * <br></br>This method applies by default this to the returned ObjectAnimator :
 *
 *  * The duration value to the max [ModelAnimation.getDuration] in order to
 * match the original animation speed.
 *  * The interpolator to [LinearInterpolator] in order to match the natural animation
 * interpolation.
 *
 * <br></br>**Don't forget to call [ObjectAnimator.start]**
 *
 * @param animations The animations of interest
 * @return The constructed ObjectAnimator
 * @see ModelAnimator.ofAnimationTime
 */
fun FilamentInstance.animate(vararg animations: ModelAnimation): ObjectAnimator {
    return ModelAnimator.ofAnimation(this, *animations)
}