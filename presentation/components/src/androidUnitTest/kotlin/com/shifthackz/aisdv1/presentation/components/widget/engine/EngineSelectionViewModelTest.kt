@file:OptIn(ExperimentalCoroutinesApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.engine

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCase
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCase
import com.shifthackz.aisdv1.presentation.components.mocks.mockHuggingFaceModels
import com.shifthackz.aisdv1.presentation.components.mocks.mockLocalAiModels
import com.shifthackz.aisdv1.presentation.components.mocks.mockStabilityAiEngines
import com.shifthackz.aisdv1.presentation.components.mocks.mockStableDiffusionModels
import com.shifthackz.aisdv1.presentation.components.mocks.mockSwarmUiModels
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore("ToDo: Investigate why sometimes tests fail on remote worker due to race-conditions.")
class EngineSelectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchersProvider = object : DispatchersProvider {
        override val io: CoroutineDispatcher = testDispatcher
        override val ui: CoroutineDispatcher = testDispatcher
        override val immediate: CoroutineDispatcher = testDispatcher
    }
    private val stubSettings = MutableStateFlow(Settings())
    private val stubLocalAiModels = MutableStateFlow<List<LocalAiModel>>(emptyList())
    private val stubLocalCoreMlModels = MutableStateFlow<List<LocalAiModel>>(emptyList())
    private val stubLocalBonsaiModels = MutableStateFlow<List<LocalAiModel>>(emptyList())
    private val stubLocalSdxlModels = MutableStateFlow<List<LocalAiModel>>(emptyList())
    private val stubException = Throwable("Something went wrong.")
    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubGetConfigurationUseCase = mockk<GetConfigurationUseCase>()
    private val stubSelectStableDiffusionModelUseCase = mockk<SelectStableDiffusionModelUseCase>()
    private val stubGetStableDiffusionModelsUseCase = mockk<GetStableDiffusionModelsUseCase>()
    private val stubObserveLocalAiModelsUseCase = mockk<ObserveLocalOnnxModelsUseCase>()
    private val stubObserveLocalCoreMlModelsUseCase = mockk<ObserveLocalCoreMlModelsUseCase>()
    private val stubObserveLocalBonsaiModelsUseCase = mockk<ObserveLocalBonsaiModelsUseCase>()
    private val stubObserveLocalSdxlModelsUseCase = mockk<ObserveLocalSdxlModelsUseCase>()
    private val stubFetchAndGetStabilityAiEnginesUseCase =
        mockk<FetchAndGetStabilityAiEnginesUseCase>()
    private val stubFetchHuggingFaceModelsUseCase = mockk<FetchHuggingFaceModelsUseCase>()
    private val stubFetchAndGetSwarmUiModelsUseCase = mockk<FetchAndGetSwarmUiModelsUseCase>()

    @Before
    fun initialize() {
        every {
            stubPreferenceManager.observe()
        } returns stubSettings

        every {
            stubObserveLocalAiModelsUseCase()
        } returns stubLocalAiModels

        every {
            stubObserveLocalCoreMlModelsUseCase()
        } returns stubLocalCoreMlModels

        every {
            stubObserveLocalBonsaiModelsUseCase()
        } returns stubLocalBonsaiModels

        every {
            stubObserveLocalSdxlModelsUseCase()
        } returns stubLocalSdxlModels
    }

    @After
    fun finalize() {
        unmockkAll()
    }

    @Test
    fun `given engine selection, initial model queries return the configured catalogs, expected selected A1111 model and custom ONNX model are loaded`() =
        runTest(testDispatcher) {
            // Given

            mockInitialData(DataTestCase.Mock)
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            val expected = EngineSelectionState(
                loading = false,
                mode = ServerSource.AUTOMATIC1111,
                sdModels = listOf("title_5598", "title_151297"),
                selectedSdModel = "title_5598",
                hfModels = emptyList(),
                selectedHfModel = HuggingFaceModel.default.alias,
                stEngines = emptyList(),
                selectedStEngine = "5598",
                localAiModels = listOf(LocalAiModel.CustomOnnx),
                selectedLocalAiModelId = "CUSTOM",
                swarmModels = emptyList(),
                selectedSwarmModel = "",
            )
            val actual = viewModel.state.value
            // Then
            Assert.assertEquals(expected, actual)

        }

    @Test
    fun `given engine selection, initial model queries return empty catalogs, expected model choices and selections are empty`() =
        runTest(testDispatcher) {
            // Given

            mockInitialData(DataTestCase.Empty)
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            val expected = EngineSelectionState(
                loading = false,
                mode = ServerSource.AUTOMATIC1111,
                sdModels = emptyList(),
                selectedSdModel = "",
                hfModels = emptyList(),
                selectedHfModel = "",
                stEngines = emptyList(),
                selectedStEngine = "",
                localAiModels = emptyList(),
                selectedLocalAiModelId = "",
            )
            val actual = viewModel.state.value
            // Then
            Assert.assertEquals(expected, actual)

        }

    @Test
    fun `given engine selection, initial model queries fail, expected loading ends with empty model choices`() =
        runTest(testDispatcher) {
            // Given

            mockInitialData(DataTestCase.Exception)
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            val expected = EngineSelectionState(
                loading = false,
                mode = ServerSource.AUTOMATIC1111,
                sdModels = emptyList(),
                selectedSdModel = "",
                hfModels = emptyList(),
                selectedHfModel = "",
                stEngines = emptyList(),
                selectedStEngine = "",
                localAiModels = emptyList(),
                selectedLocalAiModelId = "",
            )
            val actual = viewModel.state.value
            // Then
            Assert.assertEquals(expected, actual)

        }

    @Test
    fun `given engine selection, A1111 model title_151297 is selected, expected selectedSdModel becomes title_151297`() =
        runTest(testDispatcher) {
            // Given

            mockInitialData(DataTestCase.Mock, ServerSource.AUTOMATIC1111)
            val viewModel = createViewModel()
            advanceUntilIdle()

            coEvery {
                stubSelectStableDiffusionModelUseCase(any())
            } returns Unit

            coEvery {
                stubGetStableDiffusionModelsUseCase()
            } returns mockStableDiffusionModels.map { (model, selected) -> model to !selected }

            // When
            viewModel.processIntent(EngineSelectionIntent("title_151297"))
            advanceUntilIdle()

            val state = viewModel.state.value
            // Then
            Assert.assertEquals(false, state.loading)
            Assert.assertEquals(listOf("title_5598", "title_151297"), state.sdModels)
            Assert.assertEquals("title_151297", state.selectedSdModel)

        }

    @Test
    fun `given engine selection, Swarm UI model 151297 is selected, expected swarmUiModel preference becomes 151297`() =
        runTest(testDispatcher) {
            // Given

            mockInitialData(DataTestCase.Mock, ServerSource.SWARM_UI)
            val viewModel = createViewModel()
            advanceUntilIdle()

            every {
                stubPreferenceManager::swarmUiModel.set(any())
            } returns Unit

            // When
            viewModel.processIntent(EngineSelectionIntent("151297"))

            // Then
            verify {
                stubPreferenceManager::swarmUiModel.set("151297")
            }

        }

    @Test
    fun `given engine selection, the default Hugging Face model is selected, expected huggingFaceModel preference receives the default alias`() =
        runTest(testDispatcher) {
            // Given

            mockInitialData(DataTestCase.Mock, ServerSource.HUGGING_FACE)
            val viewModel = createViewModel()
            advanceUntilIdle()

            every {
                stubPreferenceManager::huggingFaceModel.set(any())
            } returns Unit

            // When
            viewModel.processIntent(EngineSelectionIntent(HuggingFaceModel.default.alias))

            // Then
            verify {
                stubPreferenceManager::huggingFaceModel.set(HuggingFaceModel.default.alias)
            }

        }

    @Test
    fun `given engine selection, Stability AI engine st_5598 is selected, expected stabilityAiEngineId preference becomes st_5598`() =
        runTest(testDispatcher) {
            // Given

            mockInitialData(DataTestCase.Mock, ServerSource.STABILITY_AI)
            val viewModel = createViewModel()
            advanceUntilIdle()

            every {
                stubPreferenceManager::stabilityAiEngineId.set(any())
            } returns Unit

            // When
            viewModel.processIntent(EngineSelectionIntent("st_5598"))

            // Then
            verify {
                stubPreferenceManager::stabilityAiEngineId.set("st_5598")
            }

        }

    @Test
    fun `given engine selection, local ONNX model llm_5598 is selected, expected localOnnxModelId preference becomes llm_5598`() =
        runTest(testDispatcher) {
            // Given

            mockInitialData(DataTestCase.Mock, ServerSource.LOCAL_MICROSOFT_ONNX)
            val viewModel = createViewModel()
            advanceUntilIdle()

            every {
                stubPreferenceManager::localOnnxModelId.set(any())
            } returns Unit

            // When
            viewModel.processIntent(EngineSelectionIntent("llm_5598"))

            // Then
            verify {
                stubPreferenceManager::localOnnxModelId.set("llm_5598")
            }

        }

    private fun mockInitialData(
        testCase: DataTestCase,
        source: ServerSource = ServerSource.AUTOMATIC1111,
    ) {
        stubSettings.value = Settings(source = source)

        when (testCase) {
            DataTestCase.Mock -> coEvery {
                stubGetConfigurationUseCase()
            } returns Configuration(
                serverUrl = if (source == ServerSource.AUTOMATIC1111) {
                    "https://a1111.example.com"
                } else {
                    ""
                },
                swarmUiUrl = if (source == ServerSource.SWARM_UI) {
                    "https://swarm.example.com"
                } else {
                    ""
                },
                huggingFaceModel = HuggingFaceModel.default.alias,
                stabilityAiApiKey = if (source == ServerSource.STABILITY_AI) {
                    "stub-api-key"
                } else {
                    ""
                },
                stabilityAiEngineId = "5598",
                swarmUiModel = "5598",
                localOnnxModelId = "CUSTOM",
                source = source,
            )

            DataTestCase.Empty -> coEvery {
                stubGetConfigurationUseCase()
            } returns Configuration()

            DataTestCase.Exception -> coEvery {
                stubGetConfigurationUseCase()
            } throws stubException
        }

        when (testCase) {
            DataTestCase.Mock -> coEvery {
                stubGetStableDiffusionModelsUseCase()
            } returns mockStableDiffusionModels

            DataTestCase.Empty -> coEvery {
                stubGetStableDiffusionModelsUseCase()
            } returns emptyList()

            DataTestCase.Exception -> coEvery {
                stubGetStableDiffusionModelsUseCase()
            } throws stubException
        }

        coEvery {
            stubFetchHuggingFaceModelsUseCase()
        } coAnswers {
            when (testCase) {
                DataTestCase.Mock -> mockHuggingFaceModels
                DataTestCase.Empty -> emptyList()
                DataTestCase.Exception -> throw stubException
            }
        }

        coEvery {
            stubFetchAndGetStabilityAiEnginesUseCase()
        } coAnswers {
            when (testCase) {
                DataTestCase.Mock -> mockStabilityAiEngines
                DataTestCase.Empty -> emptyList()
                DataTestCase.Exception -> throw stubException
            }
        }

        coEvery {
            stubFetchAndGetSwarmUiModelsUseCase()
        } coAnswers {
            when (testCase) {
                DataTestCase.Mock -> mockSwarmUiModels
                DataTestCase.Empty -> emptyList()
                DataTestCase.Exception -> throw stubException
            }
        }

        stubLocalAiModels.value = when (testCase) {
            DataTestCase.Mock -> mockLocalAiModels
            DataTestCase.Empty -> emptyList()
            DataTestCase.Exception -> emptyList()
        }
        stubLocalSdxlModels.value = emptyList()
        stubLocalBonsaiModels.value = emptyList()
    }

    private fun TestScope.createViewModel() = EngineSelectionViewModel(
        fetchEasyDiffusionModelsUseCase = mockk(),
        getEasyDiffusionModelsUseCase = mockk(),
        captureEasyDiffusionConnectionUseCase = mockk(),
        selectEasyDiffusionModelUseCase = mockk(),
        providerSelectionGuard = mockk(),
        diagnostics = mockk(relaxed = true),
        dispatchersProvider = dispatchersProvider,
        preferenceManager = stubPreferenceManager,
        getConfigurationUseCase = stubGetConfigurationUseCase,
        selectStableDiffusionModelUseCase = stubSelectStableDiffusionModelUseCase,
        getStableDiffusionModelsUseCase = stubGetStableDiffusionModelsUseCase,
        observeLocalMnnModelsUseCase = mockk {
            every { this@mockk.invoke() } returns flowOf(
                emptyList()
            )
        },
        selectMnnModelUseCase = mockk(),
        observeLocalOnnxModelsUseCase = stubObserveLocalAiModelsUseCase,
        observeLocalCoreMlModelsUseCase = stubObserveLocalCoreMlModelsUseCase,
        observeLocalBonsaiModelsUseCase = stubObserveLocalBonsaiModelsUseCase,
        observeLocalSdxlModelsUseCase = stubObserveLocalSdxlModelsUseCase,
        fetchAndGetStabilityAiEnginesUseCase = stubFetchAndGetStabilityAiEnginesUseCase,
        getHuggingFaceModelsUseCase = stubFetchHuggingFaceModelsUseCase,
        fetchAndGetSwarmUiModelsUseCase = stubFetchAndGetSwarmUiModelsUseCase,
    )

    private enum class DataTestCase {
        Mock,
        Empty,
        Exception,
    }
}
