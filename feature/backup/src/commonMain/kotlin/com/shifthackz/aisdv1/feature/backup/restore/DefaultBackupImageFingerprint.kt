package com.shifthackz.aisdv1.feature.backup.restore

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okio.Buffer
import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.use
import kotlin.io.encoding.Base64
import kotlin.io.encoding.Base64.PaddingOption

/**
 * Hashes decoded Base64 chunks so alternate valid text representations still identify the same saved image.
 * At most one 64KiB UTF8 chunk and its decoded bytes are held in addition to the caller's data.
 *
 * @author Dmitriy Moroz
 */
internal class DefaultBackupImageFingerprint : BackupImageFingerprint {

    override suspend fun invoke(readChunk: suspend (Long, Int) -> ByteArray?): String? {
        val hashing = HashingSink.sha256(blackholeSink())
        var hasBytes = false
        hashing.buffer().use { sink ->
            var offset = 0L
            var pending = ""
            val utf8 = Buffer()
            while (true) {
                currentCoroutineContext().ensureActive()
                val chunk = readChunk(offset, CHUNK_BYTES) ?: return null
                if (chunk.isEmpty()) {
                    if (!utf8.exhausted()) return null
                    break
                }
                offset += chunk.size
                utf8.write(chunk)
                val normalized = buildString(pending.length + chunk.size) {
                    append(pending)
                    while (!utf8.exhausted()) {
                        val lead = utf8[0].toInt() and 0xff
                        val byteCount = when {
                            lead < 0x80 -> 1
                            lead and 0xe0 == 0xc0 -> 2
                            lead and 0xf0 == 0xe0 -> 3
                            lead and 0xf8 == 0xf0 -> 4
                            else -> return null
                        }
                        if (utf8.size < byteCount) break
                        val codePoint = utf8.readUtf8CodePoint()
                        if (codePoint > Char.MAX_VALUE.code) return null
                        val character = codePoint.toChar()
                        if (!character.isWhitespace()) append(
                            when (character) {
                                '-' -> '+'
                                '_' -> '/'
                                else -> character
                            }
                        )
                    }
                }
                // Retain the final quartet until EOF; padding is valid only at the end of the whole image.
                val completeChars = ((normalized.length - 4).coerceAtLeast(0) / 4) * 4
                val complete = normalized.substring(0, completeChars)
                if ('=' in complete) return null
                val decoded = decode(complete) ?: return null
                sink.write(decoded)
                hasBytes = hasBytes || decoded.isNotEmpty()
                pending = normalized.substring(completeChars)
            }
            val decoded = decode(pending) ?: return null
            sink.write(decoded)
            hasBytes = hasBytes || decoded.isNotEmpty()
        }
        return hashing.hash.hex().takeIf { hasBytes }
    }

    /**
     * Treats malformed image text as unidentifiable without masking storage or cancellation failures.
     *
     * @param value Complete Base64 quartets or the final optionally padded group.
     * @author Dmitriy Moroz
     */
    private fun decode(value: String): ByteArray? = try {
        Base64.Default.withPadding(PaddingOption.PRESENT_OPTIONAL).decode(value)
    } catch (_: IllegalArgumentException) {
        null
    }

    private companion object {
        const val CHUNK_BYTES = 65536
    }
}
