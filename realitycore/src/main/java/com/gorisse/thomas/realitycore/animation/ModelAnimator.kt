package com.google.ar.sceneform.animation

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.text.TextUtils
import android.util.Property
import android.view.animation.LinearInterpolator
import com.gorisse.thomas.realitycore.entity.ModelEntity
import java.lang.ref.WeakReference
import java.util.*

/**
 * This class provides support for animating an [ModelEntity]
 * <h2>Usage</h2>
 *
 *
 *
 * By default the [ModelAnimator] can play the full [ModelAnimation] starting from 0 to
 * the animation duration with:
 *
 * <pre>
 * [ModelAnimator.ofAnimation]
</pre> *
 *
 *
 * If you want to specify a start and end, you should use:
 *
 * <pre>
 * [ModelAnimator.ofAnimationTime]
</pre> *
 * <pre>
 * [ModelAnimator.ofAnimationFrame]
</pre> *
 * <pre>
 * [ModelAnimator.ofAnimationFraction]
</pre> *
 *
 * <h2>Use cases</h2>
 *
 * <h3>Simple usage</h3>
 *
 *
 * On a very basic 3D model like a single infinite rotating sphere, you should not have to
 * use [ModelAnimator] but probably instead just call:
 *
 * <pre>
 * [ModelEntity.animate]
</pre> *
 *
 * <h3>Single Model with Single Animation</h3>
 *
 *
 * If you want to animate a single model to a specific timeline position, use:
 *
 * <pre>
 * [ModelAnimator.ofAnimationTime]
</pre> *
 * <pre>
 * [ModelAnimator.ofAnimationFrame]
</pre> *
 * <pre>
 * [ModelAnimator.ofAnimationFraction]
</pre> *
 *
 *  *
 * A single time, frame, fraction value will go from the actual position to the desired one
 *
 *  *
 * Two values means form value1 to value2
 *
 *  *
 * More than two values means form value1 to value2 then to value3
 *
 * *Example:*
 * <pre>
 * ModelAnimator.ofAnimationFraction(model, "VerticalTranslation", 0f, 0.8f, 0f).start();
</pre> *
 *
 * <h3>Single Model with Multiple Animations</h3>
 *
 *
 * If the model is a character, for example, there may be one ModelAnimation for a walkcycle, a
 * second for a jump, a third for sidestepping and so on.
 *
 * <pre>
 * [ModelAnimator.ofAnimation]
</pre> *
 * <pre>
 * [ModelAnimator.ofMultipleAnimations]
</pre> *
 * *Example:*
 *
 *
 * Here you can see that no call to `animator.cancel()` is required because the
 * `animator.setAutoCancel(boolean)` is set to true by default.
 *
 * <pre>
 * ObjectAnimator walkAnimator = ModelAnimator.ofAnimation(model, "walk");
 * walkButton.setOnClickListener(v -> walkAnimator.start());
 *
 * ObjectAnimator runAnimator = ModelAnimator.ofAnimation(model, "run");
 * runButton.setOnClickListener(v -> runAnimator.start());
</pre> *
 * *or sequentially:*
 * <pre>
 * AnimatorSet animatorSet = new AnimatorSet();
 * animatorSet.playSequentially(ModelAnimator.ofMultipleAnimations(model, "walk", "run"));
 * animatorSet.start();
</pre> *
 * <h3>Multiple Models with Multiple Animations</h3>
 *
 *
 * For a synchronised animation set like animating a complete scene with multiple models
 * time or sequentially, please consider using an [android.animation.AnimatorSet] with one
 * [ModelAnimator] parametrized per step :
 *
 * *Example:*
 * <pre>
 * AnimatorSet completeFly = new AnimatorSet();
 *
 * ObjectAnimator liftOff = ModelAnimator.ofAnimationFraction(airPlaneModel, "FlyAltitude",0, 40);
 * liftOff.setInterpolator(new AccelerateInterpolator());
 *
 * AnimatorSet flying = new AnimatorSet();
 * ObjectAnimator flyAround = ModelAnimator.ofAnimation(airPlaneModel, "FlyAround");
 * flyAround.setRepeatCount(ValueAnimator.INFINITE);
 * flyAround.setDuration(10000);
 * ObjectAnimator airportBusHome = ModelAnimator.ofAnimationFraction(busModel, "Move", 0);
 * flying.playTogether(flyAround, airportBusHome);
 *
 * ObjectAnimator land = ModelAnimator.ofAnimationFraction(airPlaneModel, "FlyAltitude", 0);
 * land.setInterpolator(new DecelerateInterpolator());
 *
 * completeFly.playSequentially(liftOff, flying, land);
</pre> *
 *
 * <h3>Morphing animation</h3>
 *
 *
 * Assuming a character object has a skeleton, one keyframe track could store the data for the
 * position changes of the lower arm bone over time, a different track the data for the rotation
 * changes of the same bone, a third the track position, rotation or scaling of another bone, and so
 * on. It should be clear, that an ModelAnimation can act on lots of such tracks.
 *
 *
 *
 * Assuming the model has morph targets (for example one morph target showing a friendly face
 * and another showing an angry face), each track holds the information as to how the influence of a
 * certain morph target changes during the performance of the clip.
 *
 *
 *
 * In a glTF context, this [android.animation.Animator] updates matrices according to glTF
 * `animation` and `skin` definitions.
 *
 *
 * <h3>[ModelAnimator] can be used for two things</h3>
 *
 *  *
 * Updating matrices in `TransformManager` components according to the model
 * `animation` definitions.
 *
 *  *
 * Updating bone matrices in `RenderableManager` components according to the model
 * `skin` definitions.
 *
 *
 *
 *
 * Every PropertyValuesHolder that applies a modification on the time position of the animation
 * must use the ModelAnimation.TIME_POSITION instead of its own Property in order to possibly cancel
 * any ObjectAnimator operating time modifications on the same ModelAnimation.
 *
 *
 *
 * More information about Animator:
 * [
 * https://developer.android.com/guide/topics/graphics/prop-animation
](https://developer.android.com/guide/topics/graphics/prop-animation) *
 *
 */
