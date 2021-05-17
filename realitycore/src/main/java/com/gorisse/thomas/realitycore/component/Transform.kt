package com.gorisse.thomas.realitycore.component

import android.animation.*
import android.util.Property
import android.view.animation.Interpolator
import com.google.android.filament.TransformManager
import com.google.android.filament.utils.*
import com.gorisse.thomas.realitycore.entity.Entity
import com.gorisse.thomas.realitycore.utils.of
import com.gorisse.thomas.realitycore.utils.relativeFrom
import com.gorisse.thomas.realitycore.utils.relativeTo
import getTransform
import setTransform

typealias Scale = Float3
typealias Rotation = Float3
typealias Position = Float3
typealias Size = Float3
typealias Direction = Float3

/**
 * ### A component that defines the scale, rotation, and translation of an entity.
 *
 * An entity acquires a Transform component, as well as a set of methods for manipulating the
 * transform, by implementing the [HasTransform] interface.
 * This is true for all entities, because the [Entity] base class implements the interface.
 *
 * @constructor Creates a new transform represented as a 4x4 matrix.
 * The Transform component can't represent all transforms that a general 4x4 matrix can
 * represent. Using a 4x4 matrix during initialization is therefore a lossy event that might
 * result in certain transformations, like shear, being dropped.
 * @property matrix A transformation matrix
 */
data class Transform(var matrix: Mat4 = Mat4()) {

    // region Creating a Transform

    /**
     * ### Creates a new transformation using the given values.
     *
     * The transform of the entity relative to the reference entity.
     *
     * @param scale A scale factor.
     * @param rotation The rotation given as a unit quaternion.
     * @param position The translation, or position along the x, y, and z axes.
     */
    constructor(
        scale: Scale = Scale(1.0f),
        rotation: Rotation = Rotation(0f),
        position: Position = Position(0f)
    ) : this(Mat4.of(scale, rotation, position))

    /**
     * ### Creates a new transform from the specified Euler angles.
     *
     * The rotation order using intrinsic rotation order is defined as:
     * 1. Rotate around y-axis (yaw).
     * 2. Rotate around the body-fixed x-axis (pitch).
     * 3. Rotate around the body-fixed z-axis (roll).
     *
     * The rotation order using extrinsic rotation order is defined as:
     * 1. Rotate around the z-axis (roll).
     * 2. Rotate around the world space x-axis (pitch).
     * 3. Rotate around the world space y-axis (yaw).
     *
     * @param pitchX The rotation around the x-axis in radians.
     * @param yawY The rotation around the y-axis in radians.
     * @param rollZ The rotation around the z-axis in radians.
     */
    constructor(pitchX: Float = 0.0f, yawY: Float = 0.0f, rollZ: Float = 0.0f) :
            this(rotation = Rotation(pitchX, yawY, rollZ))

    // endregion

    // region Setting Transform Properties

    /**
     * ### The scaling factor applied to the entity.
     */
    var scale: Scale
        get() = this.matrix.scale
        set(value) {
            this.matrix = Mat4.of(value, this.rotation, this.position)
        }

    /**
     * ### The rotation of the entity relative to its parent specified as Euler Angles.
     */
    var rotation: Rotation
        get() = this.matrix.rotation
        set(value) {
            this.matrix = Mat4.of(this.scale, value, this.position)
        }

    /**
     * ### The position of the entity along the x, y, and z axes.
     */
    var position: Position
        get() = this.matrix.position
        set(value) {
            this.matrix = Mat4.of(this.scale, this.rotation, value)
        }

    // endregion

    // region Getting the Identity Transform

    companion object {

        /**
         * ### The identity transform.
         *
         * The identity transform is defined as scale = (1, 1, 1), rotation = (0, 0, 0, 1), and translation = (0, 0, 0).
         */
        fun identity() = Transform(Mat4.identity())
    }

    // endregion
}

/**
 * ### An interface that enables manipulating the scale, rotation, and translation of an entity.
 *
 * All entities automatically implements this interface because the [Entity] base class does. This
 * implementation gives all entities a [Transform] component, and a collection of methods for
 * manipulating the component, that you use to position the entity in space.
 */
interface HasTransform {

    // region Accessing the Component

