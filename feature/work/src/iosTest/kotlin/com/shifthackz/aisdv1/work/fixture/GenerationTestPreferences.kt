package com.shifthackz.aisdv1.work.fixture

import com.shifthackz.aisdv1.domain.entity.MnnBackend

import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import kotlinx.coroutines.flow.Flow

import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Provides only the source and revision used by background ownership checks.
 * Unrelated preference access fails immediately instead of silently influencing a scenario.
 *
 * @author Dmitriy Moroz
 */
internal class GenerationTestPreferences : PreferenceManager {
    override var textToImageSamplingSteps: Int
        get() = error("Unexpected preference read: textToImageSamplingSteps")
        set(value) {
            error("Unexpected preference write: textToImageSamplingSteps = $value")
        }

    override var textToImageCfgScale: Float
        get() = error("Unexpected preference read: textToImageCfgScale")
        set(value) {
            error("Unexpected preference write: textToImageCfgScale = $value")
        }

    override var imageToImageSamplingSteps: Int
        get() = error("Unexpected preference read: imageToImageSamplingSteps")
        set(value) {
            error("Unexpected preference write: imageToImageSamplingSteps = $value")
        }

    override var imageToImageCfgScale: Float
        get() = error("Unexpected preference read: imageToImageCfgScale")
        set(value) {
            error("Unexpected preference write: imageToImageCfgScale = $value")
        }

    override var connectionTimeout: NetworkTimeout
        get() = error("Unexpected preference read: connectionTimeout")
        set(value) {
            error("Unexpected preference write: connectionTimeout = $value")
        }

    override var generationRequestTimeout: NetworkTimeout
        get() = error("Unexpected preference read: generationRequestTimeout")
        set(value) {
            error("Unexpected preference write: generationRequestTimeout = $value")
        }

    override var automatic1111ServerUrl: String
        get() = error("Unexpected preference read: automatic1111ServerUrl")
        set(value) {
            error("Unexpected preference write: automatic1111ServerUrl = $value")
        }

    override var swarmUiServerUrl: String
        get() = error("Unexpected preference read: swarmUiServerUrl")
        set(value) {
            error("Unexpected preference write: swarmUiServerUrl = $value")
        }

    override var easyDiffusionUrl: String
        get() = error("Unexpected preference read: easyDiffusionUrl")
        set(value) {
            error("Unexpected preference write: easyDiffusionUrl = $value")
        }

    override var easyDiffusionModel: String
        get() = error("Unexpected preference read: easyDiffusionModel")
        set(value) {
            error("Unexpected preference write: easyDiffusionModel = $value")
        }

    override var swarmUiModel: String
        get() = error("Unexpected preference read: swarmUiModel")
        set(value) {
            error("Unexpected preference write: swarmUiModel = $value")
        }

    override var demoMode: Boolean
        get() = error("Unexpected preference read: demoMode")
        set(value) {
            error("Unexpected preference write: demoMode = $value")
        }

    override var developerMode: Boolean
        get() = error("Unexpected preference read: developerMode")
        set(value) {
            error("Unexpected preference write: developerMode = $value")
        }

    override var localMediaPipeCustomModelPath: String
        get() = error("Unexpected preference read: localMediaPipeCustomModelPath")
        set(value) {
            error("Unexpected preference write: localMediaPipeCustomModelPath = $value")
        }

    override var localCoreMlCustomModelPath: String
        get() = error("Unexpected preference read: localCoreMlCustomModelPath")
        set(value) {
            error("Unexpected preference write: localCoreMlCustomModelPath = $value")
        }

    override var localBonsaiCustomModelPath: String
        get() = error("Unexpected preference read: localBonsaiCustomModelPath")
        set(value) {
            error("Unexpected preference write: localBonsaiCustomModelPath = $value")
        }

    override var localOnnxCustomModelPath: String
        get() = error("Unexpected preference read: localOnnxCustomModelPath")
        set(value) {
            error("Unexpected preference write: localOnnxCustomModelPath = $value")
        }

    override var localSdxlCustomModelPath: String
        get() = error("Unexpected preference read: localSdxlCustomModelPath")
        set(value) {
            error("Unexpected preference write: localSdxlCustomModelPath = $value")
        }

    override var localOnnxAllowCancel: Boolean
        get() = error("Unexpected preference read: localOnnxAllowCancel")
        set(value) {
            error("Unexpected preference write: localOnnxAllowCancel = $value")
        }

    override var localOnnxSchedulerThread: SchedulersToken
        get() = error("Unexpected preference read: localOnnxSchedulerThread")
        set(value) {
            error("Unexpected preference write: localOnnxSchedulerThread = $value")
        }

