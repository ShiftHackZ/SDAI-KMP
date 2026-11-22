package com.shifthackz.aisdv1.domain.entity

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class CivitaiPostUrlTest {
    @Test
    fun `given canonical public post URLs, links are validated, expected HTTPS Civitai posts accepted`() {
        // Given
        val urls = listOf("https://civitai.com/posts/1", "https://civitai.com/posts/1234567890")
        // When
        val admitted = urls.map { it.isCivitaiPostUrl() }
        // Then
        assertTrue(admitted.all { it })
    }

    @Test
    fun `given unsafe or nonpost URLs, links are validated, expected no browser action admitted`() {
        // Given
        val urls = listOf(
            "http://civitai.com/posts/1",
            "https://civitai.com.evil/posts/1",
            "https://evil@civitai.com/posts/1",
            "javascript:alert(1)",
            "file:///private/image",
            "https://civitai.com/posts/0",
            "https://civitai.com/posts/-1",
            "https://civitai.com/models/1",
            "https://civitai.com/posts/1\n"
        )
        // When
        val admitted = urls.map { it.isCivitaiPostUrl() }
        // Then
        assertFalse(admitted.any { it })
    }
}
