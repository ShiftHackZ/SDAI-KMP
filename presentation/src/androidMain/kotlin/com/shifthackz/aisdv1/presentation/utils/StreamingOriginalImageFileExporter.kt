package com.shifthackz.aisdv1.presentation.utils

import android.util.Base64
import android.util.Base64InputStream
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.presentation.widget.image.GalleryMemoryBudget
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Streams Base64 to an owned file using an 8 KiB buffer and injected size limits.
 * The decoded byte limit is three quarters of the encoded-character budget.
 *
 * @param descriptor Private file-provider cache directory.
 * @param budget Explicit encoded/decoded export size bounds.
 * @author Dmitriy Moroz
 */
internal class StreamingOriginalImageFileExporter(
    private val descriptor: FileProviderDescriptor,
    private val budget: GalleryMemoryBudget
) : OriginalImageFileExporter {
    /**
     * Streams admitted Base64 original bytes to a cache file without bitmap re-encoding.
     * The signature selects the PNG/JPEG/WebP extension; failures and cancellation remove partial output.
     * Complete image structure is not validated by this byte-preserving exporter.
     *
     * @param base64 Original bytes encoded as Base64.
     * @param name Safe application-owned output stem.
     * @author Dmitriy Moroz
     */
    override suspend fun export(base64: String, name: String): File {
        require(name.matches(Regex("[a-zA-Z0-9_-]{1,100}")))
        if (base64.length > budget.encodedChars) throw IllegalStateException()
        val offset = base64.indexOf("base64,").let { if (it >= 0) it + 7 else 0 }
        val source = object : InputStream() {
            private var index = offset
            override fun read(): Int =
                if (index >= base64.length) -1 else base64[index++].code.also { require(it < 128) }

            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                if (index >= base64.length) return -1
                val count = minOf(length, base64.length - index)
                for (i in 0 until count) buffer[offset + i] = read().toByte()
                return count
            }
        }
        var output: File? = null
        try {
            Base64InputStream(source, Base64.DEFAULT).use { decoded ->
                val header = ByteArray(12)
                var headerSize = 0
                while (headerSize < header.size) {
                    val count = decoded.read(header, headerSize, header.size - headerSize)
                    if (count < 0) break
                    headerSize += count
                }
                val extension = when {
                    headerSize >= 8 && header.take(8) == listOf(
                        137,
                        80,
                        78,
                        71,
                        13,
                        10,
                        26,
                        10
                    ).map(Int::toByte) -> "png"

                    headerSize >= 3 && header[0] == 0xff.toByte() && header[1] == 0xd8.toByte() && header[2] == 0xff.toByte() -> "jpg"
                    headerSize == 12 && String(header, 0, 4, Charsets.US_ASCII) == "RIFF" && String(
                        header,
                        8,
                        4,
                        Charsets.US_ASCII
                    ) == "WEBP" -> "webp"

                    else -> throw IllegalStateException()
                }
                val directory = File(descriptor.imagesCacheDirPath).apply { mkdirs() }
                val file = File(directory, "$name.$extension")
                output = file
                file.outputStream().buffered().use { target ->
                    target.write(header, 0, headerSize)
                    val buffer = ByteArray(8192)
                    var total = headerSize
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = decoded.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > budget.encodedChars.toLong() * 3 / 4) throw IllegalStateException()
                        target.write(buffer, 0, count)
                    }
                }
                return file
            }
        } catch (failure: Throwable) {
            output?.delete(); throw failure
        }
    }
}
