package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Protects platform and completion validation before committing the independent provider selection.
 *
 * @author Dmitriy Moroz
 */
class ConfigureUpscalerUseCaseImplTest {
    private val models = mockk<DownloadableModelRepository>()
    private val build = mockk<BuildInfoProvider>()
    private val preferences = mockk<PreferenceManager>(relaxed = true)
    private val useCase = ConfigureUpscalerUseCaseImpl(models, build, preferences)
    private val model = LocalAiModel(
        "upscaler_mnn_anime",
        LocalAiModel.Type.Upscaler,
        "MNN",
        "17 MB",
        emptyList(),
        downloaded = true
    )

    @Before
    fun allowAndroid() {
        every { build.platform } returns Platform.ANDROID
    }

    @Test
    fun `given upscaler configuration, downloaded dedicated model is selected, expected only independent preference committed`() =
        runTest {
            // Given
            coEvery { models.getById(model.id) } returns model
            // When
            useCase(model.id)
            // Then
            verify(exactly = 1) { preferences.upscalerModelId = model.id }
            verify(exactly = 0) { preferences.localMnnModelId = any() }
        }

    @Test
    fun `given iOS upscaler configuration, ESRGAN is selected, expected rejection before model access or preference write`() =
        runTest {
            // Given
            every { build.platform } returns Platform.IOS
            // When
            val result = runCatching { useCase("upscaler_esrgan") }
            // Then
            assertTrue(result.exceptionOrNull() is IllegalStateException)
            coVerify(exactly = 0) { models.getById(any()) }
            verify(exactly = 0) { preferences.upscalerModelId = any() }
        }

    @Test
    fun `given iOS upscaler configuration, downloaded MNN model is selected, expected supported selection committed`() =
        runTest {
            // Given
            every { build.platform } returns Platform.IOS
            coEvery { models.getById(model.id) } returns model
            // When
            useCase(model.id)
            // Then
            verify(exactly = 1) { preferences.upscalerModelId = model.id }
        }

    @Test
    fun `given upscaler configuration, generation checkpoint is selected, expected rejection with previous preference preserved`() =
        runTest {
            // Given
            coEvery { models.getById(model.id) } returns model.copy(type = LocalAiModel.Type.Mnn)
            // When
            val result = runCatching { useCase(model.id) }
            // Then
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            verify(exactly = 0) { preferences.upscalerModelId = any() }
        }

    @Test
    fun `given upscaler configuration, model is not downloaded, expected rejection without persisting selection`() =
        runTest {
            // Given
            coEvery { models.getById(model.id) } returns model.copy(downloaded = false)
            // When
            val result = runCatching { useCase(model.id) }
            // Then
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            verify(exactly = 0) { preferences.upscalerModelId = any() }
        }

    @Test
    fun `given upscaler configuration, repository is cancelled, expected original cancellation with previous selection preserved`() =
        runTest {
            // Given
            val failure = CancellationException("cancel lookup")
            coEvery { models.getById(model.id) } throws failure
            // When
            val result = runCatching { useCase(model.id) }
            // Then
            assertSame(failure, result.exceptionOrNull())
            verify(exactly = 0) { preferences.upscalerModelId = any() }
        }
}
