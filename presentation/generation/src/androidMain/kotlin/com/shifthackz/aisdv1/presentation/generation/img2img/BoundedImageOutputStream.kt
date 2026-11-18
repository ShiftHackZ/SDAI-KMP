package com.shifthackz.aisdv1.presentation.generation.img2img

import android.util.Base64
import java.io.IOException
import java.io.OutputStream

/**
 * Owns a JPEG compression buffer whose capacity and accepted writes never exceed the output limit.
 * The Base64 encoder reads its populated range directly, without a second JPEG byte-array copy.
 *
 * @param encodedChars Maximum encoded output characters.
 * @param checkActive Cooperative cancellation check invoked before every compression write.
 * @author Dmitriy Moroz
 */
internal class BoundedImageOutputStream(
    private val encodedChars: Int,
    private val checkActive: () -> Unit,
) : OutputStream() {
    private val maxBytes = ((encodedChars.toLong() / 4L) * 3L).toInt()
    private var buffer = ByteArray(minOf(maxBytes, INITIAL_CAPACITY))
    private var count = 0

    init {
        require(encodedChars > 0)
    }

    /**
     * Admits one compressed byte before allocating more storage.
     *
     * @param value Byte emitted by the image compressor.
     * @author Dmitriy Moroz
     */
    override fun write(value: Int) {
        checkActive()
        admit(1)
        buffer[count++] = value.toByte()
    }

    /**
     * Admits a compressed range before growing or copying the backing buffer.
     *
     * @param source Compression bytes.
     * @param offset First source byte.
     * @param length Number of bytes to append.
     * @author Dmitriy Moroz
     */
    override fun write(source: ByteArray, offset: Int, length: Int) {
        require(offset >= 0 && length >= 0 && offset <= source.size && length <= source.size - offset)
        checkActive()
        admit(length)
        source.copyInto(buffer, count, offset, offset + length)
        count += length
    }

    /**
     * Encodes only admitted bytes and checks padded Base64 length before allocation.
     *
     * @author Dmitriy Moroz
     */
    fun encoded(): String {
        checkActive()
        if (count == 0 || ((count.toLong() + 2L) / 3L) * 4L > encodedChars) throw IOException("Image output limit")
        return Base64.encodeToString(buffer, 0, count, Base64.NO_WRAP).also {
            if (it.length > encodedChars) throw IOException("Image output limit")
        }
    }

    /**
     * Releases owned compression storage when conversion succeeds, fails or is cancelled.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        buffer = ByteArray(0); count = 0
    }

    private fun admit(length: Int) {
        val required = count.toLong() + length.toLong()
        if (required > maxBytes.toLong()) throw IOException("Image output limit")
        if (required > buffer.size) {
            val capacity = minOf(
                maxBytes.toLong(),
                maxOf(required, buffer.size.toLong() * 2L, INITIAL_CAPACITY.toLong())
            )
            buffer = buffer.copyOf(capacity.toInt())
        }
    }

    private companion object {
        const val INITIAL_CAPACITY = 8192
    }
}
