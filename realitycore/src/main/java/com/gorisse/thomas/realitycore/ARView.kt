package com.gorisse.thomas.realitycore

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import androidx.lifecycle.*
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.utils.Mat4
import com.google.ar.core.Frame
import com.gorisse.thomas.realitycore.ar.ArEnvironment
import com.gorisse.thomas.realitycore.filament.Filament
import com.gorisse.thomas.realitycore.filament.filamentEngine
import com.gorisse.thomas.realitycore.filament.resourceLoader
import com.gorisse.thomas.realitycore.scene.Scene
import com.gorisse.thomas.realitycore.utils.OpenGL
import com.gorisse.thomas.realitycore.utils.OpenGL.isOpenGlVersionSupported
import com.gorisse.thomas.realitycore.utils.OpenGLVersionNotSupported
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ## A view that displays an augmented reality experience that incorporates content from
 * RealityCore.
 * Use an ARView instance to display rendered 3D graphics to the user. You typically add a single
 * view to your app's layouts, and then provide an outlet for that view in your code.
 * Alternatively, you can create and add a view to your view hierarchy programmatically at runtime,
 * as you would any other view.
 *
 * A view has a single Scene instance that you access through the read-only scene property.
 * To the view's [Scene] instance you add one or more [AnchorEntity] instances that tell the view’s
 * [ARSession] how to tether content to something in the real world. To each anchor, you attach a
 * hierarchy of other [EntityInstance] that make up the content of the scene.
 */
open class ARView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : SurfaceView(context, attrs, defStyleAttr, defStyleRes), LifecycleObserver {

    val lifecycleScope: Flow<LifecycleCoroutineScope> = callbackFlow {
        val listener = object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                removeOnAttachStateChangeListener(this)
                offer(findViewTreeLifecycleOwner()!!.lifecycleScope)
            }

