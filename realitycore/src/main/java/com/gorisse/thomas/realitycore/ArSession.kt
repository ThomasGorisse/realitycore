package com.gorisse.thomas.realitycore

import android.content.Context
import com.google.ar.core.Config
import com.google.ar.core.Session

class ArSession(
    context: Context,
    val cameraTextureId: Int,
    sessionConfig: Config.() -> Unit = {}
) : Session(context) {

    val cameraId get() = cameraConfig.cameraId

    val isDepthModeSupported = isDepthModeSupported(Config.DepthMode.AUTOMATIC)

    var planeFindingMode: Config.PlaneFindingMode
        get() = config.planeFindingMode
        set(value) {
            config.planeFindingMode = value
            configure(config)
        }

    var depthMode: Config.DepthMode
        get() = config.depthMode
        set(value) {
            config.depthMode = value
            configure(config)
        }

    var lightEstimationMode: Config.LightEstimationMode
        get() = config.lightEstimationMode
        set(value) {
            config.lightEstimationMode = value
            configure(config)
        }

    init {
        configure(config.apply {
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            focusMode = Config.FocusMode.AUTO

            depthMode =
                if (isDepthModeSupported(Config.DepthMode.AUTOMATIC)) Config.DepthMode.AUTOMATIC
                else Config.DepthMode.DISABLED

            lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            // getting ar frame doesn't block and gives last frame
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        }.apply(sessionConfig))
        setCameraTextureName(cameraTextureId)
    }

    companion object {
        const val NEAR_CLIP_PLANE_METERS: Float = 0.1f
        const val FAR_CLIP_PLANE_METERS: Float = 500f
    }
}