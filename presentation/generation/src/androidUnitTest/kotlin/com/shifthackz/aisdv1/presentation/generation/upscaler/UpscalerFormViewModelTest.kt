package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
 * Protects retained shared-form selection, platform visibility and failure recovery on preference changes.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UpscalerFormViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val updates = MutableSharedFlow<Settings>(replay = 1)
    private val preferences = mockk<PreferenceManager>()
    private val models = mockk<GetCachedLocalModelCatalogUseCase>()
    private val build = mockk<BuildInfoProvider>()
    private val stores = mutableListOf<ViewModelStore>()
    private val esrgan = LocalAiModel(
        "upscaler_esrgan",
        LocalAiModel.Type.Upscaler,
        "ESRGAN",
        "17 MB",
        emptyList(),
        downloaded = true
    )
    private val mnn = esrgan.copy(id = "upscaler_mnn_anime", name = "MNN")

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { build.platform } returns Platform.ANDROID
        every { preferences.observe() } returns updates
        every { preferences.upscalerModelId } returns esrgan.id
        coEvery { models(LocalAiModel.Type.Upscaler) } returns listOf(esrgan, mnn)
        updates.tryEmit(Settings())
    }

    @After
    fun cleanup() {
        stores.forEach(ViewModelStore::clear); Dispatchers.resetMain()
    }

    @Test
    fun `given shared upscaler form, iOS catalog is read, expected only MNN and independent saved selection`() =
        runTest {
            // Given
            every { build.platform } returns Platform.IOS
            every { preferences.upscalerModelId } returns mnn.id
            val vm = create()
            // When
            advanceUntilIdle()
            // Then
            assertEquals(listOf(mnn), vm.state.value.models)
            assertEquals(mnn, vm.state.value.model)
            assertFalse(vm.state.value.isLoading)
        }

    @Test
    fun `given shared upscaler form, saved model preference changes, expected retained form observes new selection`() =
        runTest {
            // Given
            val vm = create()
            advanceUntilIdle()
            every { preferences.upscalerModelId } returns mnn.id
            // When
            updates.emit(Settings())
            advanceUntilIdle()
            // Then
            assertEquals(mnn, vm.state.value.model)
        }

    @Test
    fun `given shared upscaler form, configured model is absent, expected no implicit replacement selection`() =
        runTest {
            // Given
            every { preferences.upscalerModelId } returns "missing"
            val vm = create()
            // When
            advanceUntilIdle()
            // Then
            assertNull(vm.state.value.model)
            assertEquals(listOf(esrgan, mnn), vm.state.value.models)
        }

    @Test
    fun `given shared upscaler form, cache read fails then preference update retries, expected visible error then recovered selected model`() =
        runTest {
            // Given
            coEvery { models(LocalAiModel.Type.Upscaler) } throws IllegalStateException("cache unavailable")
            val vm = create()
            advanceUntilIdle()
            assertEquals("cache unavailable", vm.state.value.error)
            coEvery { models(LocalAiModel.Type.Upscaler) } returns listOf(esrgan, mnn)
            // When
            updates.emit(Settings())
            advanceUntilIdle()
            // Then
            assertNull(vm.state.value.error)
            assertEquals(esrgan, vm.state.value.model)
            assertFalse(vm.state.value.isLoading)
        }

    /**
     * Owns collectors through a real lifecycle store using the test scheduler.
     *
     * @author Dmitriy Moroz
     */
    private fun create(): UpscalerFormViewModel {
        val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
        return UpscalerFormViewModel(preferences, models, dispatchers, build)
            .also { stores += ViewModelStore().apply { put("form", it) } }
    }
}
