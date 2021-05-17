package com.gorisse.thomas.realitycore.utils

import com.google.android.filament.utils.*
import com.gorisse.thomas.realitycore.component.Transform
import com.gorisse.thomas.realitycore.entity.Entity
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

inline val Float.Companion.degreesInTau: Float get() = 360f
inline val Float.Companion.tau: Float get() = PI.toFloat() * 2f
inline val Float.toDegrees: Float get() = this * (Float.degreesInTau / Float.tau)
inline val Float.toRadians: Float get() = this * (Float.tau / Float.degreesInTau)

inline val Float.clampToTau: Float
    get() =
        when {
            this < 0f ->
                this + ceil(-this / Float.tau) * Float.tau
            this >= Float.tau ->
                this - floor(this / Float.tau) * Float.tau
            else ->
                this
        }

val Mat4.rotation4: Float4
    get() {
        val t = x.x + y.y + z.z
        return normalize(
            when {
                t > 0 -> {
                    val s = sqrt(t + 1.0).toFloat() * 2.0f
                    Float4((z.y - y.z) / s, (x.z - z.x) / s, (y.x - x.y) / s, 0.25f * s)
                }
                x.x > y.y && x.x > z.z -> {
                    val s = sqrt((1.0f + x.x - y.y - z.z).toDouble()).toFloat() * 2.0f
                    Float4(0.25f * s, (x.y + y.x) / s, (x.z + z.x) / s, (z.y - y.z) / s)
                }
                y.y > z.z -> {
                    val s = sqrt((1.0f + y.y - x.x - z.z).toDouble()).toFloat() * 2.0f
                    Float4((x.y + y.x) / s, 0.25f * s, (y.z + z.y) / s, (x.z - z.x) / s)
                }
                else -> {
                    val s = sqrt((1.0f + z.z - x.x - y.y).toDouble()).toFloat() * 2.0f
                    Float4((y.x - x.y) / s, (x.z + z.x) / s, (y.z + z.y) / s, 0.25f * s)
                }
            }
        )
    }

fun rotation4(q: Float4): Mat4 {
    val n = normalize(q)
    val s = Mat4(x = n.x * n, y = n.y * n, z = n.z * n)
    return Mat4().apply {
        x.x = 1.0f - 2.0f * (s.y.y + s.z.z)
        x.y = 2.0f * (s.x.y - s.z.w)
        x.z = 2.0f * (s.x.z + s.y.w)
        y.x = 2.0f * (s.x.y + s.z.w)
        y.y = 1.0f - 2.0f * (s.x.x + s.z.z)
        y.z = 2.0f * (s.y.z - s.x.w)
        z.x = 2.0f * (s.x.z - s.y.w)
        z.y = 2.0f * (s.y.z + s.x.w)
        z.z = 1.0f - 2.0f * (s.x.x + s.y.y)
    }
}

/**
 * ### Gets the scale, rotation, and position of a matrix relatively to the local space of a reference matrix entity
 *
 * @receiver The matrix given in the local space of the reference matrix.
 * @param referenceMatrix The matrix that defines a frame of reference. Set this to null to indicate world space.
 *
 * @return The matrix specified relative to reference matrix.
 */
infix fun Mat4.relativeTo(referenceMatrix: Mat4?) = Mat4.of(
    scale = referenceMatrix?.let { scale * it.scale } ?: scale,
    rotation = referenceMatrix?.let { rotation + it.rotation } ?: rotation,
    position = referenceMatrix?.let { position + it.position } ?: position
)

/**
 * ### Gets the relatives scale, rotation, and position of a matrix inside the local space of a reference matrix
 *
 * @receiver The matrix specified relative to reference matrix.
 * @param referenceMatrix The matrix that defines a frame of reference. Set this to null to indicate world space.
 *
 * @return The matrix given in the local space of the matrix.
 */
infix fun Mat4.relativeFrom(referenceMatrix: Mat4?) = Mat4.of(
    scale = referenceMatrix?.let { scale / it.scale } ?: scale,
    rotation = referenceMatrix?.let { rotation - it.rotation } ?: rotation,
    position = referenceMatrix?.let { position - it.position } ?: position
)

fun Mat4.Companion.of(
    scale: Float3 = Float3(1.0f),
    rotation: Float3 = Float3(0f),
    position: Float3 = Float3(0f)
) = scale(scale) * rotation(rotation) * translation(position)