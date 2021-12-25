package com.gorisse.thomas.realitycore.ar

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.core.content.getSystemService
import com.google.android.filament.*
import com.google.android.filament.RenderableManager.PrimitiveType
import com.google.android.filament.VertexBuffer.AttributeType
import com.google.android.filament.VertexBuffer.VertexAttribute
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Float4
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.translation
import com.google.ar.core.ArImage
import com.google.ar.core.Frame
import com.google.ar.sceneform.extension.*
import com.google.ar.sceneform.loader.MaterialLoader
import com.google.ar.sceneform.math.rotation4
import com.google.ar.sceneform.utilities.toFloatBuffer
import com.google.ar.sceneform.utilities.toShortBuffer
import com.gorisse.thomas.realitycore.ArSession
import com.gorisse.thomas.realitycore.filament.build

class ArSkybox(
    private val context: Context,
    val engine: Engine,
    private val session: ArSession,
    val scene: Scene,
    width: Int,
    height: Int
) {
    private val cameraManager: CameraManager = context.getSystemService()!!

    private var displayRotation: Int = 0

    private var hasDepthImage: Boolean = false
    private lateinit var depthTexture: Texture
    private var uvTransform = Mat4()

    private val stream: Stream = Stream.Builder()
        .stream(session.cameraTextureId.toLong())
        .width(width)
        .height(height)
        .build(engine)

    /**
     * [RenderableInstance] for camera rendering
     */
    val renderableInstance: RenderableInstance by lazy {
        RenderableManager
            .Builder(1)
            .castShadows(false)
            .receiveShadows(false)
            .culling(false)
            .geometry(
                0,
                PrimitiveType.TRIANGLES,
                VertexBuffer
                    .Builder()
                    .vertexCount(VERTEX_COUNT)
                    .bufferCount(2)
                    .attribute(
                        VertexAttribute.POSITION,
                        POSITION_BUFFER_INDEX,
                        AttributeType.FLOAT2,
                        0,
                        0
                    )
                    .attribute(
                        VertexAttribute.UV0,
                        UV_BUFFER_INDEX,
                        AttributeType.FLOAT2,
                        0,
                        0
                    )
                    .build(engine)
                    .apply {
                        setBufferAt(
                            engine,
                            POSITION_BUFFER_INDEX,
                            VERTICES.toFloatBuffer()
                        )
                        setBufferAt(
                            engine,
                            UV_BUFFER_INDEX,
                            UVS.toFloatBuffer()
                        )
                    },
                IndexBuffer
                    .Builder()
                    .indexCount(INDICES.size)
                    .bufferType(IndexBuffer.Builder.IndexType.USHORT)
                    .build(engine)
                    .apply {
                        setBuffer(engine, INDICES.toShortBuffer())
                    }
            )
            //.material(0, flatMaterialInstance)
            .build()
            .createInstance(engine)
    }

    internal fun doFrame(frame: Frame) {
        renderableInstance.material = ((
                frame.takeIf { session.isDepthModeSupported }?.acquireDepthImage()?.takeIf {
                    try {
                        it.planes[0].buffer[0] != 0.toByte()
                    } catch (error: Throwable) {
                        false
                    }
                } as? ArImage)?.let { depthImage ->
            hasDepthImage = true
            depthTexture.setImage(
                engine,
                0,
                Texture.PixelBufferDescriptor(
                    depthImage.planes[0].buffer,
                    Texture.Format.RG,
                    Texture.Type.UBYTE,
                    1,
                    0,
                    0,
                    0,
                    Handler(Looper.myLooper()!!)
                ) {
                    depthImage.close()
                    hasDepthImage = false
                }
            )
            depthMaterialInstance
        } ?: flatMaterialInstance)
            .also { materialInstance ->
                uvTransform(displayRotation).takeIf { it != uvTransform }?.let {
                    uvTransform = it
                    materialInstance.setParameter(
                        "uvTransform",
                        MaterialInstance.FloatElement.FLOAT4,
                        uvTransform.toFloatArray(),
                        0,
                        4,
                    )

                }
            }
    }

    /**
     * The [MaterialInstance] used when Depth is not activated or not available
     */
    val flatMaterialInstance: MaterialInstance by lazy {
        MaterialLoader.loadFilamatFile(context.assets, "materials/ar_environment_material_flat.filamat")
            .createInstance()
            .apply {
                setExternalStreamTexture("cameraTexture", stream)
                setParameter(
                    "uvTransform",
                    MaterialInstance.FloatElement.FLOAT4,
                    Mat4().toFloatArray(),
                    0,
                    4
                )
            }
    }

    /**
     * The [MaterialInstance] used when Depth is activated and available
     */
    val depthMaterialInstance: MaterialInstance by lazy {
        MaterialLoader.loadFilamatFile(context.assets, "materials/ar_environment_material_depth.filamat")
            .createInstance()
            .apply {
                setExternalStreamTexture("cameraTexture", stream)
                setTexture(
                    "depthTexture",
                    textureBuilder = {
                        width(width)
                        height(height)
                        sampler(Texture.Sampler.SAMPLER_2D)
                        format(Texture.InternalFormat.RG8)
                        levels(1)
                    }, texture = { depthTexture = this })
                setParameter(
                    "uvTransform",
                    MaterialInstance.FloatElement.FLOAT4,
                    Mat4().toFloatArray(),
                    0,
                    4
                )
            }
    }

    private fun uvTransform(displayRotation: Int) = translation(Float3(.5f, .5f, 0f)) *
            rotation4(
                Float4(
                    0f,
                    0f,
                    -1f,
                    cameraManager.imageRotation(session.cameraId, displayRotation)
                        .toFloat()
                )
            ) * translation(Float3(-.5f, -.5f, 0f))

    private fun CameraManager.imageRotation(cameraId: String, displayRotation: Int): Int =
        (getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!! +
                when (displayRotation) {
                    Surface.ROTATION_0 -> 90
                    Surface.ROTATION_90 -> 0
                    Surface.ROTATION_180 -> 270
                    Surface.ROTATION_270 -> 180
                    else -> throw Exception()
                } + 270) % 360

    companion object {
        private const val VERTEX_COUNT = 4
        private const val POSITION_BUFFER_INDEX = 0
        private val VERTICES = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
        )
        private const val UV_BUFFER_INDEX = 1
        private val UVS = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )
        private val INDICES = shortArrayOf(
            0, 1, 2, 2, 1, 3
        )
    }
}
