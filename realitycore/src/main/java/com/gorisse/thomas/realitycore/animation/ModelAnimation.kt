package com.google.ar.sceneform.animation

import android.text.TextUtils
import android.util.FloatProperty
import android.util.IntProperty
import android.util.Property
import com.gorisse.thomas.realitycore.entity.ModelEntity
import java.util.concurrent.TimeUnit

/**
 * An ModelAnimation is a reusable set of keyframe tracks which represent an animation.
 *
 *
 * This class provides support for animating time positions on a targeted
 * [AnimatableModel]
 *
 *
 * <h2>Here are some use cases for animations :</h2>
 *
 *  *
 * On a very basic 3D model like a single infinite rotating sphere, you should not have to
 * use this class but probably instead just call
 * [AnimatableModel.animate]
 *
 *  *
 * For a synchronised animation set like animating a cube and a sphere position and rotation at same
 * time or sequentially, please consider using an [android.animation.AnimatorSet] playing a
 * [ModelAnimator.ofAnimation]
 * or [ModelAnimator.ofPropertyValuesHolder]
 *
 *  *
 * If the mesh is a character, for example, there may be one ModelAnimation for a walkcycle, a
 * second for a jump, a third for sidestepping and so on.
 * <br></br>Assuming a character object has a skeleton, one keyframe track could store the data for the
 * position changes of the lower arm bone over time, a different track the data for the rotation
 * changes of the same bone, a third the track position, rotation or scaling of another bone, and so
 * on. It should be clear, that an ModelAnimation can act on lots of such tracks.
 * <br></br>Assuming the model has morph targets (for example one morph target showing a friendly face
 * and another showing an angry face), each track holds the information as to how the influence of a
 * certain morph target changes during the performance of the clip.
 * In this case you should manage one [android.animation.ObjectAnimator] coming from
 * [ModelAnimator.ofAnimation] per action.
 * And an [android.animation.AnimatorSet] to play them sequentially or together.
 *
 *
 */