object ModelAnimator {
    /**
     * Constructs and returns an [ObjectAnimator] for all [ModelAnimation]
     * inside an [ModelEntity].
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model The targeted model to animate
     * @return The constructed ObjectAnimator
     * @see .ofAnimation
     */
    fun ofAllAnimations(model: ModelEntity): ObjectAnimator {
        val animations = mutableListOf<ModelAnimation>()
        for (i in animations.indices) {
            animations.add(model.animations[i])
        }
        return ofAnimation(model, *animations.toTypedArray())
    }

    /**
     * Constructs and returns list of [ObjectAnimator] given names inside an
     * [ModelEntity].
     * Can be used directly with [android.animation.AnimatorSet.playTogether]
     * [android.animation.AnimatorSet.playSequentially]
     *
     * @param model The targeted model to animate
     * @return The constructed ObjectAnimator
     * @see .ofAnimation
     */
    fun ofMultipleAnimations(
        model: ModelEntity, vararg animationNames: String
    ): List<ObjectAnimator> {
        val objectAnimators: MutableList<ObjectAnimator> = ArrayList()
        for (element in animationNames) {
            objectAnimators.add(ofAnimation(model, element))
        }
        return objectAnimators
    }

    /**
     * Constructs and returns an [ObjectAnimator] for targeted [ModelAnimation] with
     * a given name inside an [ModelEntity].
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model          The targeted model to animate
     * @param animationNames The string names of the animations.
     * <br></br>This name should correspond to the one defined and exported in
     * the model.
     * <br></br>Typically the action name defined in the 3D creation software.
     * [ModelAnimation.getName]
     * @return The constructed ObjectAnimator
     * @see .ofAnimation
     */
    fun ofAnimation(model: ModelEntity, vararg animationNames: String): ObjectAnimator {
        val animations = mutableListOf<ModelAnimation>()
        for (i in animationNames.indices) {
            getAnimationByName(model, animationNames[i])?.let { animations.add(it) }
        }
        return ofAnimation(model, *animations.toTypedArray())
    }

    /**
     * Constructs and returns an [ObjectAnimator] for targeted [ModelAnimation] with
     * a given index inside an [ModelEntity].
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model            The targeted animatable to animate
     * @param animationIndexes Zero-based indexes for the animations of interest.
     * @return The constructed ObjectAnimator
     * @see .ofAnimation
     */
    fun ofAnimation(model: ModelEntity, vararg animationIndexes: Int): ObjectAnimator {
        val animations = mutableListOf<ModelAnimation>()
        for (i in animationIndexes.indices) {
            animations.add(model.animations[animationIndexes[i]])
        }
        return ofAnimation(model, *animations.toTypedArray())
    }