    /**
     * ### The local transform (i.e. relative to the parent) of a transform component.
     *
     * This operation can be slow if the hierarchy of transform is too deep, and this will be particularly bad when updating a lot of transforms.
     * In that case, consider using [TransformManager.openLocalTransformTransaction] / [TransformManager.commitLocalTransformTransaction].
     *
     * @see [getTransform]
     * @see [setTransform]
     */
    var transform: Transform

    // endregion

    // region Scaling an Entity

    /**
     * ### The scale of the entity relative to its parent.
     *
     * This is the same as the scale value on the transform.
     */
    var scale: Scale
        get() = scale(null)
        set(value) = setScale(value, null)

    /**
     * ### Gets the scale of an entity relative to the given entity.
     *
     * @param relativeTo The entity that defines a frame of reference.
     * @return The scale of the entity relative to the reference entity.
     */
    fun scale(relativeTo: Entity?): Scale {
        return convertTo(this.transform, relativeTo).scale
    }

    /**
     * ### Sets the scale factor of the entity relative to the given reference entity.
     *
     * @param scale A new scale factor, relative to the reference entity.
     * @param relativeTo The entity that defines a frame of reference. Set this to null to indicate world space.
     */
    fun setScale(scale: Scale, relativeTo: Entity?) {
        this.transform = convertFrom(Transform(scale = scale), relativeTo)
    }

    // endregion

    // region Rotating an Entity

    /**
     * ### The rotation of the entity relative to its parent specified as Euler Angles.
     *
     * This is the same as the rotation value on the transform.
     */
    var rotation: Rotation
        get() = rotation(null)
        set(value) = setRotation(value, null)

    /**
     * ### Gets the orientation of an entity relative to the given entity.
     *
     * @param relativeTo The entity that defines a frame of reference.
     * @return The orientation of the entity relative to the reference entity.
     */
    fun rotation(relativeTo: Entity?): Rotation {
        return convertTo(this.transform, relativeTo).rotation
    }

    /**
     * ### Sets the rotation of the entity relative to the given reference entity.
     *
     * @param rotation A new rotation, relative to the reference entity.
     * @param relativeTo The entity that defines a frame of reference. Set this to null to indicate world space.
     */
    fun setRotation(rotation: Rotation, relativeTo: Entity?) {
        this.transform = convertFrom(Transform(rotation = rotation), relativeTo)
    }

    // endregion

    // region Positioning an Entity

    /**
     * ### The position of the entity relative to its parent.
     *
     * This is the same as the position value on the transform.
     */
    var position: Position
        get() = position(null)
        set(value) = setPosition(value, null)

    /**
     * ### Gets the position of an entity relative to the given entity.
     *
     * @param relativeTo The entity that defines a frame of reference.
     * @return The position of the entity relative to the reference entity.
     */
    fun position(relativeTo: Entity?): Position {
        return convertTo(this.transform, relativeTo).position
    }

    /**
     * ### Sets the position of the entity relative to the given reference entity.
     *
     * @param position A new position, relative to the reference entity
     * @param relativeTo The entity that defines a frame of reference. Set this to null to indicate world space.
     */
    fun setPosition(position: Position, relativeTo: Entity?) {
        this.transform = convertFrom(Transform(position = position), relativeTo)
    }

    // endregion

    // region Using a Matrix

    /**
     * ### Gets the 4x4 transform matrix of an entity relative to the given entity.
     *
     * @param relativeTo The entity that defines a frame of reference.
     * @return The transform of the entity relative to the reference entity.
     */
    fun transformMatrix(relativeTo: Entity?): Mat4 {
        return convertTo(transform, relativeTo).matrix
    }

    /**
     * ### Sets the transform of the entity relative to the given reference entity using a 4x4 matrix representation.
     *
     * The Transform component can't represent all transforms that a general 4x4 matrix can represent.
     * Setting a transform using a 4x4 matrix is therefore a lossy event that might result in certain transformations, like shear, being dropped.
     *
     * @param transform A 4x4 transform matrix, given relative to reference entity.
     * @param relativeTo The entity that defines a frame of reference. Set this to null to indicate world space.
     */
    fun setTransformMatrix(matrix: Mat4, relativeTo: Entity?) {
        this.transform = convertFrom(Transform(matrix = matrix), relativeTo)
    }

    // endregion

    // region Moving an Entity

