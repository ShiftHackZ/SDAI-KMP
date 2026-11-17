package com.shifthackz.aisdv1.domain.preference

import com.shifthackz.aisdv1.domain.entity.MnnBackend

import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Shared settings and provider preferences. Observers receive fresh settings snapshots after explicitly notifying writes.
 *
 * @author Dmitriy Moroz
 */
interface PreferenceManager {
    /**
     * Last sampling step count in the txt2img form; defaults to 20 on first launch.
     *
     * @author Dmitriy Moroz
     */
    var textToImageSamplingSteps: Int

    /**
     * Last CFG scale in the txt2img form; defaults to 7.0 on first launch.
     *
     * @author Dmitriy Moroz
     */
    var textToImageCfgScale: Float

    /**
     * Last sampling step count in the img2img form, stored independently of txt2img.
     *
     * @author Dmitriy Moroz
     */
    var imageToImageSamplingSteps: Int

    /**
     * Last CFG scale in the img2img form, stored independently of txt2img.
     *
     * @author Dmitriy Moroz
     */
    var imageToImageCfgScale: Float

    /**
     * Deadline for provider setup and metadata requests, independent of generation waiting limits.
     * Zero disables application deadlines; platform connection policies may still apply.
     *
     * @author Dmitriy Moroz
     */
    var connectionTimeout: NetworkTimeout

    /**
     * Waiting limit shared by all network-based AI generation providers.
     *
     * @author Dmitriy Moroz
     */
    var generationRequestTimeout: NetworkTimeout

    /**
     * Persisted AUTOMATIC1111 server root, independent from other providers.
     *
     * @author Dmitriy Moroz
     */
    var automatic1111ServerUrl: String

    /**
     * Persisted SwarmUI server root; normalized without changing the protocol delimiter.
     *
     * @author Dmitriy Moroz
     */
    var swarmUiServerUrl: String

    /**
     * HTTP(S) root or reverse-proxy prefix; changing the selected instance advances its revision atomically.
     *
     * @author Dmitriy Moroz
     */
    var easyDiffusionUrl: String

    /**
     * Complete checkpoint identifier; changing the selected Easy Diffusion model invalidates captured work.
     *
     * @author Dmitriy Moroz
     */
    var easyDiffusionModel: String

    /**
     * Exact selected SwarmUI checkpoint name.
     *
     * @author Dmitriy Moroz
     */
    var swarmUiModel: String

    /**
     * Whether AUTOMATIC1111 uses the configured demonstration endpoint.
     *
     * @author Dmitriy Moroz
     */
    var demoMode: Boolean

    /**
     * Whether developer tools are visible in Settings.
     *
     * @author Dmitriy Moroz
     */
    var developerMode: Boolean

    /**
     * User-selected custom MediaPipe model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    var localMediaPipeCustomModelPath: String

    /**
     * User-selected custom Core ML model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    var localCoreMlCustomModelPath: String

    /**
     * User-selected custom Bonsai model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    var localBonsaiCustomModelPath: String

    /**
     * User-selected custom ONNX model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxCustomModelPath: String

    /**
     * User-selected custom stable-diffusion.cpp model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    var localSdxlCustomModelPath: String

    /**
     * Whether legacy ONNX generation exposes cancellation.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxAllowCancel: Boolean

    /**
     * Dispatcher policy for legacy ONNX inference.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxSchedulerThread: SchedulersToken

    /**
     * Whether to periodically monitor a supported self-hosted connection.
     *
     * @author Dmitriy Moroz
     */
    var monitorConnectivity: Boolean

    /**
     * Whether completed results are inserted into the gallery automatically.
     *
     * @author Dmitriy Moroz
     */
    var autoSaveAiResults: Boolean

    /**
     * Whether saved gallery results are exported to the platform photo store.
     *
     * @author Dmitriy Moroz
     */
    var saveToMediaStore: Boolean

    /**
     * Whether generation forms keep advanced controls expanded.
     *
     * @author Dmitriy Moroz
     */
    var formAdvancedOptionsAlwaysShow: Boolean

    /**
     * Whether prompts use the tag editor instead of plain text input.
     *
     * @author Dmitriy Moroz
     */
    var formPromptTaggedInput: Boolean

    /**
     * Selected provider; replacement increments the durable selection revision.
     *
     * @author Dmitriy Moroz
     */
    var source: ServerSource

    /**
     * Durable provider revision, also advanced by selected Easy Diffusion URL/model changes.
     *
     * @author Dmitriy Moroz
     */
    val providerSelectionRevision: Long

    /**
     * Selected AUTOMATIC1111 checkpoint identifier.
     *
     * @author Dmitriy Moroz
     */
    var sdModel: String

    /**
     * AI Horde credential; the anonymous key is supported independently of other services.
     *
     * @author Dmitriy Moroz
     */
    var hordeApiKey: String

    /**
     * Official OpenAI API key; separate from compatible servers and OAuth sessions.
     *
     * @author Dmitriy Moroz
     */
    var openAiApiKey: String

    /**
     * Editable image model identifier supplied to the compatible server.
     *
     * @author Dmitriy Moroz
     */
    var openAiCompatibleModel: String

