package com.shifthackz.aisdv1.presentation.generation.samplers

import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget
import androidx.lifecycle.viewModelScope
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.validation.ValidationResult
import com.shifthackz.aisdv1.core.validation.seed.SeedValidator
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.feature.openaioauth.OpenAiOAuthGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveStableDiffusionCppProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.presentation.components.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageIntent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.generation.img2img.toImageToImageIntent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageIntent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.generation.txt2img.toTextToImageIntent
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormEvent
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow


/**
 * Creates either production generation view model with deterministic metadata and settings inputs.
 * Unrelated generation dependencies are strict mocks so these tests cannot accidentally generate images.
 *
 * @param type Generation screen whose actual sampler state and recovery intent are exercised.
 * @param dispatcher Dispatcher sharing the test scheduler and installed Main dispatcher.
 * @param getSamplers Configurable sampler boundary for failed, delayed, empty and successful attempts.
 * @param immediateDispatcher Entry dispatcher, optionally eager to model Main.immediate initialization.
 * @param seedValidator Seed validator supplied to both production view models; unrelated sampler tests accept all seeds.
 * @param nativeSeedValidator Validator configured for native providers, independently supplied by seed tests.
 * @param easyDiffusionSeedValidator Validator configured for the Easy Diffusion unsigned 32-bit seed range.
 *
 * @author Dmitriy Moroz
 */