    /**
     * ### Moves an entity instantly to a new location given by a transform.
     *
     * @param transform A [Transform] instance that indicates the new location
     * @param relativeTo The entity that defines a frame of reference. Set this to null to indicate world space.
     */
    fun moveTo(transform: Transform, relativeTo: Entity?) =
        this.setTransformMatrix(transform.matrix, relativeTo)

    /**
     * ### Positions and orients the entity to look at a target from a given position.
     *
     * You can use this method on any entity, but it's particularly useful for orienting cameras and lights to aim at a particular point in space.
     * @see setTransformMatrix
     *
     * @param target The target position to look at.
     * @param fromPosition The new position of the entity.
     * @param upVector The up direction of the entity after moving.
     * @param relativeTo The entity that defines a frame of reference. Set this to null to indicate world space.
     */
    fun lookAt(
        target: Position,
        fromPosition: Position,
        upVector: Float3 = Float3(y = 1.0f),
        relativeTo: Entity?
    ) = setTransformMatrix(
        lookAt(eye = fromPosition, target = target, up = upVector),
        relativeTo
    )

    // endregion

    // region Animating an Entity

    /**
     * ### Moves an entity over a period of time to a new location given by a transform.
     *
     * @param target A Transform instance that indicates the new location.
     * @param relativeTo The entity that defines a frame of reference. Set this to null to indicate world space.
     * @param duration The time in milliseconds over which the move should occur.
     * @param interpolator The interpolator to be used by this animation. A value of null will result in linear interpolation.
     *
     * @return An [Animator] instance that you use to control the animation playback.
     */
    fun moveTo(
        target: Transform,
        relativeTo: Entity?,
        duration: Long,
        interpolator: Interpolator? = null
    ): Animator =
        ObjectAnimator.ofObject(
            this,
            object :
                Property<HasTransform, FloatArray>(FloatArray::class.java, "transformMatrix") {
                override fun get(entity: HasTransform): FloatArray =
                    entity.transformMatrix(relativeTo).toFloatArray()

                override fun set(entity: HasTransform, value: FloatArray) =
                    entity.setTransformMatrix(Mat4.of(*value), relativeTo)
            }, FloatArrayEvaluator(), target.matrix.toFloatArray()
        ).apply {
            this.duration = duration
            this.interpolator = interpolator
            start()
        }
    // endregion

    // region Converting Values Between Coordinate Spaces

    /**
     * ### Converts the scale, rotation, and position of a transform from the local space of a reference entity to the local space of the entity on which you called this method.
     *
     * @param transform The transform specified relative to referenceEntity.
     * @param referenceEntity The entity that defines a frame of reference. Set this to null to indicate world space.
     *
     * @return The transform given in the local space of the entity.
     */
    fun convertFrom(transform: Transform, referenceEntity: Entity?) =
        (transform relativeFrom referenceEntity) relativeTo this

    /**
     * ### Converts the scale, rotation, and position of a transform from the local space of the entity on which you called this method to the local space of a reference entity.
     *
     * @param transform The transform given in the local space of the entity.
     * @param referenceEntity The entity that defines a frame of reference. Set this to null to indicate world space.
     *
     * @return The transform specified relative to referenceEntity.
     */
    fun convertTo(transform: Transform, referenceEntity: Entity?) =
        (transform relativeFrom this) relativeTo referenceEntity

    // endregion
}

/**
 * ### Gets the scale, rotation, and position of a Transform relatively to the local space of a reference entity
 *
 * @receiver The Transform given in the local space of the reference transform.
 * @param referenceEntity The entity that defines a frame of reference. Set this to null to indicate world space.
 *
 * @return The Transform specified relative to reference entity.
 */
infix fun Transform.relativeTo(referenceEntity: HasTransform?) =
    Transform(this.matrix relativeTo referenceEntity?.transform?.matrix)

/**
 * ### Gets the relatives scale, rotation, and position of a Transform inside the local space of a reference entity
 *
 * @receiver The Transform specified relative to reference transform.
 * @param referenceEntity The entity that defines a frame of reference. Set this to null to indicate world space.
 *
 * @return The Transform given in the local space of the transform.
 */
infix fun Transform.relativeFrom(referenceEntity: HasTransform?) =
    Transform(this.matrix relativeFrom referenceEntity?.transform?.matrix)
