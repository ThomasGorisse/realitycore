package com.gorisse.thomas.realitycore.entity

import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.FilamentInstance

/**
 *
 * Owns a bundle of Filament objects that have been created by <code>AssetLoader</code>.
 *
 * <p>For usage instructions, see the documentation for {@link AssetLoader}.</p>
 *
 * <p>This class owns a hierarchy of entities that have been loaded from a glTF asset. Every entity has
 * a <code>TransformManager</code> component, and some entities also have
 * <code>NameComponentManager</code> and/or <code>RenderableManager</code> components.</p>
 *
 * <p>In addition to the aforementioned entities, an asset has strong ownership over a list of
 * <code>VertexBuffer</code>, <code>IndexBuffer</code>, <code>MaterialInstance</code>, and
 * <code>Texture</code>.</p>
 *
 * <p>Clients can use {@link ResourceLoader} to create textures, compute tangent quaternions, and
 * upload data into vertex buffers and index buffers.</p>
 *
 * @see FilamentInstance
 * @see FilamentAsset
 */
typealias Model = FilamentAsset
