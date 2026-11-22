package com.shifthackz.aisdv1.presentation.generation.di

import com.shifthackz.aisdv1.core.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.TextToImageRouter
import com.shifthackz.aisdv1.core.validation.di.easyDiffusionSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.mnnSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.nativeSeedValidatorQualifier
import com.shifthackz.aisdv1.presentation.components.model.ExtraType
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.generation.modal.embedding.EmbeddingViewModel
import com.shifthackz.aisdv1.presentation.generation.modal.extras.ExtrasViewModel
import com.shifthackz.aisdv1.presentation.generation.modal.tag.EditTagViewModel
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageViewModel
import org.koin.dsl.module

/**
 * Creates generation forms and their prompt-extra editors from domain contracts.
 * The app root retains the form instances; image-to-image receives its composition-bound native picker,
 * and benchmark operations resolve lazily when admission requires them.
 *
 * @author Dmitriy Moroz
 */
val generationViewModelModule = module {
    factory { (prompt: String, negativePrompt: String, type: ExtraType) ->
        ExtrasViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            fetchAndGetLorasUseCase = get(),
            fetchAndGetHyperNetworksUseCase = get(),
            preferenceManager = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
            type = type,
        )
    }

    factory { (prompt: String, negativePrompt: String) ->
        EmbeddingViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            fetchAndGetEmbeddingsUseCase = get(),
            preferenceManager = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
        )
    }
    factory { (prompt: String, negativePrompt: String, tag: String, isNegative: Boolean) ->
        EditTagViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
            tag = tag,
            isNegative = isNegative,
        )
    }
    factory { (router: TextToImageRouter) ->
        TextToImageViewModel(
            remoteGenerationProgressGateway = get(),
            observeEasyDiffusionProgressUseCase = get(),
            nativeGenerationGateway = get(),
            mnnBackendPolicy = get(),
            observeMnnProcessStatusUseCase = get(),
            diagnostics = get(),
            openAiOAuthGateway = get(),
            providerSelectionGuard = get(),
            dispatchersProvider = get(),
            getConfigurationUseCase = get(),
            getStableDiffusionSamplersUseCase = get(),
            getForgeModulesUseCase = get(),
            fetchAndGetArliAiModelsUseCase = get(),
            isADetailerAvailableUseCase = get(),
            textToImageUseCase = get(),
            saveGenerationResultUseCase = get(),
            saveLastResultToCacheUseCase = get(),
            interruptSourceGenerationUseCase = get(),
            observeHordeProcessStatusUseCase = get(),
            observeLocalDiffusionProcessStatusUseCase = get(),
            observeStableDiffusionCppProcessStatusUseCase = get(),
            observeCoreMlProcessStatusUseCase = get(),
            observeBonsaiProcessStatusUseCase = get(),
            preferenceManager = get(),
            backgroundTaskManager = get(),
            backgroundWorkObserver = get(),
            wakeLockInterActor = get(),
            platformServices = get(),
            buildInfoProvider = get(),
            generationFormUpdateEvent = get(),
            dimensionValidator = get(),
            seedValidator = get(),
            nativeSeedValidator = get(nativeSeedValidatorQualifier),
            mnnSeedValidator = get(mnnSeedValidatorQualifier),
            easyDiffusionSeedValidator = get(easyDiffusionSeedValidatorQualifier),
            evaluateGenerationBenchmarkUseCase = { get() },
            recordBenchmarkPromptAnswerUseCase = { get() },
            suppressBenchmarkWarningsUseCase = { get() },
            imageSaver = get(),
            imageSharer = get(),
            router = router,
        )
    }
    factory { (router: ImageToImageRouter, platformActions: ImageToImagePlatformActions) ->
        ImageToImageViewModel(
            remoteGenerationProgressGateway = get(),
            observeEasyDiffusionProgressUseCase = get(),
            originalImageBudget = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            getConfigurationUseCase = get(),
            getStableDiffusionSamplersUseCase = get(),
            getForgeModulesUseCase = get(),
            fetchAndGetArliAiModelsUseCase = get(),
            isADetailerAvailableUseCase = get(),
            getRandomImageUseCase = get(),
            imageToImageUseCase = get(),
            saveGenerationResultUseCase = get(),
            saveLastResultToCacheUseCase = get(),
            interruptSourceGenerationUseCase = get(),
            observeHordeProcessStatusUseCase = get(),
            observeCoreMlProcessStatusUseCase = get(),
            observeLocalDiffusionProcessStatusUseCase = get(),
            preferenceManager = get(),
            backgroundTaskManager = get(),
            backgroundWorkObserver = get(),
            wakeLockInterActor = get(),
            platformServices = get(),
            buildInfoProvider = get(),
            generationFormUpdateEvent = get(),
            dimensionValidator = get(),
            seedValidator = get(),
            nativeSeedValidator = get(nativeSeedValidatorQualifier),
            easyDiffusionSeedValidator = get(easyDiffusionSeedValidatorQualifier),
            evaluateGenerationBenchmarkUseCase = { get() },
            recordBenchmarkPromptAnswerUseCase = { get() },
            suppressBenchmarkWarningsUseCase = { get() },
            imageSaver = get(),
            imageSharer = get(),
            router = router,
            platformActions = platformActions,
        )
    }
}
