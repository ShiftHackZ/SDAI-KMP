package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import java.io.ByteArrayInputStream
import java.io.SequenceInputStream
import java.nio.ByteBuffer
import java.util.Collections
import java.util.zip.CRC32
import java.util.zip.InflaterInputStream

/**
 * Checks chunk CRCs, terminal IEND and the entire bounded scanline stream without a bitmap copy.
 * Internal encoders produce non-interlaced PNG; unsupported encoding is rejected before gallery IO.
 *
 * @author Dmitriy Moroz
 */
internal class CompletePngImageValidator : PngImageValidator {
    /**
     * Checks the complete PNG structure and decoded-size policy before result publication.
     * Invalid, truncated, unsupported or over-limit output throws instead of reaching gallery storage.
     *
     * @param bytes Encoded PNG.
     * @param request Expected native output parameters.
     * @author Dmitriy Moroz
     */
    override fun validate(bytes: ByteArray, request: NativeGenerationRequest) {
        require(bytes.size >= 33 && bytes.copyOfRange(0, 8).contentEquals(SIGNATURE))
        val buffer = ByteBuffer.wrap(bytes)
        var offset = 8
        var expectedBytes = 0L
        var hasHeader = false
        var hasEnd = false
        var hasClosedData = false
        val data = mutableListOf<ByteArrayInputStream>()
        while (offset < bytes.size) {
            require(bytes.size - offset >= 12)
            val length = buffer.getInt(offset)
            require(length >= 0 && length.toLong() <= bytes.size.toLong() - offset - 12)
            val type = String(bytes, offset + 4, 4, Charsets.US_ASCII)
            val crc = CRC32().apply { update(bytes, offset + 4, length + 4) }.value
            require(crc == (buffer.getInt(offset + 8 + length).toLong() and 0xffffffffL))
            if (!hasHeader) require(type == "IHDR")
            when (type) {
                "IHDR" -> {
                    require(!hasHeader && length == 13)
                    hasHeader = true
                    val width = buffer.getInt(offset + 8)
                    val height = buffer.getInt(offset + 12)
                    require(width in 1..4096 && height in 1..4096 && width.toLong() * height <= 16_777_216L)
                    if (request.engine != NativeGenerationEngine.MEDIA_PIPE) require(width == request.payload.width && height == request.payload.height)
                    val depth = bytes[offset + 16].toInt() and 255
                    val channels = when (bytes[offset + 17].toInt() and 255) {
                        0 -> 1; 2 -> 3; 3 -> 1; 4 -> 2; 6 -> 4; else -> error("Unsupported PNG color type")
                    }
                    require(depth in setOf(1, 2, 4, 8, 16))
                    require(bytes[offset + 18] == 0.toByte() && bytes[offset + 19] == 0.toByte() && bytes[offset + 20] == 0.toByte())
                    expectedBytes = ((width.toLong() * channels * depth + 7) / 8 + 1) * height
                }

                "IDAT" -> {
                    require(!hasClosedData)
                    data.add(ByteArrayInputStream(bytes, offset + 8, length))
                }

                "IEND" -> {
                    require(length == 0 && data.isNotEmpty()); hasEnd = true
                }

                else -> if (data.isNotEmpty()) hasClosedData = true
            }
            offset += length + 12
            if (hasEnd) {
                require(offset == bytes.size); break
            }
        }
        require(hasHeader && hasEnd && data.isNotEmpty())
        val inputs = SequenceInputStream(Collections.enumeration(data))
        InflaterInputStream(inputs).use { stream ->
            val chunk = ByteArray(8192)
            var total = 0L
            while (true) {
                val count = stream.read(chunk)
                if (count < 0) break
                total += count
                require(total <= expectedBytes)
            }
            require(total == expectedBytes)
        }
    }

    private companion object {
        val SIGNATURE = byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)
    }
}
