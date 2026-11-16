package com.shifthackz.aisdv1.network.api.easydiffusion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Exercises byte-level framing used by the production streaming transport.
 * @author Dmitriy Moroz
 */
class EasyDiffusionJsonFramerTest {
    @Test
    fun `adjacent nested objects and escaped strings survive byte by byte UTF8 input`() {
        val objects = listOf(
            """{"text":"ліс { } [ ] \"quoted\"","nested":[{"step":1}]}""",
            """{"status":"succeeded","output":[]}"""
        )
        val framer = EasyDiffusionJsonFramer()
        val actual = (" \n" + objects.joinToString("") + "\r\t").encodeToByteArray()
            .asSequence().mapNotNull(framer::accept).toList()
        framer.finish()
        assertEquals(objects, actual)
    }

    @Test
    fun `empty whitespace stream is a valid pending response`() {
        val framer = EasyDiffusionJsonFramer()
        assertEquals(
            emptyList<String>(),
            " \n\r\t".encodeToByteArray().asSequence().mapNotNull(framer::accept).toList()
        )
        framer.finish()
    }

    @Test
    fun `HTML arrays and scalar roots are rejected`() {
        listOf("<html>", "[]", "null", "42", "\"text\"").forEach { body ->
            assertThrows(IllegalArgumentException::class.java) {
                body.encodeToByteArray().forEach(EasyDiffusionJsonFramer()::accept)
            }
        }
    }

    @Test
    fun `EOF inside strings escapes and nested structures is rejected`() {
        listOf(
            "{",
            """{"value":"unfinished""",
            """{"value":[{}]""",
            """{"value":"escape\\"""
        ).forEach { body ->
            val framer = EasyDiffusionJsonFramer()
            body.encodeToByteArray().forEach(framer::accept)
            assertThrows(IllegalStateException::class.java, framer::finish)
        }
    }

    @Test
    fun `invalid UTF8 is rejected at object completion`() {
        val framer = EasyDiffusionJsonFramer()
        val bytes = byteArrayOf(123, 34, 97, 34, 58, 34, -1, 34, 125)
        assertThrows(CharacterCodingException::class.java) { bytes.forEach(framer::accept) }
    }

    @Test
    fun `frame size is bounded before accepting an oversized object`() {
        val framer = EasyDiffusionJsonFramer()
        framer.accept('{'.code.toByte())
        repeat(32 * 1024 * 1024 - 1) { framer.accept(' '.code.toByte()) }
        assertThrows(IllegalStateException::class.java) { framer.accept('}'.code.toByte()) }
    }
}