internal class SamplerRecoveryViewModelFixture(
    type: AiGenerationResult.Type,
    dispatcher: CoroutineDispatcher,
    getSamplers: GetStableDiffusionSamplersUseCase,
    immediateDispatcher: CoroutineDispatcher = dispatcher,
    seedValidator: SeedValidator = mockk {
        every { this@mockk.invoke(any()) } returns ValidationResult(isValid = true)
    },
    nativeSeedValidator: SeedValidator = seedValidator,
    easyDiffusionSeedValidator: SeedValidator = seedValidator,
) {
    val settings = MutableStateFlow(Settings(serverUrl = "http://server-a:7860"))
    val errors = mutableListOf<Throwable>()
    private val preferences = mockk<PreferenceManager>(relaxed = true) {
        every { observe() } returns settings
        every { textToImageSamplingSteps } returns 20
        every { textToImageCfgScale } returns 7f
        every { imageToImageSamplingSteps } returns 20
        every { imageToImageCfgScale } returns 7f
    }
    private val dispatchers = object : DispatchersProvider {
        override val io = dispatcher
        override val ui = dispatcher
        override val immediate = immediateDispatcher
    }
    private val configuration = mockk<GetConfigurationUseCase> {
        coEvery { this@mockk.invoke() } answers {
            Configuration(source = settings.value.source, serverUrl = settings.value.serverUrl)
        }
    }
    private val hordeStatus = mockk<ObserveHordeProcessStatusUseCase> {
        every { this@mockk.invoke() } returns emptyFlow()
    }
    private val localStatus = mockk<ObserveLocalDiffusionProcessStatusUseCase> {
        every { this@mockk.invoke() } returns emptyFlow()
    }
    private val textToImage: TextToImageViewModel?
    private val imageToImage: ImageToImageViewModel?

    val state: GenerationInputFormState
        get() = textToImage?.state?.value ?: checkNotNull(imageToImage).state.value

    init {
        if (type == AiGenerationResult.Type.TEXT_TO_IMAGE) {
            textToImage = TextToImageViewModel(
                mnnBackendPolicy = mockk {
                    every { supportedBackends } returns listOf(
                        MnnBackend.CPU,
                        MnnBackend.OPENCL
                    )
                },
                observeMnnProcessStatusUseCase = mockk { every { this@mockk.invoke() } returns emptyFlow() },
                observeEasyDiffusionProgressUseCase = mockk { every { this@mockk.invoke() } returns emptyFlow() },
                nativeGenerationGateway = mockk {
                    every { observeStage(any()) } returns emptyFlow()
                    every { observeStatus(any()) } returns emptyFlow()
                },
                diagnostics = mockk(relaxed = true),
                dispatchersProvider = dispatchers,
                getConfigurationUseCase = configuration,
                getStableDiffusionSamplersUseCase = getSamplers,
                getForgeModulesUseCase = mockk { coEvery { this@mockk.invoke() } returns emptyList() },
                fetchAndGetArliAiModelsUseCase = mockk(),
                isADetailerAvailableUseCase = mockk { coEvery { this@mockk.invoke() } returns false },
                textToImageUseCase = mockk(),
                saveGenerationResultUseCase = mockk(),
                saveLastResultToCacheUseCase = mockk(),
                interruptSourceGenerationUseCase = mockk(),
                observeHordeProcessStatusUseCase = hordeStatus,
                observeLocalDiffusionProcessStatusUseCase = localStatus,
                observeStableDiffusionCppProcessStatusUseCase = mockk<ObserveStableDiffusionCppProcessStatusUseCase> {
                    every { this@mockk.invoke() } returns emptyFlow()
                },
                observeCoreMlProcessStatusUseCase = mockk<ObserveCoreMlProcessStatusUseCase> {
                    every { this@mockk.invoke() } returns emptyFlow()
                },
                observeBonsaiProcessStatusUseCase = mockk<ObserveBonsaiProcessStatusUseCase> {
                    every { this@mockk.invoke() } returns emptyFlow()
                },
                preferenceManager = preferences,
                backgroundTaskManager = mockk(),
                backgroundWorkObserver = mockk(),
                wakeLockInterActor = mockk(),
                platformServices = mockk(),
                buildInfoProvider = BuildInfoProvider.stub,
                generationFormUpdateEvent = GenerationFormUpdateEvent(),
                dimensionValidator = mockk(),
                seedValidator = seedValidator,
                nativeSeedValidator = nativeSeedValidator,
                mnnSeedValidator = nativeSeedValidator,
                easyDiffusionSeedValidator = easyDiffusionSeedValidator,
                evaluateGenerationBenchmarkUseCase = { mockk() },
                recordBenchmarkPromptAnswerUseCase = { mockk() },
                suppressBenchmarkWarningsUseCase = { mockk() },
                imageSaver = mockk(),
                imageSharer = mockk(),
                router = mockk(),
                openAiOAuthGateway = mockk<OpenAiOAuthGateway> {
                    every { observeIdentity() } returns emptyFlow()
                    coEvery { currentIdentity() } returns null
                },
                providerSelectionGuard = mockk(),
                onError = errors::add,
            )
            imageToImage = null
        } else {
            textToImage = null
            imageToImage = ImageToImageViewModel(
                observeEasyDiffusionProgressUseCase = mockk { every { this@mockk.invoke() } returns emptyFlow() },
                originalImageBudget = OriginalImageBudget(32 * 1024 * 1024, 16_777_216L),
                diagnostics = mockk(relaxed = true),
                dispatchersProvider = dispatchers,
                getConfigurationUseCase = configuration,
                getStableDiffusionSamplersUseCase = getSamplers,
                getForgeModulesUseCase = mockk { coEvery { this@mockk.invoke() } returns emptyList() },
                fetchAndGetArliAiModelsUseCase = mockk(),
                isADetailerAvailableUseCase = mockk { coEvery { this@mockk.invoke() } returns false },
                getRandomImageUseCase = mockk(),
                imageToImageUseCase = mockk(),
                saveGenerationResultUseCase = mockk(),
                saveLastResultToCacheUseCase = mockk(),
                interruptSourceGenerationUseCase = mockk(),
                observeHordeProcessStatusUseCase = hordeStatus,
                observeLocalDiffusionProcessStatusUseCase = localStatus,
                preferenceManager = preferences,
                backgroundTaskManager = mockk(),
                backgroundWorkObserver = mockk(),
                wakeLockInterActor = mockk(),
                platformServices = mockk(),
                buildInfoProvider = BuildInfoProvider.stub,
                generationFormUpdateEvent = GenerationFormUpdateEvent(),
                dimensionValidator = mockk(),
                seedValidator = seedValidator,
                nativeSeedValidator = nativeSeedValidator,
                easyDiffusionSeedValidator = easyDiffusionSeedValidator,
                evaluateGenerationBenchmarkUseCase = { mockk() },
                recordBenchmarkPromptAnswerUseCase = { mockk() },
                suppressBenchmarkWarningsUseCase = { mockk() },
                imageSaver = mockk(),
                imageSharer = mockk(),
                router = mockk(),
                platformActions = mockk(),
                onError = errors::add,
            )
        }
    }

    /**
     * Sends an input edit through the selected screen's production event mapping and view model.
     *
     * @param event edit emitted by the shared generation form.
     *
     * @author Dmitriy Moroz
     */
    fun process(event: GenerationInputFormEvent) {
        textToImage?.processIntent(checkNotNull(event.toTextToImageIntent()))
        imageToImage?.processIntent(checkNotNull(event.toImageToImageIntent()))
    }

    /**
     * Sends the recovery intent used by the inline Retry action and foreground lifecycle callback.
     *
     * @author Dmitriy Moroz
     */
    fun retry() {
        textToImage?.processIntent(TextToImageIntent.RetrySamplers)
        imageToImage?.processIntent(ImageToImageIntent.RetrySamplers)
    }

    /**
     * Cancels the production view-model scope, including its settings and identity observers.
     *
     * @author Dmitriy Moroz
     */
    fun close() {
        textToImage?.viewModelScope?.cancel()
        imageToImage?.viewModelScope?.cancel()
    }
}