    /**
     * Optional bearer token for the independent compatible server.
     *
     * @author Dmitriy Moroz
     */
    var openAiCompatibleApiKey: String

    /**
     * Complete HTTP(S) API prefix, including any version and reverse-proxy path.
     *
     * @author Dmitriy Moroz
     */
    var openAiCompatibleUrl: String

    /**
     * Hugging Face inference token.
     *
     * @author Dmitriy Moroz
     */
    var huggingFaceApiKey: String

    /**
     * Selected supported Hugging Face inference model alias.
     *
     * @author Dmitriy Moroz
     */
    var huggingFaceModel: String

    /**
     * Stability AI API credential.
     *
     * @author Dmitriy Moroz
     */
    var stabilityAiApiKey: String

    /**
     * Fal.ai API credential.
     *
     * @author Dmitriy Moroz
     */
    var falAiApiKey: String

    /**
     * ArliAI API credential.
     *
     * @author Dmitriy Moroz
     */
    var arliAiApiKey: String

    /**
     * Selected ArliAI checkpoint identifier.
     *
     * @author Dmitriy Moroz
     */
    var arliAiModel: String

    /**
     * Selected Stability AI engine identifier.
     *
     * @author Dmitriy Moroz
     */
    var stabilityAiEngineId: String

    /**
     * Whether onboarding has already completed.
     *
     * @author Dmitriy Moroz
     */
    var onBoardingComplete: Boolean

    /**
     * Whether a version transition requires provider setup again.
     *
     * @author Dmitriy Moroz
     */
    var forceSetupAfterUpdate: Boolean

    /**
     * Selected downloaded ONNX model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxModelId: String

    /**
     * Whether legacy ONNX inference requests Android NNAPI acceleration.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxUseNNAPI: Boolean

    /**
     * Selected downloaded MediaPipe model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localMediaPipeModelId: String

    /**
     * Selected stable-diffusion.cpp model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localSdxlModelId: String

    /**
     * Selected downloaded Core ML model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localCoreMlModelId: String

    /**
     * Last committed immutable custom manifest identity, retained across catalog selections.
     *
     * @author Dmitriy Moroz
     */
    var localMnnCustomActivationId: String

    /**
     * Manifest SHA256 captured with the selected model and queued generation inputs.
     *
     * @author Dmitriy Moroz
     */
    var localMnnActivationId: String

    /**
     * Selected catalog or custom UI identity; generation also requires the captured manifest SHA256.
     *
     * @author Dmitriy Moroz
     */
    var localMnnModelId: String

    /**
     * External custom bundle source shown by setup; execution uses its validated owned copy.
     *
     * @author Dmitriy Moroz
     */
    var localMnnModelPath: String

    /**
     * Requested MNN backend, checked again by the inference child.
     *
     * @author Dmitriy Moroz
     */
    var localMnnBackend: MnnBackend

    /**
     * Selected downloaded Bonsai model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localBonsaiModelId: String

    /**
     * Whether theme colors follow the system palette.
     *
     * @author Dmitriy Moroz
     */
    var designUseSystemColorPalette: Boolean

    /**
     * Whether appearance follows the system light/dark preference.
     *
     * @author Dmitriy Moroz
     */
    var designUseSystemDarkTheme: Boolean

    /**
     * Explicit dark appearance when system appearance is disabled.
     *
     * @author Dmitriy Moroz
     */
    var designDarkTheme: Boolean

    /**
     * Selected accent palette token, stored by its stable enum name.
     *
     * @author Dmitriy Moroz
     */
    var designColorToken: String

    /**
     * Selected dark background palette token, stored by its stable enum name.
     *
     * @author Dmitriy Moroz
     */
    var designDarkThemeToken: String

    /**
     * Whether generation is scheduled through the background task manager.
     *
     * @author Dmitriy Moroz
     */
    var backgroundGeneration: Boolean

    /**
     * Number of background processes tracked by the existing work UI.
     *
     * @author Dmitriy Moroz
     */
    var backgroundProcessCount: Int

    /**
     * Tracks whether the first local generation benchmark prompt has already
     * been answered.
     *
     * @author Dmitriy Moroz
     */
    var localGenerationBenchmarkPromptAnswered: Boolean

    /**
     * Tracks whether benchmark recommendation warnings are muted until the next
     * benchmark run.
     *
     * @author Dmitriy Moroz
     */
    var benchmarkRecommendationWarningSuppressed: Boolean

    /**
     * Gallery grid density preference.
     *
     * @author Dmitriy Moroz
     */
    var galleryGrid: Grid

    /**
     * Selected application language code; empty follows the platform default.
     *
     * @author Dmitriy Moroz
     */
    var languageCode: String

    /**
     * Emits the current settings snapshot and rereads persisted values after each invalidation.
     *
     * @author Dmitriy Moroz
     */
    fun observe(): Flow<Settings>

    /**
     * Invalidates current settings so observers reload values written through a complete configuration transaction.
     *
     * @author Dmitriy Moroz
     */
    suspend fun refresh()
}
