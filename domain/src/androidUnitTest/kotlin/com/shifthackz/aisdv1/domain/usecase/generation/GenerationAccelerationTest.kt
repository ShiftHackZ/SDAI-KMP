package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.domain.mocks.mockTextToImagePayload
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Protects recorded execution-mode names from provider or preference changes after submission.
 *
 * @author Dmitriy Moroz
 */
class GenerationAccelerationTest {
    @Test
    fun `given captured Android local backend selections, metadata is resolved, expected provider specific execution modes`() {
        // Given
        val payload = mockTextToImagePayload.copy(
            mnnBackend = MnnBackend.OPENCL,
            sdxlBackend = SdxlBackend.VULKAN,
            bonsaiBackend = BonsaiBackend.CPU
        )
        val expected = mapOf(
            ServerSource.LOCAL_MICROSOFT_ONNX to "CPU",
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE to "GPU",
            ServerSource.LOCAL_MNN to "OpenCL",
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP to "Vulkan",
            ServerSource.LOCAL_APPLE_BONSAI to "CPU"
        )
        // When
        val actual = expected.keys.associateWith(payload::generationAcceleration)
        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `given remote providers with local backend fields present, provenance is captured, expected None acceleration for every remote provider`() {
        // Given
        val payload = mockTextToImagePayload.copy(mnnBackend = MnnBackend.OPENCL)
        val sources = ServerSource.entries.filter { it.type != ServerSourceType.LOCAL }
        // When
        val modes = sources.map(payload::generationAcceleration)
        // Then
        assertEquals(List(sources.size) { "None" }, modes)
    }

    @Test
    fun `given MNN CPU and Metal selections, provenance is captured, expected stable display names`() {
        // Given
        val cpu = mockTextToImagePayload.copy(mnnBackend = MnnBackend.CPU)
        val metal = mockTextToImagePayload.copy(mnnBackend = MnnBackend.METAL)
        // When
        val modes = listOf(cpu, metal).map { it.generationAcceleration(ServerSource.LOCAL_MNN) }
        // Then
        assertEquals(listOf("CPU", "Metal"), modes)
    }
}