            override fun onViewDetachedFromWindow(v: View?) {
            }
        }
        addOnAttachStateChangeListener(listener)
        awaitClose { removeOnAttachStateChangeListener(listener) }
    }

    private val cameraStreamTextureId: Int = OpenGL.createExternalTextureId()

    //ArCore
    /**
     * The AR session that supports the view's rendering.
     */
    val session: ArSession = ArSession(context, cameraStreamTextureId)

    //Filament
    /**
     * Engine is filament's main entry-point.
     * An Engine instance main function is to keep track of all resources created by the user and
     * manage the rendering thread as well as the hardware renderer.
     */
    val engine: Engine = Filament.retain()

    /**
     * The scene that the view renders and simulates.
     */
    val scene: Scene by lazy { Scene(engine) }
    internal val camera: Camera by lazy {
        engine.createCamera()
            .apply {
//            //TODO : check that
//            // Default camera settings are used everwhere that ARCore HDR Lighting (Deeplight) is disabled or
//            // unavailable.
//            val DEFAULT_CAMERA_APERATURE = 4.0f
//            val DEFAULT_CAMERA_SHUTTER_SPEED = 1.0f / 30.0f
//            val DEFAULT_CAMERA_ISO = 320.0f
//
//            // HDR lighting camera settings are chosen to provide an exposure value of 1.0.  These are used
//            // when ARCore HDR Lighting is enabled in Sceneform.
//            val ARCORE_HDR_LIGHTING_CAMERA_APERATURE = 1.0f
//            val ARCORE_HDR_LIGHTING_CAMERA_SHUTTER_SPEED = 1.2f
//            val ARCORE_HDR_LIGHTING_CAMERA_ISO = 100.0f

                // Set the exposure on the camera, this exposure follows the sunny f/16 rule
                // Since we've defined a light that has the same intensity as the sun, it
                // guarantees a proper exposure
                setExposure(16f, 1f / 125f, 100f)
            }
    }

    internal val view = engine.createView()
        .apply {
            camera = this@ARView.camera
            scene = this@ARView.scene.filamentScene
        }

    //ArCore to Filament
    lateinit var arEnvironment: ArEnvironment

    //FrameCallback
    val renderer = Renderer(session, filamentEngine, this, view, ::doFrame)
    var frame: Frame? = null

    init {
        if (!context.isOpenGlVersionSupported(Filament.MIN_OPENGL_VERSION)) {
            throw OpenGLVersionNotSupported
        }
        MainScope().launch {
            lifecycleScope.collect { lifecycleScope ->
                lifecycleScope.launchWhenResumed {
                    try {
                        session.resume()
                        renderer.start()

                        awaitCancellation()
                    } finally {
                        renderer.destroy()
                        session.pause()
                    }
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val lifecycleScope = findViewTreeLifecycleOwner()!!.lifecycleScope
        lifecycleScope.launchWhenCreated {
            try {
                Filament.retain()
                awaitCancellation()
            } finally {
                renderer.destroy()
                session.close()
                Filament.release()
            }
        }
        lifecycleScope.launchWhenResumed {
            try {
                session.resume()
                renderer.start()
                awaitCancellation()
            } finally {
                renderer.destroy()
                session.pause()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        renderer.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        renderer.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        renderer.destroy()
        session.close()
        Filament.release()
    }

    /**
     * Callback that occurs for each display frame. Updates the scene and reposts itself to be called
     * by the choreographer on the next frame.
     */
    open fun doFrame(frame: Frame) {
        if (frame == null) {
            val dimensions = frame.camera.textureIntrinsics.imageDimensions
            cameraStream = CameraStream(context, engine, session, dimensions[0], dimensions[1])
            scene.addRenderable(cameraStream.renderableInstance)
        }
        this.frame = frame
        cameraStream.doFrame(frame)

        // update camera projection
        camera.setCustomProjection(
            frame.projectionMatrix.toFloatArray().toDoubleArray(),
            ARSession.NEAR_CLIP_PLANE_METERS.toDouble(),
            ARSession.FAR_CLIP_PLANE_METERS.toDouble()
        )

        val cameraTransform = frame.camera.displayOrientedPose.matrix.toFloatArray()
        camera.setModelMatrix(cameraTransform)
        val instance = engine.transformManager.create(cameraStreamTextureId)
        engine.transformManager.setTransform(instance, cameraTransform)

        // Allow the resource loader to finalize textures that have become ready.
        resourceLoader.asyncUpdateLoad()

        scene.doFrame(frame)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = right - left
        val height = bottom - top
        renderer.setDesiredSize(width, height)
        session.setDisplayGeometry(display.rotation, width, height)
        cameraStream.displayRotation = display.rotation
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        // this makes sure that the view's onTouchListener is called.
        if (!super.onTouchEvent(motionEvent)) {
            scene!!.onTouchEvent(motionEvent)
            // We must always return true to guarantee that this view will receive all touch events.
            // TODO: Update Scene.onTouchEvent to return if it was handled.
            return true
        }
        return true
    }

    /**
     * To capture the contents of this view, designate a [Surface] onto which this SceneView
     * should be mirrored. Use [android.media.MediaRecorder.getSurface], [ ][android.media.MediaCodec.createInputSurface] or [ ][android.media.MediaCodec.createPersistentInputSurface] to obtain the input surface for
     * recording. This will incur a rendering performance cost and should only be set when capturing
     * this view. To stop the additional rendering, call stopMirroringToSurface.
     *
     * @param surface the Surface onto which the rendered scene should be mirrored.
     * @param left    the left edge of the rectangle into which the view should be mirrored on surface.
     * @param bottom  the bottom edge of the rectangle into which the view should be mirrored on
     * surface.
     * @param width   the width of the rectangle into which the SceneView should be mirrored on surface.
     * @param height  the height of the rectangle into which the SceneView should be mirrored on
     * surface.
     */
    fun startMirroringToSurface(surface: Surface, left: Int, bottom: Int, width: Int, height: Int) {
        renderer.startMirroring(surface, left, bottom, width, height)
    }

    /**
     * When capturing is complete, call this method to stop mirroring the SceneView to the specified
     * [Surface]. If this is not called, the additional performance cost will remain.
     *
     * The application is responsible for calling [Surface.release] on the Surface when
     * done.
     */
    fun stopMirroringToSurface(surface: Surface) {
        renderer.stopMirroring(surface)
    }
}

val Frame.projectionMatrix: Mat4
    get() = Mat4.of(*FloatArray(16).apply {
        camera.getProjectionMatrix(
            this,
            0,
            ARSession.NEAR_CLIP_PLANE_METERS,
            ARSession.FAR_CLIP_PLANE_METERS
        )
    })

fun FloatArray.toDoubleArray(): DoubleArray = DoubleArray(size)
    .also { doubleArray ->
        for (i in indices) {
            doubleArray[i] = this[i].toDouble()
        }
    }