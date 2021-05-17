package com.gorisse.thomas.realitycore

import android.view.Choreographer
import android.view.Surface
import android.view.SurfaceView
import com.google.android.filament.Engine
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.ar.core.Frame
import java.util.concurrent.TimeUnit

class Renderer(
    val arSession: ArSession, val engine: Engine, val surfaceView: SurfaceView, val view: View,
    private val doFrame: (frame: Frame) -> Unit
) : Choreographer.FrameCallback {
    companion object {
        private const val maxFramesPerSecond: Long = 60
    }

    sealed class FrameRate(val factor: Long) {
        object Full : FrameRate(1)
        object Half : FrameRate(2)
        object Third : FrameRate(3)
    }

    private data class Mirror(
        var surface: Surface? = null,
        var viewport: Viewport,
        var swapChain: SwapChain? = null
    )

    var isStarted = false
    private val choreographer: Choreographer = Choreographer.getInstance()
    private var lastTick: Long = 0
    private var frameRate: FrameRate = FrameRate.Full

    var timestamp: Long = 0L

    private val mirrors = mutableListOf<Mirror>()

    val renderer = engine.createRenderer()
    var swapChain: SwapChain? = null
    val displayHelper = DisplayHelper(surfaceView.context)
    val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
        renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface)
                displayHelper.attach(renderer, surfaceView.display)
            }

            override fun onDetachedFromSurface() {
                displayHelper.detach()
                swapChain?.let {
                    engine.destroySwapChain(it)
                    // Required to ensure we don't return before Filament is done executing the
                    // destroySwapChain command, otherwise Android might destroy the Surface
                    // too early
                    engine.flushAndWait()
                    swapChain = null
                }
            }

            override fun onResized(width: Int, height: Int) {
                view.viewport = Viewport(0, 0, width, height)
            }
        }

        attachTo(surfaceView)
    }

    override fun doFrame(frameTimeNanos: Long) {
        choreographer.postFrameCallback(this)

        // limit to max fps
        val nanoTime = System.nanoTime()
        val tick = nanoTime / (TimeUnit.SECONDS.toNanos(1) / maxFramesPerSecond)

        if (lastTick / frameRate.factor == tick / frameRate.factor) {
            return
        }

        lastTick = tick

        // render using frame from last tick to reduce possibility of jitter but increases latency
        if (// only render if we have an ar frame
            timestamp != 0L &&
            uiHelper.isReadyToRender &&
            // This means you are sending frames too quickly to the GPU
            renderer.beginFrame(swapChain!!, frameTimeNanos)
        ) {
            renderer.render(view)
            renderer.endFrame()
        }

        synchronized(mirrors) {
            mirrors.iterator().forEach { mirror ->
                if (mirror.surface == null) {
                    if (mirror.swapChain != null) {
                        engine.destroySwapChain(mirror.swapChain!!)
                    }
                    mirrors.remove(mirror)
                } else if (mirror.swapChain == null) {
                    mirror.swapChain = engine.createSwapChain(mirror.surface!!)
                }
            }
        }

        val frame = arSession.update()

        // During startup the camera system may not produce actual images immediately. In
        // this common case, a frame with timestamp = 0 will be returned.
        if (frame.timestamp != 0L &&
            frame.timestamp != timestamp
        ) {
            timestamp = frame.timestamp
            doFrame(frame)
        }
    }

    fun start() {
        if (!isStarted) {
            isStarted = true
            choreographer.postFrameCallback(this)
        }
    }

    fun pause() {
        if (isStarted) {
            isStarted = false
            choreographer.removeFrameCallback(this)
        }
    }

    fun destroy() {
        if (isStarted) {
            pause()
        }
        displayHelper.detach()
        uiHelper.detach()
        swapChain?.let { engine.destroySwapChain(it) }
        engine.destroyRenderer(renderer)
    }

    internal fun setDesiredSize(width: Int, height: Int) {
        uiHelper.setDesiredSize(width, height)
    }

    /**
     * Starts mirroring to the specified [Surface].
     */
    internal fun startMirroring(surface: Surface, left: Int, bottom: Int, width: Int, height: Int) {
        synchronized(mirrors) {
            mirrors.add(
                Mirror(
                    surface,
                    Viewport(left, bottom, width, height)
                )
            )
        }
    }

    /**
     * Stops mirroring to the specified [Surface].
     */
    internal fun stopMirroring(surface: Surface) {
        synchronized(mirrors) {
            for (mirror in mirrors) {
                if (mirror.surface === surface) {
                    mirror.surface = null
                }
            }
        }
    }
}
