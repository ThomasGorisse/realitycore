package com.gorisse.thomas.realitycore.filament

import android.content.res.AssetManager
import android.graphics.Color
import com.google.android.filament.Colors
import com.google.android.filament.Material
import com.google.android.filament.Texture
import com.google.android.filament.TextureSampler
import com.google.android.filament.TextureSampler.*
import com.gorisse.thomas.realitycore.utils.loadAsset

/**
 * ### Load material data. The material data is a binary blob produced by matc.
 *
 * @param fileName the .filamat file
 * @param apply apply custom [Material.Builder] settings
 */
suspend fun AssetManager.loadMaterial(
    fileName: String,
    apply: Material.Builder.() -> Unit = {}
): Material = loadAsset(fileName, create = { buffer ->
    Material.Builder().payload(buffer, buffer.remaining())
        .apply(apply)
        .build(filamentEngine)
}, destroy = { it?.destroy() })

/**
 * ### Sets the color of the given parameter on this material's default instance.
 *
 * @param name the name of the material color parameter
 * @param color the color of type Colors.RgbaType.LINEAR with possible alpha component
 *
 * @see com.google.android.filament.Material.getDefaultInstance
 */
fun Material.setDefaultColor(name: String, color: Int) = setDefaultParameter(
    name,
    Colors.RgbaType.SRGB,
    Color.red(color).toFloat() / 255.0f,
    Color.green(color).toFloat() / 255.0f,
    Color.blue(color).toFloat() / 255.0f,
    Color.alpha(color).toFloat() / 255.0f
)

/**
 * ### Sets a texture and sampler parameter on this material's default instance.
 *
 * @param name The name of the material texture parameter
 * @param texture The texture to set as parameter
 * @param sampler The sampler to be used with this texture
 *
 * @see com.google.android.filament.Material.getDefaultInstance
 */
fun Material.setDefaultTexture(name: String, texture: Texture, sampler: TextureSampler = TextureSampler(MinFilter.LINEAR_MIPMAP_LINEAR, MagFilter.LINEAR, WrapMode.REPEAT)) {
    val filamentSampler = TextureSampler().apply {
        minFilter =
            when (texture.sampler.minFilter) {
                Texture.Sampler.MinFilter.NEAREST -> TextureSampler.MinFilter.NEAREST
                Texture.Sampler.MinFilter.LINEAR -> TextureSampler.MinFilter.LINEAR
                Texture.Sampler.MinFilter.NEAREST_MIPMAP_NEAREST -> TextureSampler.MinFilter.NEAREST_MIPMAP_NEAREST
                Texture.Sampler.MinFilter.LINEAR_MIPMAP_NEAREST -> TextureSampler.MinFilter.LINEAR_MIPMAP_NEAREST
                Texture.Sampler.MinFilter.NEAREST_MIPMAP_LINEAR -> TextureSampler.MinFilter.NEAREST_MIPMAP_LINEAR
                Texture.Sampler.MinFilter.LINEAR_MIPMAP_LINEAR -> TextureSampler.MinFilter.LINEAR_MIPMAP_LINEAR
                else -> throw java.lang.IllegalArgumentException("Invalid MinFilter")
            }
    }
    Texture.Builder()
    setDefaultParameter(name, texture.filamentTexture, filamentSampler)
}

/**
 * ### Destroys a [Material] and frees all its associated resources.
 *
 * All [com.google.android.filament.MaterialInstance] of the specified [Material] must be destroyed before
 * destroying it; if some [MaterialInstance] remain, this method fails silently.
 */
fun Material.destroy() {
    filamentEngine.destroyMaterial(this)
}

