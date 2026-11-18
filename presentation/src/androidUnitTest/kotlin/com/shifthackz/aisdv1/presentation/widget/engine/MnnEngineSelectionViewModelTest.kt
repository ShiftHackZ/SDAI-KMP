@file:OptIn(ExperimentalCoroutinesApi::class)

package com.shifthackz.aisdv1.presentation.widget.engine

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.SelectMnnModelUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Exercises the live model picker without importing external custom folders a second time.
 *
 * @author Dmitriy Moroz
 */
class MnnEngineSelectionViewModelTest {
    private val preferences = mockk<PreferenceManager>()
    private val configuration = mockk<GetConfigurationUseCase>()
    private val select = mockk<SelectMnnModelUseCase>()
    private val catalog = MutableStateFlow<List<LocalAiModel>>(emptyList())
    private val viewModels = ViewModelStore()
    private val failures = mutableListOf<Throwable>()

    @After
    fun dispose() {
        viewModels.clear()
    }

    @Test
    fun `given MNN model picker, custom snapshot is unavailable, expected only downloaded catalog choices`() =
        runTest {
            // Given
            val owned = LocalAiModel.CustomMnn.copy(id = "mnn_owned", downloaded = true)
            catalog.value = listOf(
                LocalAiModel.CustomMnn,
                owned,
                owned.copy(id = "mnn_missing", downloaded = false)
            )

            // When
            val picker = createPicker()
            advanceUntilIdle()

            // Then
            assertEquals(listOf(owned), picker.state.value.localAiModels)
            assertEquals(ServerSource.LOCAL_MNN, picker.state.value.mode)
        }

    @Test
    fun `given MNN model picker, existing custom snapshot is chosen, expected owned selection operation`() =
        runTest {
            // Given
            catalog.value = listOf(LocalAiModel.CustomMnn.copy(downloaded = true))
            coEvery { select(LocalAiModel.CustomMnn.id) } returns Result.success(Unit)
            val picker = createPicker()
            advanceUntilIdle()

            // When
            picker.processIntent(EngineSelectionIntent(LocalAiModel.CustomMnn.id))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { select(LocalAiModel.CustomMnn.id) }
            assertEquals(emptyList<Throwable>(), failures)
        }

    @Test
    fun `given MNN model picker, owned snapshot validation fails, expected original error reported`() =
        runTest {
            // Given
            val failure = IllegalStateException("snapshot missing")
            catalog.value = listOf(LocalAiModel.CustomMnn.copy(downloaded = true))
            coEvery { select(LocalAiModel.CustomMnn.id) } returns Result.failure(failure)
            val picker = createPicker()
            advanceUntilIdle()

            // When
            picker.processIntent(EngineSelectionIntent(LocalAiModel.CustomMnn.id))
            advanceUntilIdle()

            // Then
            assertSame(failure, failures.single())
        }

    /**
     * Uses one virtual scheduler for the real view-model's lifecycle collectors.
     *
     * @author Dmitriy Moroz
     */
    private fun TestScope.createPicker(): EngineSelectionViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = object : DispatchersProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val ui: CoroutineDispatcher = dispatcher
            override val immediate: CoroutineDispatcher = dispatcher
        }
        every { preferences.observe() } returns flowOf(Settings(source = ServerSource.LOCAL_MNN))
        coEvery { configuration() } returns Configuration(
            source = ServerSource.LOCAL_MNN,
            localMnnModelId = LocalAiModel.CustomMnn.id
        )
        return EngineSelectionViewModel(
            diagnostics = mockk(relaxed = true),
            dispatchersProvider = dispatchers,
            fetchAndGetSwarmUiModelsUseCase = mockk(),
            observeLocalMnnModelsUseCase = mockk { every { this@mockk.invoke() } returns catalog },
            selectMnnModelUseCase = select,
            observeLocalOnnxModelsUseCase = mockk {
                every { this@mockk.invoke() } returns flowOf(
                    emptyList()
                )
            },
            observeLocalCoreMlModelsUseCase = mockk {
                every { this@mockk.invoke() } returns flowOf(
                    emptyList()
                )
            },
            observeLocalBonsaiModelsUseCase = mockk {
                every { this@mockk.invoke() } returns flowOf(
                    emptyList()
                )
            },
            observeLocalSdxlModelsUseCase = mockk {
                every { this@mockk.invoke() } returns flowOf(
                    emptyList()
                )
            },
            fetchAndGetStabilityAiEnginesUseCase = mockk(),
            getHuggingFaceModelsUseCase = mockk(),
            preferenceManager = preferences,
            fetchEasyDiffusionModelsUseCase = mockk(),
            getEasyDiffusionModelsUseCase = mockk(),
            captureEasyDiffusionConnectionUseCase = mockk(),
            selectEasyDiffusionModelUseCase = mockk(),
            providerSelectionGuard = mockk(),
            getConfigurationUseCase = configuration,
            selectStableDiffusionModelUseCase = mockk(),
            getStableDiffusionModelsUseCase = mockk(),
            onError = failures::add
        ).also { viewModels.put("picker", it) }
    }
}