    /**
     * Constructs and returns an [ObjectAnimator] for a targeted [ModelAnimation] inside
     * an [ModelEntity].
     * This method applies by default this to the returned ObjectAnimator :
     *
     *  * The duration value to the max [ModelAnimation.getDuration] in order to
     * match the original animation speed.
     *  * The interpolator to [LinearInterpolator] in order to match the natural animation
     * interpolation.
     *
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model      The targeted animatable to animate
     * @param animations The animations of interest
     * @return The constructed ObjectAnimator
     */
    fun ofAnimation(model: ModelEntity, vararg animations: ModelAnimation): ObjectAnimator {
        val propertyValuesHolders = mutableListOf<android.animation.PropertyValuesHolder>()
        var duration: Long = 0
        for (i in animations.indices) {
            duration = Math.max(duration, animations[i].getDurationMillis())
            propertyValuesHolders.add(
                PropertyValuesHolder.ofAnimationTime(
                    animations[i], 0f, animations[i].getDuration()
                )
            )
        }
        val objectAnimator = ofPropertyValuesHolder(model, *propertyValuesHolders.toTypedArray())
        objectAnimator.duration = duration
        return objectAnimator
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * time values.
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model         The targeted model to animate
     * @param animationName The string name of the animation.
     * <br></br>This name should correspond to the one defined and exported in
     * the model.
     * <br></br>Typically the action name defined in the 3D creation software.
     * [ModelAnimation.getName]
     * @param times         The elapsed times (between 0 and [ModelAnimation.getDuration]
     * that the [ModelAnimation] will animate between.
     * @return The constructed ObjectAnimator
     * @see .ofAnimationTime
     * @see ModelAnimation.getName
     */
    fun ofAnimationTime(
        model: ModelEntity, animationName: String, vararg times: Float
    ): ObjectAnimator? {
        return getAnimationByName(model, animationName)?.let { ofAnimationTime(model, it, *times) }
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * time values.
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model          The targeted model to animate
     * @param animationIndex Zero-based index for the animation of interest.
     * @param times          The elapsed times (between 0 and [ModelAnimation.getDuration]
     * that the [ModelAnimation] will animate between.
     * @return The constructed ObjectAnimator
     * @see .ofAnimationTime
     */
    fun ofAnimationTime(
        model: ModelEntity, animationIndex: Int, vararg times: Float
    ): ObjectAnimator {
        return ofAnimationTime(
            model, model.animations[animationIndex], *times
        )
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * time values.
     *
     *
     * Time values can help you targeting a specific position on an animation coming from
     * a 3D creation software with a default times based timeline.
     * It's the 3D designer responsibility to tell you what specific timeline position
     * corresponds to a specific model appearance.
     *
     *
     *  * A single value implies that that value is the one being animated to starting from the
     * actual value on the provided [ModelAnimation].
     *  * Two values imply a starting and ending values.
     *  * More than two values imply a starting value, values to animate through along the way,
     * and an ending value (these values will be distributed evenly across the duration of the
     * animation).
     *
     *
     *
     * The properties (time, frame,... position) are applied to the [ModelEntity]
     * <br></br>This method applies by default this to the returned ObjectAnimator :
     *
     *
     *  * The duration value to the [ModelAnimation.getDuration] in order to
     * match the original animation speed.
     *  * The interpolator to [LinearInterpolator] in order to match the natural animation
     * interpolation.
     *
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model     The targeted model to animate
     * @param animation The animation of interest
     * @param times     The elapsed times (between 0 and [ModelAnimation.getDuration]
     * that the [ModelAnimation] will animate between.
     * @return The constructed ObjectAnimator
     */
    fun ofAnimationTime(
        model: ModelEntity, animation: ModelAnimation, vararg times: Float
    ): ObjectAnimator {
        return ofPropertyValuesHolder(
            model, animation, PropertyValuesHolder.ofAnimationTime(animation, *times)
        )
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * frame values.
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model         The targeted model to animate
     * @param animationName The string name of the animation.
     * <br></br>This name should correspond to the one defined and exported in
     * the model.
     * <br></br>Typically the action name defined in the 3D creation software.
     * [ModelAnimation.getName]
     * @param frames        The frame numbers (between 0 and [ModelAnimation.getFrameCount] that
     * the [ModelAnimation] will animate between.
     * @return The constructed ObjectAnimator
     * @see .ofAnimationFrame
     * @see ModelAnimation.getName
     */
    fun ofAnimationFrame(
        model: ModelEntity,
        animationName: String,
        vararg frames: Int
    ): ObjectAnimator? {
        return getAnimationByName(model, animationName)?.let {
            ofAnimationFrame(
                model,
                it,
                *frames
            )
        }
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * frame values.
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model          The targeted model to animate
     * @param animationIndex Zero-based index for the animation of interest.
     * @param frames         The frame numbers (between 0 and [ModelAnimation.getFrameCount] that
     * the [ModelAnimation] will animate between.
     * @return The constructed ObjectAnimator
     * @see .ofAnimationFrame
     */
    fun ofAnimationFrame(
        model: ModelEntity,
        animationIndex: Int,
        vararg frames: Int
    ): ObjectAnimator {
        return ofAnimationFrame(model, model.animations[animationIndex], *frames)
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * frame values.
     *
     *
     * Frame number can help you targeting a specific position on an animation coming from
     * a 3D creation software with a frame numbers based timeline.
     * It's the 3D designer responsibility to tell you what specific timeline position
     * corresponds to a specific model appearance.
     * <br></br>The corresponding time of a frame number is calculated using
     * [ModelAnimation.getFrameRate].
     *
     *
     *  * A single value implies that that value is the one being animated to starting from the
     * actual value on the provided [ModelAnimation].
     *  * Two values imply a starting and ending values.
     *  * More than two values imply a starting value, values to animate through along the way,
     * and an ending value (these values will be distributed evenly across the duration of the
     * animation).
     *
     *
     *
     * The properties (time, frame,... position) are applied to the [ModelEntity]
     * <br></br>This method applies by default this to the returned ObjectAnimator :
     *
     *
     *  * The duration value to the [ModelAnimation.getDuration] in order to
     * match the original animation speed.
     *  * The interpolator to [LinearInterpolator] in order to match the natural animation
     * interpolation.
     *
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model     The targeted model to animate
     * @param animation The animation of interest
     * @param frames    The frame numbers (between 0 and [ModelAnimation.getFrameCount] that
     * the [ModelAnimation] will animate between.
     * @return The constructed ObjectAnimator
     * @see .ofAnimationTime
     */
    fun ofAnimationFrame(
        model: ModelEntity, animation: ModelAnimation, vararg frames: Int
    ): ObjectAnimator {
        return ofPropertyValuesHolder(
            model, animation, PropertyValuesHolder.ofAnimationFrame(animation, *frames)
        )
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * fraction values.
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model         The targeted model to animate
     * @param animationName The string name of the animation.
     * <br></br>This name should correspond to the one defined and exported in
     * the model.
     * <br></br>Typically the action name defined in the 3D creation software.
     * [ModelAnimation.getName]
     * @param fractions     The fractions (percentage) (between 0 and 1)
     * @return The constructed ObjectAnimator
     * @see .ofAnimationFraction
     * @see ModelAnimation.getName
     */
    fun ofAnimationFraction(
        model: ModelEntity,
        animationName: String,
        vararg fractions: Float
    ): ObjectAnimator? {
        return getAnimationByName(model, animationName)?.let {
            ofAnimationFraction(
                model,
                it,
                *fractions
            )
        }
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * fraction values.
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model          The targeted model to animate
     * @param animationIndex Zero-based index for the animation of interest.
     * @param fractions      The fractions (percentage) (between 0 and 1)
     * @return The constructed ObjectAnimator
     * @see .ofAnimationFraction
     */
    fun ofAnimationFraction(
        model: ModelEntity,
        animationIndex: Int,
        vararg fractions: Float
    ): ObjectAnimator {
        return ofAnimationFraction(model, model.animations[animationIndex], *fractions)
    }

    /**
     * Constructs and returns an ObjectAnimator clipping a [ModelAnimation] to a given set of
     * fraction values.
     *
     *  * A single value implies that that value is the one being animated to starting from the
     * actual value on the provided [ModelAnimation].
     *  * Two values imply a starting and ending values.
     *  * More than two values imply a starting value, values to animate through along the way,
     * and an ending value (these values will be distributed evenly across the duration of the
     * animation).
     *
     *
     *
     * The properties (time, frame,... position) are applied to the [ModelEntity]
     * This method applies by default this to the returned ObjectAnimator :
     *
     *
     *  * The duration value to the [ModelAnimation.getDuration] in order to
     * match the original animation speed.
     *  * The interpolator to [LinearInterpolator] in order to match the natural animation
     * interpolation.
     *
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model     The targeted model to animate
     * @param animation The animation of interest
     * @param fractions The fractions (percentage) (between 0 and 1)
     * @return The constructed ObjectAnimator
     * @see .ofAnimationTime
     */
    fun ofAnimationFraction(
        model: ModelEntity, animation: ModelAnimation, vararg fractions: Float
    ): ObjectAnimator {
        return ofPropertyValuesHolder(
            model, animation, PropertyValuesHolder.ofAnimationFraction(animation, *fractions)
        )
    }

    private fun ofPropertyValuesHolder(
        model: ModelEntity,
        animation: ModelAnimation,
        value: android.animation.PropertyValuesHolder
    ): ObjectAnimator {
        val objectAnimator = ofPropertyValuesHolder(model, value)
        objectAnimator.duration = animation.getDurationMillis()
        objectAnimator.setAutoCancel(true)
        return objectAnimator
    }

    /**
     * Constructs and returns an ObjectAnimator a [ModelAnimation] applying
     * PropertyValuesHolders.
     *
     *  * A single value implies that that value is the one being animated to starting from the
     * actual value on the provided [ModelAnimation].
     *  * Two values imply a starting and ending values.
     *  * More than two values imply a starting value, values to animate through along the way,
     * and an ending value (these values will be distributed evenly across the duration of the
     * animation).
     *
     *
     *
     * The properties (time, frame,... position) are applied to the [ModelEntity]
     * <br></br>This method applies by default this to the returned ObjectAnimator :
     *
     *
     *  * The interpolator to [LinearInterpolator] in order to match the natural animation
     * interpolation.
     *
     *
     * Don't forget to call [ObjectAnimator.start]
     *
     * @param model  The targeted model to animate
     * @param values A set of PropertyValuesHolder objects whose values will be animated between over time.
     * @return The constructed ObjectAnimator
     */
    fun ofPropertyValuesHolder(
        model: ModelEntity,
        vararg values: android.animation.PropertyValuesHolder
    ): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(model, *values)
        objectAnimator.interpolator = LinearInterpolator()
        objectAnimator.repeatCount = ValueAnimator.INFINITE
        return objectAnimator
    }

    /**
     * Get the associated `Animation` by name or null if none exist with the given name.
     *
     *
     * This name should correspond to the one defined and exported in the model.
     * <br></br>Typically the action name defined in the 3D creation software.
     *
     *
     * @param name Weak reference to the string name of the animation or the
     * `String.valueOf(animation.getIndex())`> if none was specified.
     */
    private fun getAnimationByName(model: ModelEntity, name: String): ModelAnimation? {
        for (i in 0 until model.animations.size) {
            val animation: ModelAnimation = model.animations[i]
            if (TextUtils.equals(animation.name, name)) {
                return model.animations[i]
            }
        }
        return null
    }

    /**
     * This class holds information about a property and the values that that property
     * should take during an animation.
     *
     * PropertyValuesHolder objects can be used to create animations with ObjectAnimator or
     * that operate on several different properties in parallel.
     *
     * <h2>Warning:</h2>
     *
     * Using this PropertyValuesHolder is very useful for targeting multiple
     * time or frame properties of multiple animations inside a same ObjectAnimator model
     *
     * **and** insure a less consuming [android.view.Choreographer.FrameCallback] than
     * using [android.animation.AnimatorSet.playTogether]
     *
     * **but** If you want to use the
     * [ObjectAnimator.setAutoCancel] functionality, you have to
     * take care of this :
     *
     * <pre>
     * ObjectAnimator.hasSameTargetAndProperties(Animator anim) {
     * PropertyValuesHolder[] theirValues = ((ObjectAnimator) anim).getValues();
     * if (((ObjectAnimator) anim).getTarget() == getTarget() &&
     * mValues.length == theirValues.length) {
     * for (int i = 0; i < mValues.length; ++i) {
     * PropertyValuesHolder pvhMine = mValues[i];
     * PropertyValuesHolder pvhTheirs = theirValues[i];
     * if (pvhMine.getPropertyName() == null ||
     * !pvhMine.getPropertyName().equals(pvhTheirs.getPropertyName())) {
     * return false;
     * }
     * }
     * return true;
     * }
     * }
    </pre> *
     *
     * @see ObjectAnimator
     */
    object PropertyValuesHolder {
        /**
         * Constructs and returns a PropertyValuesHolder for a targeted [ModelAnimation].
         * This method applies by default this to the returned ObjectAnimator :
         *
         *  * The duration value to the [ModelAnimation.getDuration] in order to
         * match the original animation speed.
         *  * The interpolator to [LinearInterpolator] in order to match the natural animation
         * interpolation.
         *
         *
         * @param animation The animation of interest
         * @return The constructed PropertyValuesHolder object.
         */
        fun ofAnimation(animation: ModelAnimation): android.animation.PropertyValuesHolder {
            return ofAnimationTime(animation, 0f, animation.getDuration())
        }

        /**
         * Constructs and returns a PropertyValuesHolder for a targeted animation set of time
         * values.
         *
         * @param animationName The string name of the animation.
         * <br></br>This name should correspond to the one defined and exported in
         * the model.
         * <br></br>Typically the action name defined in the 3D creation software.
         * [ModelAnimation.getName]
         * @param times         The elapsed times (between 0 and [ModelAnimation.getDuration]
         * that the [ModelAnimation] will animate between.
         * @return The constructed PropertyValuesHolder object.
         * @see .ofAnimationTime
         */
        fun ofAnimationTime(
            animationName: String,
            vararg times: Float
        ): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofFloat(
                AnimationProperty(
                    animationName,
                    ModelAnimation.TIME_POSITION
                ), *times
            )
        }

        /**
         * Constructs and returns a PropertyValuesHolder for a targeted [ModelAnimation] with
         * a given set of time values.
         *
         *  * A single value implies that that value is the one being animated to starting from the
         * actual value on the provided [ModelAnimation].
         *  * Two values imply a starting and ending values.
         *  * More than two values imply a starting value, values to animate through along the way,
         * and an ending value (these values will be distributed evenly across the duration of the
         * animation).
         *
         *
         *
         * The properties (time, frame,... position) are applied to the [ModelEntity]
         * <br></br>This method applies by default this to the returned ObjectAnimator :
         *
         *
         *  * The duration value to the [ModelAnimation.getDuration] in order to
         * match the original animation speed.
         *  * The interpolator to [LinearInterpolator] in order to match the natural animation
         * interpolation.
         *
         *
         * @param animation The animation of interest
         * @param times     The elapsed times (between 0 and [ModelAnimation.getDuration]
         * that the [ModelAnimation] will animate between.
         * @return The constructed PropertyValuesHolder object.
         */
        fun ofAnimationTime(
            animation: ModelAnimation,
            vararg times: Float
        ): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofFloat(
                AnimationProperty(
                    animation,
                    ModelAnimation.TIME_POSITION
                ), *times
            )
        }

        /**
         * Constructs and returns a PropertyValuesHolder for a targeted [ModelAnimation] with
         * a given set of fame values.
         *
         * @param animationName The string name of the animation.
         * <br></br>This name should correspond to the one defined and exported in
         * the model.
         * <br></br>Typically the action name defined in the 3D creation software.
         * [ModelAnimation.getName]
         * @param frames        The frame numbers (between 0 and
         * [ModelAnimation.getFrameCount] that
         * the [ModelAnimation] will animate between.
         * @return The constructed PropertyValuesHolder object.
         * @see .ofAnimationFrame
         */
        fun ofAnimationFrame(
            animationName: String,
            vararg frames: Int
        ): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofInt(
                AnimationProperty(
                    animationName,
                    ModelAnimation.FRAME_POSITION
                ), *frames
            )
        }

        /**
         * Constructs and returns a PropertyValuesHolder for a targeted [ModelAnimation] with
         * a given set of frame values.
         *
         *  * A single value implies that that value is the one being animated to starting from the
         * actual value on the provided [ModelAnimation].
         *  * Two values imply a starting and ending values.
         *  * More than two values imply a starting value, values to animate through along the way,
         * and an ending value (these values will be distributed evenly across the duration of the
         * animation).
         *
         *
         *
         * The properties (time, frame,... position) are applied to the [ModelEntity]
         * <br></br>This method applies by default this to the returned ObjectAnimator :
         *
         *
         *  * The duration value to the [ModelAnimation.getDuration] in order to
         * match the original animation speed.
         *  * The interpolator to [LinearInterpolator] in order to match the natural animation
         * interpolation.
         *
         *
         * @param animation The animation of interest
         * @param frames    The frame numbers (between 0 and [ModelAnimation.getFrameCount] that
         * the [ModelAnimation] will animate between.
         * @return The constructed PropertyValuesHolder object.
         */
        fun ofAnimationFrame(
            animation: ModelAnimation,
            vararg frames: Int
        ): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofInt(
                AnimationProperty(
                    animation,
                    ModelAnimation.FRAME_POSITION
                ), *frames
            )
        }

        /**
         * Constructs and returns a PropertyValuesHolder for a targeted [ModelAnimation] with
         * a given set of fraction values.
         *
         * @param animationName The string name of the animation.
         * <br></br>This name should correspond to the one defined and exported in
         * the model.
         * <br></br>Typically the action name defined in the 3D creation software.
         * [ModelAnimation.getName]
         * @param fractions     The fractions (percentage) (between 0 and 1)
         * @return The constructed PropertyValuesHolder object.
         * @see .ofAnimationFraction
         */
        fun ofAnimationFraction(
            animationName: String,
            vararg fractions: Float
        ): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofFloat(
                AnimationProperty(
                    animationName,
                    ModelAnimation.FRACTION_POSITION
                ), *fractions
            )
        }

        /**
         * Constructs and returns a PropertyValuesHolder for a targeted [ModelAnimation] with
         * a given set of fraction values.
         *
         *  * A single value implies that that value is the one being animated to starting from the
         * actual value on the provided [ModelAnimation].
         *  * Two values imply a starting and ending values.
         *  * More than two values imply a starting value, values to animate through along the way,
         * and an ending value (these values will be distributed evenly across the duration of the
         * animation).
         *
         *
         *
         * The properties (time, frame,... position) are applied to the [ModelEntity]
         * <br></br>This method applies by default this to the returned ObjectAnimator :
         *
         *
         *  * The duration value to the [ModelAnimation.getDuration] in order to
         * match the original animation speed.
         *  * The interpolator to [LinearInterpolator] in order to match the natural animation
         * interpolation.
         *
         *
         * @param animation The animation of interest
         * @param fractions The fractions (percentage) (between 0 and 1)
         * @return The constructed PropertyValuesHolder object.
         */
        fun ofAnimationFraction(
            animation: ModelAnimation,
            vararg fractions: Float
        ): android.animation.PropertyValuesHolder {
            return android.animation.PropertyValuesHolder.ofFloat(
                AnimationProperty(
                    animation,
                    ModelAnimation.FRACTION_POSITION
                ), *fractions
            )
        }

        /**
         * Internal class to manage a sub Renderable Animation property
         */
        internal class AnimationProperty<T> : Property<ModelEntity, T> {
            var animation: WeakReference<ModelAnimation>? = null
            var animationName: String? = null
            var property: Property<ModelAnimation, T>

            constructor(animation: ModelAnimation, property: Property<ModelAnimation, T>) : super(
                property.type,
                "animation[" + animation.name + "]." + property.name
            ) {
                this.property = property
                this.animation = WeakReference(animation)
            }

            constructor(animationName: String, property: Property<ModelAnimation, T>) : super(
                property.type,
                "animation[" + animationName + "]." + property.name
            ) {
                this.property = property
                this.animationName = animationName
            }

            override fun set(obj: ModelEntity, value: T) {
                property[getAnimation(obj)] = value
            }

            override fun get(obj: ModelEntity): T {
                return property[getAnimation(obj)]
            }

            private fun getAnimation(model: ModelEntity): ModelAnimation? {
                if (animation == null && animation!!.get() == null) {
                    animation = WeakReference(getAnimationByName(model, animationName!!))
                }
                return animation!!.get()
            }
        }
    }
}