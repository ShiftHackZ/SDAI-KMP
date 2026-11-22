package com.shifthackz.aisdv1.feature.civitai

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class CivitaiDescriptionTest {
    @Test
    fun `given recorded image provenance, description is generated, expected SDAI website and original device settings included`() {
        // Given
        val result = civitaiTestResult
        // When
        val text = CivitaiDescriptionImpl()(result)
        // Then
        listOf(
            "<strong>SDAI</strong>",
            "https://sdai.moroz.cc",
            "Original iPhone",
            "iOS 27",
            "Metal",
            "Recorded model",
            "local_mnn",
            "1234 ms",
            "512x768",
            "Euler",
            "42"
        ).forEach { assertTrue(text.contains(it)) }
        assertFalse(text.contains(result.inputImage))
    }

    @Test
    fun `given prompt with HTML syntax, description is generated, expected escaped public text`() {
        // Given
        val result = civitaiTestResult.copy(prompt = "<script>&test</script>")
        // When
        val text = CivitaiDescriptionImpl()(result)
        // Then
        assertTrue(text.contains("&lt;script&gt;&amp;test&lt;/script&gt;"))
        assertFalse(text.contains("<script>"))
    }

    @Test
    fun `given legacy image without provenance, description is generated, expected unavailable values omitted`() {
        // Given
        val result = civitaiTestResult.copy(
            deviceName = null,
            osVersion = null,
            providerKey = null,
            acceleration = null,
            generationDurationMillis = null
        )
        // When
        val text = CivitaiDescriptionImpl()(result)
        // Then
        assertFalse(text.contains("null"))
        assertFalse(text.contains("Phone / device:"))
        assertFalse(text.contains("Generation provider:"))
    }
}
