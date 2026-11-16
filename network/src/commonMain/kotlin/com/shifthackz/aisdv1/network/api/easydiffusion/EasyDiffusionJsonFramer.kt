package com.shifthackz.aisdv1.network.api.easydiffusion

import kotlinx.io.Buffer
import kotlinx.io.readByteArray

/**
 * Frames adjacent JSON objects using ASCII structural bytes, so UTF8 characters split across network
 * reads remain intact until a complete object is decoded. Strings and escapes cannot alter depth.
 * A single object is capped at 32 MiB; consumed progress frames do not accumulate.
 * @author Dmitriy Moroz
 */
internal class EasyDiffusionJsonFramer {
    private val buffer = Buffer()
    private var depth = 0
    private var isInsideString = false
    private var isEscaped = false

    /** Adds one received byte and returns the completed UTF8 object, if any.
     * @param byte Raw UTF8 byte from the HTTP body.
     * @author Dmitriy Moroz */
    fun accept(byte: Byte): String? {
        val ch = (byte.toInt() and 255).toChar()
        if (depth == 0) {
            if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') return null
            require(ch == '{') { "Easy Diffusion returned malformed JSON" }
        }
        check(buffer.size < 32L * 1024 * 1024) { "Easy Diffusion stream object is too large" }
        buffer.writeByte(byte)
        if (isInsideString) {
            when {
                isEscaped -> isEscaped = false
                ch == '\\' -> isEscaped = true
                ch == '"' -> isInsideString = false
            }
        } else {
            when (ch) {
                '"' -> isInsideString = true
                '{', '[' -> depth++
                '}', ']' -> depth--
            }
        }
        require(depth >= 0) { "Easy Diffusion returned malformed JSON" }
        return if (depth == 0) buffer.readByteArray()
            .decodeToString(throwOnInvalidSequence = true) else null
    }

    /** Rejects a truncated object rather than treating a transport EOF as terminal success.
     * @author Dmitriy Moroz */
    fun finish() {
        check(depth == 0 && buffer.size == 0L) { "Easy Diffusion returned truncated JSON" }
    }
}
