package com.shifthackz.aisdv1.presentation.configuration.upscaler

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.LocalModelCatalogResult
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.feature.work.ModelDownloadManager
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.RefreshLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConfigureUpscalerUseCase
import com.shifthackz.aisdv1.presentation.components.service.storage.StorageUsageObserver
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Covers independent provider setup, error recovery, owned downloads and deletion confirmation.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UpscalerSetupViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val refresh = mockk<RefreshLocalModelCatalogUseCase>()
    private val cached = mockk<GetCachedLocalModelCatalogUseCase>()
    private val configure = mockk<ConfigureUpscalerUseCase>()
    private val delete = mockk<DeleteModelUseCase>()
    private val batches = MutableStateFlow<List<ModelDownloadBatch>>(emptyList())
    private val downloads = mockk<ModelDownloadManager>(relaxed = true)
    private val preferences = mockk<PreferenceManager>()
    private val build = mockk<BuildInfoProvider>()
    private val router = mockk<ServerSetupRouter>(relaxed = true)
    private val storage = mockk<StorageUsageObserver>(relaxed = true)
    private val stores = mutableListOf<ViewModelStore>()
    private val esrgan = LocalAiModel(
        "upscaler_esrgan_anime",
        LocalAiModel.Type.Upscaler,
        "ESRGAN",
        "17 MB",
        listOf("https://models.example/model.pth"),
        downloaded = true
    )
    private val mnn = esrgan.copy(id = "upscaler_mnn_anime", name = "MNN")

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { build.platform } returns Platform.ANDROID
        every { preferences.upscalerModelId } returns esrgan.id
        every { downloads.batches } returns batches
        coEvery { refresh(LocalAiModel.Type.Upscaler) } returns LocalModelCatalogResult(
            listOf(
                esrgan,
                mnn
            )
        )
        coEvery { cached(LocalAiModel.Type.Upscaler) } returns listOf(esrgan, mnn)
        coEvery { configure(any()) } returns Unit
        coEvery { delete(any()) } returns Unit
    }

    @After
    fun cleanup() {
        stores.forEach(ViewModelStore::clear)
        Dispatchers.resetMain()
    }

    @Test
    fun `given upscaler setup, refresh returns cached choices with a failure, expected usable models and retry status`() =
        runTest {
            // Given
            coEvery { refresh(LocalAiModel.Type.Upscaler) } returns LocalModelCatalogResult(
                listOf(
                    esrgan
                ), IllegalStateException("offline")
            )
            val vm = create()
            // When
            advanceUntilIdle()
            // Then
            assertFalse(vm.state.value.isLoading)
            assertEquals(listOf(esrgan), vm.state.value.models)
            assertEquals(LocalCatalogStatus.ERROR, vm.state.value.catalogStatus)
            assertEquals(esrgan.id, vm.state.value.selectedId)
        }

    @Test
    fun `given iOS upscaler setup, unsupported ESRGAN selection arrives, expected MNN draft preserved`() =
        runTest {
            // Given
            every { build.platform } returns Platform.IOS
            every { preferences.upscalerModelId } returns mnn.id
            val vm = create()
            advanceUntilIdle()
            // When
            vm.processIntent(UpscalerSetupIntent.SelectSource(UpscalerSource.LOCAL_ESRGAN))
            // Then
            assertEquals(UpscalerSource.LOCAL_MNN, vm.state.value.source)
            assertEquals(mnn.id, vm.state.value.selectedId)
        }

    @Test
    fun `given upscaler setup, provider is changed, expected first downloaded owning model selected without saving preferences`() =
        runTest {
            // Given
            val vm = create()
            advanceUntilIdle()
            // When
            vm.processIntent(UpscalerSetupIntent.SelectSource(UpscalerSource.LOCAL_MNN))
            // Then
            assertEquals(mnn.id, vm.state.value.selectedId)
            verify(exactly = 0) { preferences.upscalerModelId = any() }
        }

    @Test
    fun `given upscaler setup, validated selection is saved, expected configure invoked and caller restored`() =
        runTest {
            // Given
            val vm = create()
            advanceUntilIdle()
            // When
            vm.processIntent(UpscalerSetupIntent.Save)
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { configure(esrgan.id) }
            verify(exactly = 1) { router.navigateBack() }
        }

    @Test
    fun `given upscaler setup, configuration fails, expected error without leaving wizard`() =
        runTest {
            // Given
            coEvery { configure(any()) } throws IllegalStateException("selection unavailable")
            val vm = create()
            advanceUntilIdle()
            // When
            vm.processIntent(UpscalerSetupIntent.Save)
            advanceUntilIdle()
            // Then
            assertEquals("selection unavailable", vm.state.value.error)
            verify(exactly = 0) { router.navigateBack() }
        }

    @Test
    fun `given upscaler setup, missing model action is pressed, expected dedicated download request using first source`() =
        runTest {
            // Given
            coEvery { refresh(LocalAiModel.Type.Upscaler) } returns LocalModelCatalogResult(
                listOf(
                    esrgan.copy(downloaded = false)
                )
            )
            val vm = create()
            advanceUntilIdle()
            // When
            vm.processIntent(UpscalerSetupIntent.ModelAction(esrgan.id))
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) {
                downloads.start(
                    ModelDownloadRequest(
                        LocalAiModel.Type.Upscaler,
                        esrgan.id,
                        esrgan.sources.first()
                    ), any()
                )
            }
        }

    @Test
    fun `given upscaler setup, running model action is pressed, expected existing transfer cancelled instead of restarted`() =
        runTest {
            // Given
            val vm = create()
            advanceUntilIdle()
            batches.value = listOf(
                ModelDownloadBatch(
                    LocalAiModel.Type.Upscaler,
                    esrgan.id,
                    DownloadState.Downloading(20)
                )
            )
            advanceUntilIdle()
            // When
            vm.processIntent(UpscalerSetupIntent.ModelAction(esrgan.id))
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { downloads.cancel(LocalAiModel.Type.Upscaler, esrgan.id) }
            coVerify(exactly = 0) { downloads.start(any(), any()) }
        }

    @Test
    fun `given upscaler setup, downloaded model deletion is dismissed, expected payload preserved`() =
        runTest {
            // Given
            val vm = create()
            advanceUntilIdle()
            vm.processIntent(UpscalerSetupIntent.ModelAction(esrgan.id))
            val pending = vm.state.value.pendingDeleteId
            // When
            vm.processIntent(UpscalerSetupIntent.Dismiss)
            // Then
            assertEquals(esrgan.id, pending)
            assertNull(vm.state.value.pendingDeleteId)
            coVerify(exactly = 0) { delete(any()) }
        }

    @Test
    fun `given upscaler setup, downloaded model deletion is confirmed, expected owned removal and storage invalidation`() =
        runTest {
            // Given
            val vm = create()
            advanceUntilIdle()
            vm.processIntent(UpscalerSetupIntent.ModelAction(esrgan.id))
            // When
            vm.processIntent(UpscalerSetupIntent.ConfirmDelete)
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { delete(esrgan.id) }
            verify(exactly = 1) { storage.notifyChanged() }
            assertNull(vm.state.value.pendingDeleteId)
        }

    @Test
    fun `given upscaler setup, unrelated provider transfer emits then upscaler completes, expected only owning catalog reloaded`() =
        runTest {
            // Given
            val vm = create()
            advanceUntilIdle()
            batches.value = listOf(ModelDownloadBatch(LocalAiModel.Type.Mnn, "diffusion"))
            advanceUntilIdle()
            coVerify(exactly = 0) { cached(any()) }
            // When
            batches.value = listOf(
                ModelDownloadBatch(
                    LocalAiModel.Type.Upscaler,
                    esrgan.id,
                    DownloadState.Complete("/owned/model.pth"),
                    isRunning = false
                )
            )
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { cached(LocalAiModel.Type.Upscaler) }
            verify(exactly = 1) { storage.notifyChanged() }
        }

    @Test
    fun `given upscaler setup, Back is pressed from configuration then provider selection, expected local step before caller navigation`() =
        runTest {
            // Given
            val vm = create()
            advanceUntilIdle()
            vm.processIntent(UpscalerSetupIntent.Next)
            // When
            vm.processIntent(UpscalerSetupIntent.Back)
            val step = vm.state.value.step
            vm.processIntent(UpscalerSetupIntent.Back)
            // Then
            assertEquals(ServerSetupState.Step.SOURCE, step)
            verify(exactly = 1) { router.navigateBack() }
        }

    /**
     * Retains the production ViewModel until the test clears its store and long-lived collectors.
     *
     * @author Dmitriy Moroz
     */
    private fun create(): UpscalerSetupViewModel {
        val dispatchers = mockk<DispatchersProvider> {
            every { io } returns dispatcher
            every { ui } returns dispatcher
            every { immediate } returns dispatcher
        }
        return UpscalerSetupViewModel(
            mockk(relaxed = true), dispatchers, refresh, cached, configure, delete,
            downloads, preferences, build, router, storage,
        ).also { stores += ViewModelStore().apply { put("setup", it) } }
    }
}
