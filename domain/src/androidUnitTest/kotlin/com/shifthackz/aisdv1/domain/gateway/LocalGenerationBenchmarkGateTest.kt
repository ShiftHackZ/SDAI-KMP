package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.policy.benchmark.LocalGenerationBenchmarkGateImpl
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationGateResult
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkLimitExceeded
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationRequest
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkRecommendation
import com.shifthackz.aisdv1.domain.gateway.BenchmarkRepository
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalGenerationBenchmarkGateTest {

    @Test
    fun `given local generation benchmark gate, remote generation source, expected ready without benchmark prompt`() =
        runTest {
            // Given

            val preferences = preferences(promptAnswered = false)
            val repository = FakeBenchmarkRepository(latest = null)
            val gate = LocalGenerationBenchmarkGateImpl(preferences, repository)

            // When
            val result = gate.evaluate(request(source = ServerSource.AUTOMATIC1111))

            // Then
            assertSame(LocalGenerationGateResult.Ready, result)

        }

    @Test
    fun `given local generation benchmark gate, first local generation without result, expected benchmark prompt`() =
        runTest {
            // Given

            val preferences = preferences(promptAnswered = false)
            val repository = FakeBenchmarkRepository(latest = null)
            val gate = LocalGenerationBenchmarkGateImpl(preferences, repository)

            // When
            val result = gate.evaluate(request())

            // Then
            assertSame(LocalGenerationGateResult.AskRunBenchmark, result)

        }

    @Test
    fun `given local generation benchmark gate, benchmark exists and first prompt is unanswered, expected prompt marked answered`() =
        runTest {
            // Given

            val preferences = preferences(promptAnswered = false)
            val repository = FakeBenchmarkRepository(latest = benchmarkResult())
            val gate = LocalGenerationBenchmarkGateImpl(preferences, repository)

            // When
            val result = gate.evaluate(request())

            // Then
            assertSame(LocalGenerationGateResult.Ready, result)
            verify { preferences.localGenerationBenchmarkPromptAnswered = true }

        }

    @Test
    fun `given local generation benchmark gate, warning suppressed and request exceeds benchmark, expected ready`() =
        runTest {
            // Given

            val preferences = preferences(
                promptAnswered = true,
                warningSuppressed = true,
            )
            val repository = FakeBenchmarkRepository(latest = benchmarkResult())
            val gate = LocalGenerationBenchmarkGateImpl(preferences, repository)

            // When
            val result = gate.evaluate(
                request(
                    width = 512,
                    height = 512,
                    samplingSteps = 30,
                    batchCount = 3,
                    hiresFix = true,
                ),
            )

            // Then
            assertSame(LocalGenerationGateResult.Ready, result)

        }

    @Test
    fun `given local generation benchmark gate, local request exceeds benchmark recommendation, expected confirmation reasons`() =
        runTest {
            // Given

            val preferences = preferences(promptAnswered = true)
            val repository = FakeBenchmarkRepository(latest = benchmarkResult())
            val gate = LocalGenerationBenchmarkGateImpl(preferences, repository)

            // When
            val result = gate.evaluate(
                request(
                    width = 512,
                    height = 512,
                    samplingSteps = 30,
                    batchCount = 3,
                    hiresFix = true,
                ),
            )

            // Then
            assertTrue(result is LocalGenerationGateResult.ConfirmExceedsRecommendation)
            val confirmation = result as LocalGenerationGateResult.ConfirmExceedsRecommendation
            assertEquals(
                listOf(
                    BenchmarkLimitExceeded.IMAGE_SIZE,
                    BenchmarkLimitExceeded.SAMPLING_STEPS,
                    BenchmarkLimitExceeded.BATCH_COUNT,
                    BenchmarkLimitExceeded.HIRES_FIX,
                ),
                confirmation.reasons,
            )

        }

    @Test
    fun `given local generation benchmark gate, provider is not recommended, expected provider confirmation`() =
        runTest {
            // Given

            val preferences = preferences(promptAnswered = true)
            val repository = FakeBenchmarkRepository(latest = benchmarkResult())
            val gate = LocalGenerationBenchmarkGateImpl(preferences, repository)

            // When
            val result = gate.evaluate(request(source = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE))

            // Then
            assertTrue(result is LocalGenerationGateResult.ConfirmExceedsRecommendation)
            val confirmation = result as LocalGenerationGateResult.ConfirmExceedsRecommendation
            assertEquals(listOf(BenchmarkLimitExceeded.PROVIDER), confirmation.reasons)

        }

    private fun preferences(
        promptAnswered: Boolean,
        warningSuppressed: Boolean = false,
    ): PreferenceManager {
        var localPromptAnswered = promptAnswered
        var localWarningSuppressed = warningSuppressed
        return mockk<PreferenceManager>(relaxed = true).apply {
            every { localGenerationBenchmarkPromptAnswered } answers { localPromptAnswered }
            every { localGenerationBenchmarkPromptAnswered = any() } answers {
                localPromptAnswered = firstArg()
            }
            every { benchmarkRecommendationWarningSuppressed } answers { localWarningSuppressed }
            every { benchmarkRecommendationWarningSuppressed = any() } answers {
                localWarningSuppressed = firstArg()
            }
        }
    }

    private fun request(
        source: ServerSource = ServerSource.LOCAL_MICROSOFT_ONNX,
        width: Int = 256,
        height: Int = 256,
        samplingSteps: Int = 8,
        batchCount: Int = 1,
        hiresFix: Boolean = false,
    ) = LocalGenerationRequest(
        source = source,
        width = width,
        height = height,
        samplingSteps = samplingSteps,
        batchCount = batchCount,
        hiresFix = hiresFix,
    )

    private fun benchmarkResult() = BenchmarkResult(
        createdAt = 1L,
        deviceInfo = BenchmarkDeviceInfo(
            platform = BenchmarkPlatform.ANDROID,
            manufacturer = "Google",
            model = "Pixel 3a XL",
            osVersion = "Android 12",
            cpuName = "SDM670",
            cpuCores = 8,
            gpuName = "Adreno 615",
            totalRamMb = 3_580,
            availableRamMb = 1_000,
            totalVramMb = null,
            availableVramMb = null,
            accelerators = emptyList(),
        ),
        cpuScore = 1_000,
        memoryScore = 900,
        acceleratorScore = 1_100,
        totalScore = 1_200,
        estimatedTimeSeconds = 270,
        recommendation = recommendation(),
        providerRecommendations = listOf(
            BenchmarkProviderRecommendation(
                provider = ServerSource.LOCAL_MICROSOFT_ONNX,
                recommended = true,
                width = 256,
                height = 256,
                samplingSteps = 8,
                cfgScale = 6f,
                batchCount = 1,
                estimatedTimeSeconds = 270,
                backgroundGeneration = false,
                sdxlBackend = SdxlBackend.CPU,
            ),
            BenchmarkProviderRecommendation(
                provider = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                recommended = false,
                width = 0,
                height = 0,
                samplingSteps = 0,
                cfgScale = 0f,
                batchCount = 1,
                estimatedTimeSeconds = 0,
                backgroundGeneration = false,
                sdxlBackend = SdxlBackend.CPU,
            ),
        ),
    )

    private fun recommendation() = BenchmarkRecommendation(
        width = 256,
        height = 256,
        samplingSteps = 8,
        cfgScale = 6f,
        batchCount = 1,
        providers = listOf(ServerSource.LOCAL_MICROSOFT_ONNX),
        backgroundGeneration = false,
        sdxlBackend = SdxlBackend.CPU,
    )

    private class FakeBenchmarkRepository(
        latest: BenchmarkResult?,
    ) : BenchmarkRepository {
        private val resultFlow = MutableStateFlow(latest)
        private var latestResult = latest

        override fun observeLatest(): Flow<BenchmarkResult?> = resultFlow

        override suspend fun getLatest(): BenchmarkResult? = latestResult

        override suspend fun save(result: BenchmarkResult): BenchmarkResult {
            latestResult = result
            resultFlow.value = result
            return result
        }
    }
}