    override var monitorConnectivity: Boolean
        get() = error("Unexpected preference read: monitorConnectivity")
        set(value) {
            error("Unexpected preference write: monitorConnectivity = $value")
        }

    override var autoSaveAiResults: Boolean
        get() = error("Unexpected preference read: autoSaveAiResults")
        set(value) {
            error("Unexpected preference write: autoSaveAiResults = $value")
        }

    override var saveToMediaStore: Boolean
        get() = error("Unexpected preference read: saveToMediaStore")
        set(value) {
            error("Unexpected preference write: saveToMediaStore = $value")
        }

    override var formAdvancedOptionsAlwaysShow: Boolean
        get() = error("Unexpected preference read: formAdvancedOptionsAlwaysShow")
        set(value) {
            error("Unexpected preference write: formAdvancedOptionsAlwaysShow = $value")
        }

    override var formPromptTaggedInput: Boolean
        get() = error("Unexpected preference read: formPromptTaggedInput")
        set(value) {
            error("Unexpected preference write: formPromptTaggedInput = $value")
        }

    override var source: ServerSource = ServerSource.EASY_DIFFUSION

    override val providerSelectionRevision: Long = 7L

    override var sdModel: String
        get() = error("Unexpected preference read: sdModel")
        set(value) {
            error("Unexpected preference write: sdModel = $value")
        }

    override var hordeApiKey: String
        get() = error("Unexpected preference read: hordeApiKey")
        set(value) {
            error("Unexpected preference write: hordeApiKey = $value")
        }

    override var openAiApiKey: String
        get() = error("Unexpected preference read: openAiApiKey")
        set(value) {
            error("Unexpected preference write: openAiApiKey = $value")
        }

    override var openAiCompatibleModel: String
        get() = error("Unexpected preference read: openAiCompatibleModel")
        set(value) {
            error("Unexpected preference write: openAiCompatibleModel = $value")
        }

    override var openAiCompatibleApiKey: String
        get() = error("Unexpected preference read: openAiCompatibleApiKey")
        set(value) {
            error("Unexpected preference write: openAiCompatibleApiKey = $value")
        }

    override var openAiCompatibleUrl: String
        get() = error("Unexpected preference read: openAiCompatibleUrl")
        set(value) {
            error("Unexpected preference write: openAiCompatibleUrl = $value")
        }

    override var huggingFaceApiKey: String
        get() = error("Unexpected preference read: huggingFaceApiKey")
        set(value) {
            error("Unexpected preference write: huggingFaceApiKey = $value")
        }

    override var huggingFaceModel: String
        get() = error("Unexpected preference read: huggingFaceModel")
        set(value) {
            error("Unexpected preference write: huggingFaceModel = $value")
        }

    override var stabilityAiApiKey: String
        get() = error("Unexpected preference read: stabilityAiApiKey")
        set(value) {
            error("Unexpected preference write: stabilityAiApiKey = $value")
        }

    override var falAiApiKey: String
        get() = error("Unexpected preference read: falAiApiKey")
        set(value) {
            error("Unexpected preference write: falAiApiKey = $value")
        }

    override var arliAiApiKey: String
        get() = error("Unexpected preference read: arliAiApiKey")
        set(value) {
            error("Unexpected preference write: arliAiApiKey = $value")
        }

    override var arliAiModel: String
        get() = error("Unexpected preference read: arliAiModel")
        set(value) {
            error("Unexpected preference write: arliAiModel = $value")
        }

    override var stabilityAiEngineId: String
        get() = error("Unexpected preference read: stabilityAiEngineId")
        set(value) {
            error("Unexpected preference write: stabilityAiEngineId = $value")
        }

    override var onBoardingComplete: Boolean
        get() = error("Unexpected preference read: onBoardingComplete")
        set(value) {
            error("Unexpected preference write: onBoardingComplete = $value")
        }

    override var forceSetupAfterUpdate: Boolean
        get() = error("Unexpected preference read: forceSetupAfterUpdate")
        set(value) {
            error("Unexpected preference write: forceSetupAfterUpdate = $value")
        }

    override var localOnnxModelId: String
        get() = error("Unexpected preference read: localOnnxModelId")
        set(value) {
            error("Unexpected preference write: localOnnxModelId = $value")
        }

    override var localOnnxUseNNAPI: Boolean
        get() = error("Unexpected preference read: localOnnxUseNNAPI")
        set(value) {
            error("Unexpected preference write: localOnnxUseNNAPI = $value")
        }