class ModelAnimation(
    model: ModelEntity, name: String, index: Int, duration: Float, frameRate: Int
) {
    private val model: ModelEntity
    private val index: Int

    /**
     * Get the name of the `animation`
     *
     *
     * This name corresponds to the one defined and exported in the [AnimatableModel].
     * <br></br>Typically the Action names defined in the 3D creation software.
     *
     *
     * @return Weak reference to the string name of the `animation`, or
     * the `String.valueOf(animation.getIndex())`> if none was specified.
     */
    var name: String
    private val duration: Float
    private val frameRate: Int

    /**
     * Time position is applied inside a global [android.view.Choreographer] frame callback
     * to ensure that the transformations are applied in a hierarchical order.
     */
    private var timePosition = 0f
    var _isDirty = false

    /**
     * Returns The Zero-based index of the target `animation` as defined in the original
     * [AnimatableModel]
     */
    fun geIndex(): Int {
        return index
    }

    /**
     * Returns the duration of this animation in seconds.
     */
    fun getDuration(): Float {
        return duration
    }

    /**
     * Returns the duration of this animation in milliseconds.
     */
    fun getDurationMillis(): Long {
        return secondsToMillis(duration)
    }

    /**
     * Get the frames per second originally defined in the
     * [android.graphics.drawable.Animatable].
     *
     * @return The number of frames refresh during one second
     */
    fun getFrameRate(): Int {
        return frameRate
    }

    /**
     * Returns the total number of frames of this animation.
     */
    fun getFrameCount(): Int {
        return timeToFrame(duration, getFrameRate())
    }

    /**
     * Get the current time position in seconds at the current animation position.
     *
     * @return timePosition Elapsed time of interest in seconds. Between 0 and
     * [.getDuration]
     * @see .getDuration
     */
    fun getTimePosition(): Float {
        return timePosition
    }

    /**
     * Sets the current position of (seeks) the animation to the specified time position in seconds.
     *
     *
     * This method will apply rotation, translation, and scale to the [AnimatableModel] that
     * have been targeted.
     *
     *
     * @param timePosition Elapsed time of interest in seconds. Between 0 and
     * [.getDuration]
     * @see .getDuration
     */
    fun setTimePosition(timePosition: Float) {
        this.timePosition = timePosition
        setDirty(true)
    }

    /**
     * Get the current frame number at the current animation position.
     *
     * @return Frame number on the timeline. Between 0 and [ ][.getFrameCount]
     */
    fun getFramePosition(): Int {
        return getFrameAtTime(getTimePosition())
    }

    /**
     * Sets the current position of (seeks) the animation to the specified frame number according to
     * the [.getFrameRate].
     *
     * @param frameNumber Frame number in the timeline. Between 0 and [.getFrameCount]
     * @see .setTimePosition
     * @see .getFrameCount
     */
    fun setFramePosition(frameNumber: Int) {
        setTimePosition(getTimeAtFrame(frameNumber))
    }

    /**
     * Get the fractional value at the current animation position.
     *
     * @return The fractional (percent) position. Between 0 and 1
     * @see .getTimePosition
     */
    fun getFractionPosition(): Float {
        return getFractionAtTime(getTimePosition())
    }

    /**
     * Sets the current position of (seeks) the animation to the specified fraction
     * position.
     *
     * @param fractionPosition The fractional (percent) position. Between 0 and 1.
     * @see .setTimePosition
     */
    fun setFractionPosition(fractionPosition: Float) {
        setTimePosition(getTimeAtFraction(fractionPosition))
    }

    /**
     * Internal usage for applying changes according to rendering update hierarchy.
     * <br></br>Time position must be applied inside a global [android.view.Choreographer] frame
     * callback to ensure that the transformations are applied in a hierarchical order.
     *
     * @return true if changes has been made
     */
    fun isDirty(): Boolean {
        return _isDirty
    }

    /**
     * Set the state of this object properties to changed.
     * And tell the [AnimatableModel] to take care of it.
     */
    fun setDirty(isDirty: Boolean) {
        this._isDirty = isDirty
        if (isDirty) {
            model.onModelAnimationChanged(this)
        }
    }

    /**
     * Get the elapsed time in seconds of a frame position
     *
     * @param frame Frame number on the timeline
     * @return Elapsed time of interest in seconds
     */
    fun getTimeAtFrame(frame: Int): Float {
        return frameToTime(frame, getFrameRate())
    }

    /**
     * Get the frame position at the elapsed time in seconds.
     *
     * @param time Elapsed time of interest in seconds
     * @return The frame number at the specified time
     */
    fun getFrameAtTime(time: Float): Int {
        return timeToFrame(time, getFrameRate())
    }

    /**
     * Get the elapsed time in seconds of a fraction position
     *
     * @param fraction The fractional (from 0 to 1) value of interest
     * @return Elapsed time at the specified fraction
     */
    fun getTimeAtFraction(fraction: Float): Float {
        return fractionToTime(fraction, duration)
    }

    /**
     * Get the fraction position at the elapsed time in seconds.
     *
     * @param time Elapsed time of interest in seconds.
     * @return The fractional (from 0 to 1) value at the specified time
     */
    fun getFractionAtTime(time: Float): Float {
        return timeToFraction(time, duration)
    }

    /**
     * This class holds information about a property and the values that that property
     * should take during an animation.
     * PropertyValuesHolder objects can be used to create animations with ObjectAnimator or
     * that operate on several different properties in parallel.
     *
     *
     * Using this [PropertyValuesHolder] provide an handled [ModelAnimator] canceling
     * since we target a same object and those PropertyValuesHolder have the same property name
     */
    object PropertyValuesHolder {
        /**
         * Constructs and returns a PropertyValuesHolder with a given set of time values.
         *
         * @param times The times that the [ModelAnimation] will animate between.
         * A time value must be between 0 and [ModelAnimation.getDuration]
         * @return PropertyValuesHolder The constructed PropertyValuesHolder object.
         */
        fun ofTime(vararg times: Float): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofFloat(TIME_POSITION, *times)
        }

        /**
         * Constructs and returns a PropertyValuesHolder with a given set of frame values.
         *
         * **<u>Warning</u>**
         * Every PropertyValuesHolder that applies a modification on the time position of the
         * animation should use the ModelAnimation.TIME_POSITION instead of its own Property in order
         * to possibly cancel any ObjectAnimator operating time modifications on the same
         * ModelAnimation.
         * [android.animation.ObjectAnimator.setAutoCancel] will have no effect
         * for different property names
         *
         *
         * That's why we avoid using an ModelAnimation.FRAME_POSITION or ModelAnimation.FRACTION_POSITION Property
         *
         * @param frames The frames that the [ModelAnimation] will animate between.
         * @return PropertyValuesHolder The constructed PropertyValuesHolder object.
         */
        fun ofFrame(vararg frames: Int): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofInt(FRAME_POSITION, *frames)
        }

        /**
         * Constructs and returns a PropertyValuesHolder with a given set of fraction values.
         *
         * **<u>Warning</u>**
         * Every PropertyValuesHolder that applies a modification on the time position of the
         * animation should use the ModelAnimation.TIME_POSITION instead of its own Property in order
         * to possibly cancel any ObjectAnimator operating time modifications on the same
         * ModelAnimation.
         * [android.animation.ObjectAnimator.setAutoCancel] will have no effect
         * for different property names
         *
         *
         * That's why we avoid using an ModelAnimation.FRAME_POSITION or ModelAnimation.FRACTION_POSITION Property
         *
         * @param fractions The fractions that the [ModelAnimation] will animate between.
         * @return PropertyValuesHolder The constructed PropertyValuesHolder object.
         */
        fun ofFraction(vararg fractions: Float): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofFloat(FRACTION_POSITION, *fractions)
        }
    }

    companion object {
        /**
         * Get the elapsed time in seconds of a frame position
         *
         * @param frame     Frame number on the timeline
         * @param frameRate The frames per second of the animation
         * @return Elapsed time of interest in seconds
         */
        fun frameToTime(frame: Int, frameRate: Int): Float {
            return frame.toFloat() / frameRate.toFloat()
        }

        /**
         * Get the frame position at the elapsed time in seconds.
         *
         * @param time      Elapsed time of interest in seconds.
         * @param frameRate The frames per second of the animation
         * @return The frame number at the specified time
         */
        fun timeToFrame(time: Float, frameRate: Int): Int {
            return (time * frameRate).toInt()
        }

        /**
         * Get the elapsed time in seconds of a fraction position
         *
         * @param fraction The fractional (from 0 to 1) value of interest
         * @param duration Duration in seconds
         * @return Elapsed time at the specified fraction
         */
        fun fractionToTime(fraction: Float, duration: Float): Float {
            return fraction * duration
        }

        /**
         * Get the fraction position at the elapsed time in seconds.
         *
         * @param time     Elapsed time of interest in seconds.
         * @param duration Duration in seconds
         * @return The fractional (from 0 to 1) value at the specified time
         */
        fun timeToFraction(time: Float, duration: Float): Float {
            return time / duration
        }

        /**
         * Convert time in seconds to time in millis
         *
         * @param time Elapsed time of interest in seconds.
         * @return Elapsed time of interest in milliseconds
         */
        fun secondsToMillis(time: Float): Long {
            return (time * TimeUnit.SECONDS.toMillis(1).toFloat()).toLong()
        }

        /**
         * A Property wrapper around the `timePosition` functionality handled by the
         * [ModelAnimation.setTimePosition] and [ModelAnimation.getTimePosition]
         * methods.
         */
        val TIME_POSITION: FloatProperty<ModelAnimation> =
            object : FloatProperty<ModelAnimation>("timePosition") {
                override fun setValue(obj: ModelAnimation, value: Float) {
                    obj.setTimePosition(value)
                }

                override fun get(obj: ModelAnimation): Float {
                    return obj.getTimePosition()
                }
            }

        /**
         * A Property wrapper around the `framePosition` functionality handled by the
         * [ModelAnimation.setFramePosition] and [ModelAnimation.getFramePosition]
         * methods
         */
        val FRAME_POSITION: Property<ModelAnimation, Int> =
            object : IntProperty<ModelAnimation>("framePosition") {
                override fun setValue(obj: ModelAnimation, value: Int) {
                    obj.setFramePosition(value)
                }

                override fun get(obj: ModelAnimation): Int {
                    return obj.getFramePosition()
                }
            }

        /**
         * A Property wrapper around the `fractionPosition` functionality handled by the
         * [ModelAnimation.setFractionPosition] and [ModelAnimation.getFractionPosition]
         * methods
         */
        val FRACTION_POSITION: Property<ModelAnimation, Float> =
            object : FloatProperty<ModelAnimation>("fractionPosition") {
                override fun setValue(obj: ModelAnimation, value: Float) {
                    obj.setFractionPosition(value)
                }

                override fun get(obj: ModelAnimation): Float {
                    return obj.getFractionPosition()
                }
            }
    }

    /**
     * ModelAnimation constructed from an [Animator]
     *
     * @param name      This name should corresponds to the one defined and exported in the
     * [AnimatableModel].
     * <br></br>Typically the action name defined in the 3D creation software.
     * @param index     Zero-based index of the target `animation` as defined in the
     * original [AnimatableModel]
     * @param duration  This original [AnimatableModel] duration
     * @param frameRate The frames per second defined in the original animation asset
     */
    init {
        this.model = model
        this.index = index
        this.name = name
        if (TextUtils.isEmpty(this.name)) {
            this.name = index.toString()
        }
        this.frameRate = frameRate
        this.duration = duration
    }
}