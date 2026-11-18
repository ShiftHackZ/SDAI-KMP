package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import kotlinx.coroutines.test.runTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies rejection before native loading and harmless interruption without an admitted attempt.
 * JNI inference itself is exercised by the platform application, not by these JVM tests.
 *
 * @author Dmitriy Moroz
 */
class MnnNativeBridgeImplTest {
    private val cache = mockk<MnnCacheOwner>()
    private val models = mockk<MnnModelLeaseOwner>()

    @Test
    fun `given MNN bridge, child model ownership fails, expected same failure before workspace or native loading`() =
        runTest {
            // Given
            val payload = mockk<TextToImagePayload> {
                every { mnnActivationId } returns "a".repeat(64)
                every { mnnBackend } returns MnnBackend.CPU
            }
            val request =
                NativeGenerationRequest(NativeGenerationEngine.MNN, payload, "/owned/mnn_model")
            val failure = IllegalStateException("model is busy")
            every { models.acquire("/owned/mnn_model", "a".repeat(64)) } throws failure
            val bridge = MnnNativeBridgeImpl(cache, models)

            // When
            val actual = runCatching { bridge.generate(request) {} }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            verify(exactly = 0) { cache.acquire() }
        }

    @Test
    fun `given MNN bridge, interruption precedes first request, expected no model or workspace acquisition`() {
        // Given
        val bridge = MnnNativeBridgeImpl(cache, models)

        // When
        bridge.interrupt()

        // Then
        verify(exactly = 0) { models.acquire(any(), any()); cache.acquire() }
    }
}
