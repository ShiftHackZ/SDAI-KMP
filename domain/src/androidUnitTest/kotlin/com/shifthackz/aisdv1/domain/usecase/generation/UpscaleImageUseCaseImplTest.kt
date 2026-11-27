package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ProcessedImage
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.gateway.BenchmarkDeviceProbe
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.gateway.UpscalerGateway
import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.repository.ImageProcessingRepository
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies model admission, provenance retention and faithful inference failures.
 *
 * @author Dmitriy Moroz
 */
class UpscaleImageUseCaseImplTest {
    private val models = mockk<DownloadableModelRepository>()
    private val gateway = mockk<UpscalerGateway>()
    private val images = mockk<ImageProcessingRepository> {
        every { read("original") } returns ProcessedImage("original", 512, 512)
    }
    private val deviceProbe = mockk<BenchmarkDeviceProbe> {
        coEvery { capture() } returns BenchmarkDeviceInfo(
            BenchmarkPlatform.ANDROID, "Google", "Pixel 10 Pro", "Android 17 (37)",
            "CPU", 8, "GPU", 8192, 4096, null, null, emptyList()
        )
    }
    private val useCase =
        UpscaleImageUseCaseImpl(models, gateway, images, deviceProbe, BuildInfoProvider.stub)
    private val configuration = UpscalerConfiguration("upscaler_test")
    private val model = LocalAiModel(
        configuration.modelId, LocalAiModel.Type.Upscaler,
        "ESRGAN", "17 MB", listOf("https://example.org/model.pth"), downloaded = true
    )
    private val original = mockAiGenerationResult.copy(
        id = 0, width = 512, height = 512,
        image = "original", inputImage = "img2img-input", providerKey = "remote-provider"
    )

    @Test
    fun `given upscaling, downloaded dedicated model succeeds, expected enlarged pixels with original provenance`() =
        runTest {
            // Given
            coEvery { models.getById(configuration.modelId) } returns model
            every { models.getModelPath(model) } returns "/owned/model.pth"
            coEvery { gateway.upscale("original", "/owned/model.pth", 512, 512) } returns "enlarged"

            // When
            val result = useCase(original, configuration)

            // Then
            assertEquals("enlarged", result.image)
            assertEquals("img2img-input", result.inputImage)
            assertEquals("remote-provider", result.providerKey)
            assertEquals("ESRGAN", result.upscalerModelName)
            assertEquals(2048, result.width)
            assertEquals(2048, result.height)
            assertEquals(0L, result.id)
        }

    @Test
    fun `given upscaling, provider metadata differs from pixels, expected actual dimensions used for inference and result`() =
        runTest {
            // Given
            every { images.read("original") } returns ProcessedImage("original", 418, 418)
            coEvery { models.getById(configuration.modelId) } returns model
            every { models.getModelPath(model) } returns "/owned/model.pth"
            coEvery { gateway.upscale("original", "/owned/model.pth", 418, 418) } returns "enlarged"

            // When
            val result = useCase(original, configuration)

            // Then
            assertEquals(1672, result.width)
            assertEquals(1672, result.height)
            coVerify(exactly = 1) { gateway.upscale("original", "/owned/model.pth", 418, 418) }
        }

    @Test
    fun `given upscaling, generation checkpoint is selected, expected rejection before native execution`() =
        runTest {
            // Given
            coEvery { models.getById(configuration.modelId) } returns model.copy(type = LocalAiModel.Type.Sdxl)

            // When
            val failure = runCatching { useCase(original, configuration) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { gateway.upscale(any(), any(), any(), any()) }
        }

    @Test
    fun `given upscaling, model download is incomplete, expected rejection before native execution`() =
        runTest {
            // Given
            coEvery { models.getById(configuration.modelId) } returns model.copy(downloaded = false)

            // When
            val failure = runCatching { useCase(original, configuration) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { gateway.upscale(any(), any(), any(), any()) }
        }

    @Test
    fun `given upscaling, source is downscaled for runtime bounds, expected downscaled pixels and actual dimensions sent to inference`() =
        runTest {
            // Given
            coEvery { images.downscale("original", 1024, 1024) } returns ProcessedImage(
                "downscaled",
                1024,
                512
            )
            coEvery { models.getById(configuration.modelId) } returns model
            every { models.getModelPath(model) } returns "/owned/model.pth"
            coEvery {
                gateway.upscale(
                    "downscaled",
                    "/owned/model.pth",
                    1024,
                    512
                )
            } returns "enlarged"

            // When
            val result =
                useCase(original.copy(type = AiGenerationResult.Type.UPSCALE), configuration)

            // Then
            assertEquals(4096, result.width)
            assertEquals(2048, result.height)
            assertEquals("img2img-input", result.inputImage)
            assertEquals("Google Pixel 10 Pro", result.deviceName)
            assertEquals("Android 17 (API 37)", result.osVersion)
            coVerify(exactly = 1) { gateway.upscale("downscaled", "/owned/model.pth", 1024, 512) }
        }

    @Test
    fun `given upscaling, x2 is requested from a native x4 model, expected resized pixels and exact final dimensions`() =
        runTest {
            // Given
            coEvery { models.getById(configuration.modelId) } returns model
            every { models.getModelPath(model) } returns "/owned/model.pth"
            coEvery {
                gateway.upscale(
                    "original",
                    "/owned/model.pth",
                    512,
                    512
                )
            } returns "native-x4"
            coEvery { images.downscale("native-x4", 1024, 1024) } returns ProcessedImage(
                "final-x2",
                1024,
                1024
            )

            // When
            val result = useCase(original, configuration.copy(scaleFactor = 2))

            // Then
            assertEquals("final-x2", result.image)
            assertEquals(1024, result.width)
            assertEquals(1024, result.height)
            assertEquals("remote-provider", result.providerKey)
            coVerify(exactly = 1) { images.downscale("native-x4", 1024, 1024) }
        }

    @Test
    fun `given upscaling, unsupported factor is requested, expected rejection before native inference`() =
        runTest {
            // Given
            val selection = configuration.copy(scaleFactor = 8)

            // When
            val failure = runCatching { useCase(original, selection) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { gateway.upscale(any(), any(), any(), any()) }
        }

    @Test
    fun `given upscaling, native inference is cancelled, expected unchanged cancellation propagation`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled")
            coEvery { models.getById(configuration.modelId) } returns model
            every { models.getModelPath(model) } returns "/owned/model.pth"
            coEvery { gateway.upscale(any(), any(), any(), any()) } throws cancellation

            // When
            val failure = runCatching { useCase(original, configuration) }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
        }
}
