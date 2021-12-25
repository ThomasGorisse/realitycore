package com.gorisse.thomas.realitycore.utils

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

suspend fun <R> AssetManager.loadAsset(filename: String, create: suspend (ByteBuffer) -> R, destroy: ((R?) -> Unit)?): R = withContext(Dispatchers.IO) {
    val asset: R? =null
    try {
        open(filename).use { inputStream ->
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            val byteBuffer = ByteBuffer.wrap(bytes)
            withContext(Dispatchers.Main) {
                create(byteBuffer)
            }
        }
    } finally {
        destroy?.invoke(asset)
    }
}

fun <R> InputStream.useBuffer(block: (ByteBuffer) -> R): R =
    use { inputStream ->
        val bytes = ByteArray(inputStream.available())
        inputStream.read(bytes)
        block(ByteBuffer.wrap(bytes))
    }

suspend fun <R> AssetManager.useBufferAsync(filename: String, block: suspend (ByteBuffer) -> R): R =
    withContext(Dispatchers.IO) { open(filename).useBufferAsync(block) }

suspend fun <R> InputStream.useBufferAsync(block: suspend (ByteBuffer) -> R): R =
    use { inputStream ->
        withContext(Dispatchers.IO) {
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            block(ByteBuffer.wrap(bytes))
        }
    }

fun <R> InputStream.useText(block: (String) -> R): R = use { inputStream ->
    inputStream.bufferedReader().use {
        block(it.readText())
    }
}

fun readIntLE(input: InputStream): Int {
    return input.read() and 0xff or (
            input.read() and 0xff shl 8) or (
            input.read() and 0xff shl 16) or (
            input.read() and 0xff shl 24)
}

fun readFloat32LE(input: InputStream): Float {
    val bytes = ByteArray(4)
    input.read(bytes, 0, 4)
    return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).float
}

fun readUIntLE(input: InputStream): Long {
    return readIntLE(input).toLong() and 0xFFFFFFFFL
}

inline val Float.Companion.size get() = java.lang.Float.BYTES

fun FloatArray.toFloatBuffer(): FloatBuffer = ByteBuffer
    .allocateDirect(size * Float.size)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer()
    .also { floatBuffer ->
        floatBuffer.put(this)
        floatBuffer.rewind()
    }

fun ShortArray.toShortBuffer(): ShortBuffer = ShortBuffer
    .allocate(size)
    .also { shortBuffer ->
        shortBuffer.put(this)
        shortBuffer.rewind()
    }