    override var localMediaPipeModelId: String
        get() = error("Unexpected preference read: localMediaPipeModelId")
        set(value) {
            error("Unexpected preference write: localMediaPipeModelId = $value")
        }

    override var localSdxlModelId: String
        get() = error("Unexpected preference read: localSdxlModelId")
        set(value) {
            error("Unexpected preference write: localSdxlModelId = $value")
        }

    override var localCoreMlModelId: String
        get() = error("Unexpected preference read: localCoreMlModelId")
        set(value) {
            error("Unexpected preference write: localCoreMlModelId = $value")
        }

    override var localMnnCustomActivationId: String
        get() = error("Unexpected preference read: localMnnCustomActivationId")
        set(value) {
            error("Unexpected preference write: localMnnCustomActivationId = $value")
        }

    override var localMnnActivationId: String
        get() = error("Unexpected preference read: localMnnActivationId")
        set(value) {
            error("Unexpected preference write: localMnnActivationId = $value")
        }

    override var localMnnModelId: String
        get() = error("Unexpected preference read: localMnnModelId")
        set(value) {
            error("Unexpected preference write: localMnnModelId = $value")
        }

    override var localMnnModelPath: String
        get() = error("Unexpected preference read: localMnnModelPath")
        set(value) {
            error("Unexpected preference write: localMnnModelPath = $value")
        }

    override var localMnnBackend: MnnBackend
        get() = error("Unexpected preference read: localMnnBackend")
        set(value) {
            error("Unexpected preference write: localMnnBackend = $value")
        }

    override var localBonsaiModelId: String
        get() = error("Unexpected preference read: localBonsaiModelId")
        set(value) {
            error("Unexpected preference write: localBonsaiModelId = $value")
        }

    override var designUseSystemColorPalette: Boolean
        get() = error("Unexpected preference read: designUseSystemColorPalette")
        set(value) {
            error("Unexpected preference write: designUseSystemColorPalette = $value")
        }

    override var designUseSystemDarkTheme: Boolean
        get() = error("Unexpected preference read: designUseSystemDarkTheme")
        set(value) {
            error("Unexpected preference write: designUseSystemDarkTheme = $value")
        }

    override var designDarkTheme: Boolean
        get() = error("Unexpected preference read: designDarkTheme")
        set(value) {
            error("Unexpected preference write: designDarkTheme = $value")
        }

    override var designColorToken: String
        get() = error("Unexpected preference read: designColorToken")
        set(value) {
            error("Unexpected preference write: designColorToken = $value")
        }

    override var designDarkThemeToken: String
        get() = error("Unexpected preference read: designDarkThemeToken")
        set(value) {
            error("Unexpected preference write: designDarkThemeToken = $value")
        }

    override var backgroundGeneration: Boolean
        get() = error("Unexpected preference read: backgroundGeneration")
        set(value) {
            error("Unexpected preference write: backgroundGeneration = $value")
        }

    override var shouldDimScreen: Boolean
        get() = error("Unexpected preference read: shouldDimScreen")
        set(value) {
            error("Unexpected preference write: shouldDimScreen = $value")
        }

    override var shouldShowPerformanceOverlay: Boolean
        get() = error("Unexpected preference read: shouldShowPerformanceOverlay")
        set(value) {
            error("Unexpected preference write: shouldShowPerformanceOverlay = $value")
        }

    override var backgroundProcessCount: Int
        get() = error("Unexpected preference read: backgroundProcessCount")
        set(value) {
            error("Unexpected preference write: backgroundProcessCount = $value")
        }

    override var localGenerationBenchmarkPromptAnswered: Boolean
        get() = error("Unexpected preference read: localGenerationBenchmarkPromptAnswered")
        set(value) {
            error("Unexpected preference write: localGenerationBenchmarkPromptAnswered = $value")
        }

    override var benchmarkRecommendationWarningSuppressed: Boolean
        get() = error("Unexpected preference read: benchmarkRecommendationWarningSuppressed")
        set(value) {
            error("Unexpected preference write: benchmarkRecommendationWarningSuppressed = $value")
        }

    override var galleryGrid: Grid
        get() = error("Unexpected preference read: galleryGrid")
        set(value) {
            error("Unexpected preference write: galleryGrid = $value")
        }

    override var languageCode: String
        get() = error("Unexpected preference read: languageCode")
        set(value) {
            error("Unexpected preference write: languageCode = $value")
        }

    override fun observe(): Flow<Settings> = error("Unexpected settings observation")

    override suspend fun refresh(): Unit = error("Unexpected settings refresh")

}
