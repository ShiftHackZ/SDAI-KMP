package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.presentation.app.bootstrap.createNativeInferenceEngineFactory

import android.app.Application
import com.shifthackz.aisdv1.app.diagnostics.GenerationAttemptJournal
import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Checks the host's MNN dispatch and child-only factory graph without executing native inference.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class, sdk = [28])
class MnnNativeGatewayTest {
    @Test
    fun `given MNN gateway, child completes isolated attempt, expected MNN callbacks and owned output publication`() =
        runTest {
            // Given
            val connection = mockk<InferenceConnection>(relaxed = true) {
                every { processId } returns 4321
            }
            val connections =
                mockk<InferenceConnectionFactory> { every { create() } returns connection }
            val files = mockk<GenerationFileStore>(relaxed = true) {
                every { readResult(any()) } returns byteArrayOf(
                    1,
                    2,
                    3
                )
            }
            val journal = mockk<GenerationAttemptJournal>(relaxed = true)
            val exits = mockk<ProcessExitDiagnosticsGateway>(relaxed = true)
            val gateway =
                NativeGenerationGatewayImpl(connections, files, journal, exits, backgroundScope)
            val request = bonsaiRequest().let {
                it.copy(
                    engine = NativeGenerationEngine.MNN,
                    payload = it.payload.copy(
                        mnnBackend = MnnBackend.OPENCL,
                        mnnModelId = "mnn_sd15",
                        mnnActivationId = "a".repeat(64),
                        mnnProfile = "sd15-512-general-int8-v1"
                    )
                )
            }
            val reading = ProcessPerformanceReading("4321:worker", 123, 456)
            coEvery { connection.readPerformance() } returns reading
            coEvery { connection.close() } returns true
            coEvery {
                connection.run(
                    any(),
                    NativeGenerationEngine.MNN,
                    any(),
                    any(),
                    any()
                )
            } coAnswers {
                assertEquals(reading, gateway.readProcessPerformance())
                arg<(LocalDiffusionStatus) -> Unit>(2)(LocalDiffusionStatus(15, 100))
                arg<(NativeGenerationStage) -> Unit>(3)(NativeGenerationStage.RUNNING)
                assertEquals(
                    LocalDiffusionStatus(15, 100),
                    gateway.observeStatus(NativeGenerationEngine.MNN).first()
                )
                assertEquals(
                    LocalDiffusionStatus(0, 0),
                    gateway.observeStatus(NativeGenerationEngine.ONNX).first()
                )
            }

            // When
            val output = gateway.generate(request)

            // Then
            assertNull(gateway.readProcessPerformance())
            assertEquals("AQID", output)
            assertEquals(
                LocalDiffusionStatus(0, 0),
                gateway.observeStatus(NativeGenerationEngine.MNN).first()
            )
            assertEquals(
                NativeGenerationStage.RUNNING,
                gateway.observeStage(NativeGenerationEngine.MNN).first()
            )
            assertEquals(
                LocalDiffusionStatus(0, 0),
                gateway.observeStatus(NativeGenerationEngine.ONNX).first()
            )
            verify(exactly = 1) { files.writeRequest(any(), request); files.delete(any()) }
            coVerify(exactly = 1) { connection.close() }
            assertFalse(gateway.cancelActive())
        }

    @Test
    fun `given native engine factory, MNN graph is requested, expected independent closable engine without native loading`() {
        // Given
        val request = bonsaiRequest().copy(engine = NativeGenerationEngine.MNN)
        val factory = createNativeInferenceEngineFactory(
            RuntimeEnvironment.getApplication(),
            isDiagnosticsEnabled = false
        )

        // When
        val first = factory.create(request)
        val second = factory.create(request)
        first.close()
        first.close()
        second.close()

        // Then
        assertTrue(first !== second)
    }
}
