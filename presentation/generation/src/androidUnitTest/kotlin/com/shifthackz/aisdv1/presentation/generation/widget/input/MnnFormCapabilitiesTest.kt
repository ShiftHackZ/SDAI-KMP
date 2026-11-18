package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.presentation.components.model.readinessFor
import com.shifthackz.aisdv1.domain.entity.isServerSourceAvailableOnAndroid
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks the actual form's fixed-profile field selection and supported-device provider policy.
 *
 * @author Dmitriy Moroz
 */
class MnnFormCapabilitiesTest {
    @Test
    fun `given MNN form, normal generation is displayed, expected backend and prompt without unsupported controls`() {
        // Given
        val state = TextToImageState(mode = ServerSource.LOCAL_MNN, mnnBackend = MnnBackend.OPENCL)

        // When
        val fields = generationInputBaseFields(state)

        // Then
        assertTrue(GenerationBaseFormField.PROVIDER in fields)
        assertTrue(GenerationBaseFormField.MNN_BACKEND in fields)
        assertTrue(GenerationBaseFormField.PROMPT in fields)
        assertFalse(GenerationBaseFormField.NEGATIVE_PROMPT in fields)
        assertFalse(GenerationBaseFormField.DIMENSIONS in fields)
        assertEquals(MnnBackend.OPENCL, state.mnnBackend)
    }

    @Test
    fun `given MNN provider, Android arm64 is present, expected Alpha readiness and available runtime`() {
        // Given
        val source = ServerSource.LOCAL_MNN

        // When
        val isAvailable = isServerSourceAvailableOnAndroid(source, arrayOf("arm64-v8a"))

        // Then
        assertTrue(isAvailable)
        assertEquals(ServerSourceReadiness.ALPHA, source.readinessFor(Platform.ANDROID))
    }

    @Test
    fun `given MNN provider, Android lacks arm64, expected unavailable runtime`() {
        // Given
        val source = ServerSource.LOCAL_MNN

        // When
        val isAvailable = isServerSourceAvailableOnAndroid(source, arrayOf("x86_64", "armeabi-v7a"))

        // Then
        assertFalse(isAvailable)
    }

    @Test
    fun `given MNN advanced form, controls are expanded, expected seed and steps without guidance sampler or batch`() {
        // Given
        val state = TextToImageState(mode = ServerSource.LOCAL_MNN, advancedOptionsVisible = true)

        // When
        val fields = generationInputAdvancedFields(state.toAdvancedOptionsState(), false)

        // Then
        assertTrue(GenerationFormField.SEED in fields)
        assertTrue(GenerationFormField.SAMPLING_STEPS in fields)
        assertFalse(GenerationFormField.GUIDANCE_SCALE in fields)
        assertFalse(GenerationFormField.SAMPLER in fields)
        assertFalse(GenerationFormField.BATCH in fields)
    }
}
