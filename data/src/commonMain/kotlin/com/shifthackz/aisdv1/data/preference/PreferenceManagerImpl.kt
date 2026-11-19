package com.shifthackz.aisdv1.data.preference

import com.shifthackz.aisdv1.domain.entity.MnnBackend

import com.shifthackz.aisdv1.core.common.extensions.fixUrlSlashes
import com.shifthackz.aisdv1.core.common.file.LOCAL_DIFFUSION_CUSTOM_PATH
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Reads shared settings from the platform key/value store and invalidates hot settings snapshots after writes.
 *
 * @param keyValueStore Platform preference store supporting complete durable configuration batches.
 *
 * @author Dmitriy Moroz
 */
internal class PreferenceManagerImpl(

    private val keyValueStore: KeyValueStore,
) : PreferenceManager {


    private val preferencesChangedState = MutableStateFlow(Any())

    /**
     * Retains txt2img sampling steps across process restarts without broadcasting form edits.
     *
     * @author Dmitriy Moroz
     */
    override var textToImageSamplingSteps: Int
        get() = keyValueStore.getInt(KEY_TEXT_TO_IMAGE_SAMPLING_STEPS, 20).takeIf { it > 0 } ?: 20
        set(value) = keyValueStore.putInt(KEY_TEXT_TO_IMAGE_SAMPLING_STEPS, value)

    /**
     * Stores txt2img CFG as a decimal string supported by both platform preference stores.
     *
     * @author Dmitriy Moroz
     */
    override var textToImageCfgScale: Float
        get() = readCfgScale(KEY_TEXT_TO_IMAGE_CFG_SCALE)
        set(value) = keyValueStore.putString(KEY_TEXT_TO_IMAGE_CFG_SCALE, value.toString())

    /**
     * Retains img2img sampling steps separately from the txt2img form.
     *
     * @author Dmitriy Moroz
     */
    override var imageToImageSamplingSteps: Int
        get() = keyValueStore.getInt(KEY_IMAGE_TO_IMAGE_SAMPLING_STEPS, 20).takeIf { it > 0 } ?: 20
        set(value) = keyValueStore.putInt(KEY_IMAGE_TO_IMAGE_SAMPLING_STEPS, value)

    /**
     * Stores img2img CFG as a decimal string without losing fractional precision.
     *
     * @author Dmitriy Moroz
     */
    override var imageToImageCfgScale: Float
        get() = readCfgScale(KEY_IMAGE_TO_IMAGE_CFG_SCALE)
        set(value) = keyValueStore.putString(KEY_IMAGE_TO_IMAGE_CFG_SCALE, value.toString())

    /**
     * Persists a separate connection limit and notifies settings observers.
     *
     * @author Dmitriy Moroz
     */
    override var connectionTimeout: NetworkTimeout
        get() = NetworkTimeout.fromSeconds(
            keyValueStore.getInt(
                KEY_CONNECTION_TIMEOUT_SECONDS,
                NetworkTimeout.TWO_MINUTES.seconds
            ),
        )
        set(value) = putInt(KEY_CONNECTION_TIMEOUT_SECONDS, value.seconds)

    /**
     * Persists the generation waiting limit and notifies Settings observers immediately.
     *
     * @author Dmitriy Moroz
     */
    override var generationRequestTimeout: NetworkTimeout
        get() = NetworkTimeout.fromSeconds(
            keyValueStore.getInt(KEY_GENERATION_REQUEST_TIMEOUT_SECONDS, 120),
        )
        set(value) = putInt(KEY_GENERATION_REQUEST_TIMEOUT_SECONDS, value.seconds)

    /**
     * Persisted AUTOMATIC1111 server root, independent from other providers.
     *
     * @author Dmitriy Moroz
     */
    override var automatic1111ServerUrl: String
        get() = keyValueStore.getString(KEY_SERVER_URL).fixUrlSlashes()
        set(value) = putString(KEY_SERVER_URL, value.fixUrlSlashes())

    /**
     * Persisted SwarmUI server root; normalized without changing the protocol delimiter.
     *
     * @author Dmitriy Moroz
     */
    override var swarmUiServerUrl: String
        get() = keyValueStore.getString(KEY_SWARM_SERVER_URL).fixUrlSlashes()
        set(value) = putString(KEY_SWARM_SERVER_URL, value.fixUrlSlashes())

    /**
     * HTTP(S) root or reverse-proxy prefix; changing the selected instance advances its revision atomically.
     *
     * @author Dmitriy Moroz
     */
    override var easyDiffusionUrl: String
        get() = keyValueStore.getString(KEY_EASY_DIFFUSION_URL, "")
        set(value) {
            keyValueStore.writeEasyDiffusionSetting(KEY_EASY_DIFFUSION_URL, value)
            onPreferencesChanged()
        }

    /**
     * Complete checkpoint identifier; changing the selected Easy Diffusion model invalidates captured work.
     *
     * @author Dmitriy Moroz
     */
    override var easyDiffusionModel: String
        get() = keyValueStore.getString(KEY_EASY_DIFFUSION_MODEL, "")
        set(value) {
            keyValueStore.writeEasyDiffusionSetting(KEY_EASY_DIFFUSION_MODEL, value)
            onPreferencesChanged()
        }

    /**
     * Exact selected SwarmUI checkpoint name.
     *
     * @author Dmitriy Moroz
     */
    override var swarmUiModel: String
        get() = keyValueStore.getString(KEY_SWARM_MODEL)
        set(value) = putString(KEY_SWARM_MODEL, value)

    /**
     * Whether AUTOMATIC1111 uses the configured demonstration endpoint.
     *
     * @author Dmitriy Moroz
     */
    override var demoMode: Boolean
        get() = keyValueStore.getBoolean(KEY_DEMO_MODE)
        set(value) = putBoolean(KEY_DEMO_MODE, value)

    /**
     * Whether developer tools are visible in Settings.
     *
     * @author Dmitriy Moroz
     */
    override var developerMode: Boolean
        get() = keyValueStore.getBoolean(KEY_DEVELOPER_MODE)
        set(value) = putBoolean(KEY_DEVELOPER_MODE, value)

    /**
     * User-selected custom MediaPipe model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    override var localMediaPipeCustomModelPath: String
        get() = keyValueStore.getString(
            KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH,
            LOCAL_DIFFUSION_CUSTOM_PATH
        )
        set(value) = keyValueStore.putString(KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH, value)

    /**
     * User-selected custom Core ML model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    override var localCoreMlCustomModelPath: String
        get() = keyValueStore.getString(KEY_CORE_ML_CUSTOM_MODEL_PATH, LOCAL_DIFFUSION_CUSTOM_PATH)
        set(value) = keyValueStore.putString(KEY_CORE_ML_CUSTOM_MODEL_PATH, value)

    /**
     * User-selected custom Bonsai model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    override var localBonsaiCustomModelPath: String
        get() = keyValueStore.getString(KEY_BONSAI_CUSTOM_MODEL_PATH, LOCAL_DIFFUSION_CUSTOM_PATH)
        set(value) = keyValueStore.putString(KEY_BONSAI_CUSTOM_MODEL_PATH, value)

    /**
     * User-selected custom ONNX model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxCustomModelPath: String
        get() = keyValueStore.getString(
            KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH,
            LOCAL_DIFFUSION_CUSTOM_PATH
        )
        set(value) = keyValueStore.putString(KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH, value)

    /**
     * User-selected custom stable-diffusion.cpp model filesystem path.
     *
     * @author Dmitriy Moroz
     */
    override var localSdxlCustomModelPath: String
        get() = keyValueStore.getString(KEY_SDXL_CUSTOM_MODEL_PATH, LOCAL_DIFFUSION_CUSTOM_PATH)
        set(value) = keyValueStore.putString(KEY_SDXL_CUSTOM_MODEL_PATH, value)

    /**
     * Whether legacy ONNX generation exposes cancellation.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxAllowCancel: Boolean
        get() = keyValueStore.getBoolean(KEY_ALLOW_LOCAL_DIFFUSION_CANCEL)
        set(value) = putBoolean(KEY_ALLOW_LOCAL_DIFFUSION_CANCEL, value)

    /**
     * Dispatcher policy for legacy ONNX inference.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxSchedulerThread: SchedulersToken
        get() = enumValueOrDefault(
            index = keyValueStore.getInt(
                KEY_LOCAL_DIFFUSION_SCHEDULER_THREAD,
                SchedulersToken.COMPUTATION.ordinal
            ),
            default = SchedulersToken.COMPUTATION,
        )
        set(value) = putInt(KEY_LOCAL_DIFFUSION_SCHEDULER_THREAD, value.ordinal)

    /**
     * Whether to periodically monitor a supported self-hosted connection.
     *
     * @author Dmitriy Moroz
     */
    override var monitorConnectivity: Boolean
        get() {
            val value = keyValueStore.getBoolean(KEY_MONITOR_CONNECTIVITY)
            return if (!source.featureTags.contains(FeatureTag.OwnServer)) false else value
        }
        set(value) = putBoolean(KEY_MONITOR_CONNECTIVITY, value)

    /**
     * Whether completed results are inserted into the gallery automatically.
     *
     * @author Dmitriy Moroz
     */
    override var autoSaveAiResults: Boolean
        get() = keyValueStore.getBoolean(KEY_AI_AUTO_SAVE, true)
        set(value) = putBoolean(KEY_AI_AUTO_SAVE, value)

    /**
     * Whether saved gallery results are exported to the platform photo store.
     *
     * @author Dmitriy Moroz
     */
    override var saveToMediaStore: Boolean
        get() = keyValueStore.getBoolean(KEY_SAVE_TO_MEDIA_STORE, defaultSaveToMediaStore())
        set(value) = putBoolean(KEY_SAVE_TO_MEDIA_STORE, value)

    /**
     * Whether generation forms keep advanced controls expanded.
     *
     * @author Dmitriy Moroz
     */
    override var formAdvancedOptionsAlwaysShow: Boolean
        get() = keyValueStore.getBoolean(KEY_FORM_ALWAYS_SHOW_ADVANCED_OPTIONS)
        set(value) = putBoolean(KEY_FORM_ALWAYS_SHOW_ADVANCED_OPTIONS, value)

    /**
     * Whether prompts use the tag editor instead of plain text input.
     *
     * @author Dmitriy Moroz
     */
    override var formPromptTaggedInput: Boolean
        get() = keyValueStore.getBoolean(KEY_FORM_PROMPT_TAGGED_INPUT)
        set(value) = putBoolean(KEY_FORM_PROMPT_TAGGED_INPUT, value)

    /**
     * Selected provider; replacement increments the durable selection revision.
     *
     * @author Dmitriy Moroz
     */
    override var source: ServerSource
        get() = keyValueStore.readProviderSelection().first
        set(value) {
            keyValueStore.writeProviderSelection(value)
            onPreferencesChanged()
        }

    override val providerSelectionRevision: Long
        get() = keyValueStore.readProviderSelection().second

    /**
     * Selected AUTOMATIC1111 checkpoint identifier.
     *
     * @author Dmitriy Moroz
     */
    override var sdModel: String
        get() = keyValueStore.getString(KEY_SD_MODEL)
        set(value) = putString(KEY_SD_MODEL, value)

    /**
     * AI Horde credential; the anonymous key is supported independently of other services.
     *
     * @author Dmitriy Moroz
     */
    override var hordeApiKey: String
        get() = keyValueStore.getString(KEY_HORDE_API_KEY)
        set(value) = putString(KEY_HORDE_API_KEY, value)

    /**
     * Complete HTTP(S) API prefix, including any version and reverse-proxy path.
     *
     * @author Dmitriy Moroz
     */
    override var openAiCompatibleUrl: String
        get() = keyValueStore.getString(KEY_OPEN_AI_COMPATIBLE_URL, "")
        set(value) = putString(KEY_OPEN_AI_COMPATIBLE_URL, value)

    /**
     * Optional bearer token for the independent compatible server.
     *
     * @author Dmitriy Moroz
     */
    override var openAiCompatibleApiKey: String
        get() = keyValueStore.getString(KEY_OPEN_AI_COMPATIBLE_API_KEY, "")
        set(value) = putString(KEY_OPEN_AI_COMPATIBLE_API_KEY, value)

    /**
     * Editable image model identifier supplied to the compatible server.
     *
     * @author Dmitriy Moroz
     */
    override var openAiCompatibleModel: String
        get() = keyValueStore.getString(KEY_OPEN_AI_COMPATIBLE_MODEL, "gpt-image-2")
        set(value) = keyValueStore.putString(KEY_OPEN_AI_COMPATIBLE_MODEL, value)

    /**
     * Official OpenAI API key; separate from compatible servers and OAuth sessions.
     *
     * @author Dmitriy Moroz
     */
    override var openAiApiKey: String
        get() = keyValueStore.getString(KEY_OPEN_AI_API_KEY)
        set(value) = putString(KEY_OPEN_AI_API_KEY, value)

    /**
     * Hugging Face inference token.
     *
     * @author Dmitriy Moroz
     */
    override var huggingFaceApiKey: String
        get() = keyValueStore.getString(KEY_HUGGING_FACE_API_KEY)
        set(value) = putString(KEY_HUGGING_FACE_API_KEY, value)

    /**
     * Selected supported Hugging Face inference model alias.
     *
     * @author Dmitriy Moroz
     */
    override var huggingFaceModel: String
        get() = keyValueStore.getString(KEY_HUGGING_FACE_MODEL_KEY, HuggingFaceModel.default.alias)
        set(value) = putString(KEY_HUGGING_FACE_MODEL_KEY, value)

    /**
     * Stability AI API credential.
     *
     * @author Dmitriy Moroz
     */
    override var stabilityAiApiKey: String
        get() = keyValueStore.getString(KEY_STABILITY_AI_API_KEY)
        set(value) = putString(KEY_STABILITY_AI_API_KEY, value)

    /**
     * Fal.ai API credential.
     *
     * @author Dmitriy Moroz
     */
    override var falAiApiKey: String
        get() = keyValueStore.getString(KEY_FAL_AI_API_KEY)
        set(value) = putString(KEY_FAL_AI_API_KEY, value)

    /**
     * ArliAI API credential.
     *
     * @author Dmitriy Moroz
     */
    override var arliAiApiKey: String
        get() = keyValueStore.getString(KEY_ARLI_AI_API_KEY)
        set(value) = putString(KEY_ARLI_AI_API_KEY, value)

    /**
     * Selected ArliAI checkpoint identifier.
     *
     * @author Dmitriy Moroz
     */
    override var arliAiModel: String
        get() = keyValueStore.getString(KEY_ARLI_AI_MODEL_KEY)
        set(value) = putString(KEY_ARLI_AI_MODEL_KEY, value)

    /**
     * Selected Stability AI engine identifier.
     *
     * @author Dmitriy Moroz
     */
    override var stabilityAiEngineId: String
        get() = keyValueStore.getString(KEY_STABILITY_AI_ENGINE_ID_KEY)
        set(value) = putString(KEY_STABILITY_AI_ENGINE_ID_KEY, value)

    /**
     * Whether onboarding has already completed.
     *
     * @author Dmitriy Moroz
     */
    override var onBoardingComplete: Boolean
        get() = keyValueStore.getBoolean(KEY_ON_BOARDING_COMPLETE)
        set(value) = keyValueStore.putBoolean(KEY_ON_BOARDING_COMPLETE, value)

    /**
     * Whether a version transition requires provider setup again.
     *
     * @author Dmitriy Moroz
     */
    override var forceSetupAfterUpdate: Boolean
        get() = keyValueStore.getBoolean(KEY_FORCE_SETUP_AFTER_UPDATE, true)
        set(value) = putBoolean(KEY_FORCE_SETUP_AFTER_UPDATE, value)

    /**
     * Selected downloaded ONNX model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxModelId: String
        get() = keyValueStore.getString(KEY_LOCAL_MODEL_ID)
        set(value) = putString(KEY_LOCAL_MODEL_ID, value)

    /**
     * Whether legacy ONNX inference requests Android NNAPI acceleration.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxUseNNAPI: Boolean
        get() = keyValueStore.getBoolean(KEY_LOCAL_NN_API)
        set(value) = putBoolean(KEY_LOCAL_NN_API, value)

    /**
     * Selected downloaded MediaPipe model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localMediaPipeModelId: String
        get() = keyValueStore.getString(KEY_MEDIA_PIPE_MODEL_ID)
        set(value) = putString(KEY_MEDIA_PIPE_MODEL_ID, value)

    /**
     * Selected stable-diffusion.cpp model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localSdxlModelId: String
        get() = keyValueStore.getString(KEY_SDXL_MODEL_ID)
        set(value) = putString(KEY_SDXL_MODEL_ID, value)

    /**
     * Selected downloaded Core ML model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localCoreMlModelId: String
        get() = keyValueStore.getString(KEY_CORE_ML_MODEL_ID)
        set(value) = putString(KEY_CORE_ML_MODEL_ID, value)

    /**
     * External custom bundle source shown in setup; generation reads its immutable private snapshot.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnModelPath: String
        get() = keyValueStore.getString(KEY_MNN_MODEL_PATH)
        set(value) = putString(KEY_MNN_MODEL_PATH, value)

    /**
     * Last committed immutable custom manifest identity, retained across catalog selections.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnCustomActivationId: String
        get() = keyValueStore.getString(KEY_MNN_CUSTOM_ACTIVATION_ID)
        set(value) = putString(KEY_MNN_CUSTOM_ACTIVATION_ID, value)

    /**
     * Manifest SHA256 captured with the selected model and queued generation inputs.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnActivationId: String
        get() = keyValueStore.getString(KEY_MNN_ACTIVATION_ID)
        set(value) = putString(KEY_MNN_ACTIVATION_ID, value)

    /**
     * Selected catalog or custom UI identity; generation also requires the captured manifest SHA256.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnModelId: String
        get() = keyValueStore.getString(KEY_MNN_MODEL_ID)
        set(value) = putString(KEY_MNN_MODEL_ID, value)

    /**
     * Captures the requested execution backend independently of other providers.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnBackend: MnnBackend
        get() {
            val raw = keyValueStore.getString(KEY_MNN_BACKEND)
            val backend = MnnBackend.parse(raw)
            if (raw != backend.key) keyValueStore.putString(KEY_MNN_BACKEND, backend.key)
            return backend
        }
        set(value) = putString(KEY_MNN_BACKEND, value.key)

    /**
     * Selected downloaded Bonsai model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localBonsaiModelId: String
        get() = keyValueStore.getString(KEY_BONSAI_MODEL_ID)
        set(value) = putString(KEY_BONSAI_MODEL_ID, value)

    /**
     * Whether theme colors follow the system palette.
     *
     * @author Dmitriy Moroz
     */
    override var designUseSystemColorPalette: Boolean
        get() = keyValueStore.getBoolean(KEY_DESIGN_DYNAMIC_COLORS)
        set(value) = putBoolean(KEY_DESIGN_DYNAMIC_COLORS, value)

    /**
     * Whether appearance follows the system light/dark preference.
     *
     * @author Dmitriy Moroz
     */
    override var designUseSystemDarkTheme: Boolean
        get() = keyValueStore.getBoolean(KEY_DESIGN_SYSTEM_DARK_THEME, true)
        set(value) = putBoolean(KEY_DESIGN_SYSTEM_DARK_THEME, value)

    /**
     * Explicit dark appearance when system appearance is disabled.
     *
     * @author Dmitriy Moroz
     */
    override var designDarkTheme: Boolean
        get() = keyValueStore.getBoolean(KEY_DESIGN_DARK_THEME, true)
        set(value) = putBoolean(KEY_DESIGN_DARK_THEME, value)

    /**
     * Selected accent palette token, stored by its stable enum name.
     *
     * @author Dmitriy Moroz
     */
    override var designColorToken: String
        get() = keyValueStore.getString(KEY_DESIGN_COLOR_TOKEN, "${ColorToken.MAUVE}")
        set(value) = putString(KEY_DESIGN_COLOR_TOKEN, value)

    /**
     * Selected dark background palette token, stored by its stable enum name.
     *
     * @author Dmitriy Moroz
     */
    override var designDarkThemeToken: String
        get() = keyValueStore.getString(KEY_DESIGN_DARK_TOKEN, "${DarkThemeToken.FRAPPE}")
        set(value) = putString(KEY_DESIGN_DARK_TOKEN, value)

    /**
     * Whether generation progress displays app CPU and resident-memory history.
     *
     * @author Dmitriy Moroz
     */
    override var shouldShowPerformanceOverlay: Boolean
        get() = keyValueStore.getBoolean(KEY_PERFORMANCE_OVERLAY, true)
        set(value) = putBoolean(KEY_PERFORMANCE_OVERLAY, value)

    /**
     * Whether active foreground generation lowers display brightness.
     * @author Dmitriy Moroz
     */
    override var shouldDimScreen: Boolean
        get() = keyValueStore.getBoolean(KEY_DIM_SCREEN)
        set(value) = putBoolean(KEY_DIM_SCREEN, value)

    /**
     * Whether generation is scheduled through the background task manager.
     *
     * @author Dmitriy Moroz
     */
    override var backgroundGeneration: Boolean
        get() = keyValueStore.getBoolean(KEY_BACKGROUND_GENERATION)
        set(value) = putBoolean(KEY_BACKGROUND_GENERATION, value)

    /**
     * Number of background processes tracked by the existing work UI.
     *
     * @author Dmitriy Moroz
     */
    override var backgroundProcessCount: Int
        get() = keyValueStore.getInt(KEY_BACKGROUND_PROCESS_COUNT)
        set(value) = keyValueStore.putInt(KEY_BACKGROUND_PROCESS_COUNT, value)

    /**
     * Whether the first-local-generation benchmark suggestion was answered.
     *
     * @author Dmitriy Moroz
     */
    override var localGenerationBenchmarkPromptAnswered: Boolean
        get() = keyValueStore.getBoolean(KEY_LOCAL_GENERATION_BENCHMARK_PROMPT_ANSWERED)
        set(value) = putBoolean(KEY_LOCAL_GENERATION_BENCHMARK_PROMPT_ANSWERED, value)

    /**
     * Whether benchmark recommendation warnings were suppressed.
     *
     * @author Dmitriy Moroz
     */
    override var benchmarkRecommendationWarningSuppressed: Boolean
        get() = keyValueStore.getBoolean(KEY_BENCHMARK_RECOMMENDATION_WARNING_SUPPRESSED)
        set(value) = putBoolean(KEY_BENCHMARK_RECOMMENDATION_WARNING_SUPPRESSED, value)

    /**
     * Gallery grid density preference.
     *
     * @author Dmitriy Moroz
     */
    override var galleryGrid: Grid
        get() = enumValueOrDefault(
            index = keyValueStore.getInt(KEY_GALLERY_GRID, Grid.entries.first().ordinal),
            default = Grid.entries.first(),
        )
        set(value) = putInt(KEY_GALLERY_GRID, value.ordinal)

    /**
     * Selected application language code; empty follows the platform default.
     *
     * @author Dmitriy Moroz
     */
    override var languageCode: String
        get() = keyValueStore.getString(KEY_LANGUAGE_CODE)
        set(value) = putString(KEY_LANGUAGE_CODE, value)

    /**
     * Emits the current settings snapshot and rereads persisted values after each invalidation.
     *
     * @author Dmitriy Moroz
     */
    override fun observe(): Flow<Settings> = preferencesChangedState
        .map {
            Settings(
                connectionTimeout = connectionTimeout,
                generationRequestTimeout = generationRequestTimeout,
                serverUrl = automatic1111ServerUrl,
                sdModel = sdModel,
                demoMode = demoMode,
                developerMode = developerMode,
                localDiffusionAllowCancel = localOnnxAllowCancel,
                localDiffusionSchedulerThread = localOnnxSchedulerThread,
                monitorConnectivity = monitorConnectivity,
                backgroundGeneration = backgroundGeneration,
                shouldDimScreen = shouldDimScreen,
                shouldShowPerformanceOverlay = shouldShowPerformanceOverlay,
                autoSaveAiResults = autoSaveAiResults,
                saveToMediaStore = saveToMediaStore,
                formAdvancedOptionsAlwaysShow = formAdvancedOptionsAlwaysShow,
                formPromptTaggedInput = formPromptTaggedInput,
                source = source,
                providerSelectionRevision = providerSelectionRevision,
                hordeApiKey = hordeApiKey,
                arliAiModel = arliAiModel,
                localUseNNAPI = localOnnxUseNNAPI,
                designUseSystemColorPalette = designUseSystemColorPalette,
                designUseSystemDarkTheme = designUseSystemDarkTheme,
                designDarkTheme = designDarkTheme,
                designColorToken = designColorToken,
                designDarkThemeToken = designDarkThemeToken,
                galleryGrid = galleryGrid,
                languageCode = languageCode,
            )
        }

    /**
     * Invalidates current settings so observers reload values written through a complete configuration transaction.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun refresh() {
        preferencesChangedState.value = Any()
    }

    /**
     * Reads a stored CFG value, retaining the default for absent or invalid data.
     *
     * @param key Preference key for the generation form's decimal CFG value.
     *
     * @author Dmitriy Moroz
     */
    private fun readCfgScale(key: String): Float = keyValueStore.getString(key)
        .toFloatOrNull()
        ?.takeIf { it.isFinite() && it >= 0f }
        ?: 7f

    /**
     * Persists a string preference and notifies settings observers after the write.
     *
     * @param key Stable preference key for the value being persisted.
     * @param value Value to persist under the supplied key.
     *
     * @author Dmitriy Moroz
     */
    private fun putString(key: String, value: String) {
        keyValueStore.putString(key, value)
        onPreferencesChanged()
    }

    /**
     * Persists a Boolean preference and notifies settings observers after the write.
     *
     * @param key Stable preference key for the value being persisted.
     * @param value Value to persist under the supplied key.
     *
     * @author Dmitriy Moroz
     */
    private fun putBoolean(key: String, value: Boolean) {
        keyValueStore.putBoolean(key, value)
        onPreferencesChanged()
    }

    /**
     * Persists an integer preference and notifies settings observers after the write.
     *
     * @param key Stable preference key for the value being persisted.
     * @param value Value to persist under the supplied key.
     *
     * @author Dmitriy Moroz
     */
    private fun putInt(key: String, value: Int) {
        keyValueStore.putInt(key, value)
        onPreferencesChanged()
    }

    /**
     * Replaces the settings invalidation token so active observers read a fresh snapshot.
     *
     * @author Dmitriy Moroz
     */
    private fun onPreferencesChanged() {
        preferencesChangedState.value = Any()
    }

    /**
     * Resolves a stored ordinal, using the supplied default for values outside the current enum.
     *
     * @param index Persisted enum ordinal, which may be obsolete or out of range.
     * @param default Fallback enum entry for an unavailable stored ordinal.
     *
     * @author Dmitriy Moroz
     */
    private inline fun <reified T : Enum<T>> enumValueOrDefault(index: Int, default: T): T =
        enumValues<T>().getOrNull(index) ?: default

    /**
     * Stable preference keys and URL normalization markers used by shared application settings.
     *
     * @author Dmitriy Moroz
     */
    companion object {
        /**
         * Txt2img sampling step preference.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_TEXT_TO_IMAGE_SAMPLING_STEPS = "key_text_to_image_sampling_steps"

        /**
         * Txt2img CFG preference, encoded as a decimal string.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_TEXT_TO_IMAGE_CFG_SCALE = "key_text_to_image_cfg_scale"

        /**
         * Img2img sampling step preference.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_IMAGE_TO_IMAGE_SAMPLING_STEPS = "key_image_to_image_sampling_steps"

        /**
         * Img2img CFG preference, encoded as a decimal string.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_IMAGE_TO_IMAGE_CFG_SCALE = "key_image_to_image_cfg_scale"

        /**
         * Independent provider connection timeout in seconds, with zero meaning no limit.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_CONNECTION_TIMEOUT_SECONDS = "key_connection_timeout_seconds"

        /**
         * Stores seconds, with zero representing an unlimited generation wait.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_GENERATION_REQUEST_TIMEOUT_SECONDS = "key_generation_request_timeout_seconds"

        /**
         * Stable storage key `key_server_url`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SERVER_URL = "key_server_url"

        /**
         * Stable storage key `key_swarm_server_url`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SWARM_SERVER_URL = "key_swarm_server_url"

        /**
         * Stable storage key `key_easy_diffusion_url`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        private const val KEY_EASY_DIFFUSION_URL = "key_easy_diffusion_url"

        private const val KEY_EASY_DIFFUSION_MODEL = "key_easy_diffusion_model"

        /**
         * Stable preference key for the selected SwarmUI checkpoint identifier.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SWARM_MODEL = "key_swarm_model"

        /**
         * Stable storage key `key_demo_mode`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DEMO_MODE = "key_demo_mode"

        /**
         * Stable storage key `key_developer_mode`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DEVELOPER_MODE = "key_developer_mode"

        /**
         * Stable storage key `key_local_diffusion_custom_model_path`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH = "key_local_diffusion_custom_model_path"

        /**
         * Stable storage key `key_mediapipe_custom_model_path`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH = "key_mediapipe_custom_model_path"

        /**
         * Stable storage key `key_core_ml_custom_model_path`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_CORE_ML_CUSTOM_MODEL_PATH = "key_core_ml_custom_model_path"

        /**
         * Stable storage key `key_bonsai_custom_model_path`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BONSAI_CUSTOM_MODEL_PATH = "key_bonsai_custom_model_path"

        /**
         * Stable storage key `key_sdxl_custom_model_path`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SDXL_CUSTOM_MODEL_PATH = "key_sdxl_custom_model_path"

        /**
         * Stable storage key `key_allow_local_diffusion_cancel`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_ALLOW_LOCAL_DIFFUSION_CANCEL = "key_allow_local_diffusion_cancel"

        /**
         * Stable storage key `key_local_diffusion_scheduler_thread`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_DIFFUSION_SCHEDULER_THREAD = "key_local_diffusion_scheduler_thread"

        /**
         * Stable storage key `key_monitor_connection`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_MONITOR_CONNECTIVITY = "key_monitor_connection"

        /**
         * Stable storage key `key_ai_auto_save`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_AI_AUTO_SAVE = "key_ai_auto_save"

        /**
         * Stable storage key `key_save_to_media_store`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SAVE_TO_MEDIA_STORE = "key_save_to_media_store"

        /**
         * Stable storage key `key_always_show_advanced_options`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_FORM_ALWAYS_SHOW_ADVANCED_OPTIONS = "key_always_show_advanced_options"

        /**
         * Stable storage key `key_prompt_tagged_input_kb`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_FORM_PROMPT_TAGGED_INPUT = "key_prompt_tagged_input_kb"

        /**
         * Stable storage key `key_sd_model`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SD_MODEL = "key_sd_model"

        /**
         * Stable storage key `key_horde_api_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_HORDE_API_KEY = "key_horde_api_key"
        const val KEY_OPEN_AI_COMPATIBLE_URL = "key_open_ai_compatible_url"
        const val KEY_OPEN_AI_COMPATIBLE_API_KEY = "key_open_ai_compatible_api_key"
        const val KEY_OPEN_AI_COMPATIBLE_MODEL = "key_open_ai_compatible_model"

        /**
         * Stable storage key `key_open_ai_api_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_OPEN_AI_API_KEY = "key_open_ai_api_key"

        /**
         * Stable storage key `key_hugging_face_api_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_HUGGING_FACE_API_KEY = "key_hugging_face_api_key"

        /**
         * Stable storage key `key_hugging_face_model_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_HUGGING_FACE_MODEL_KEY = "key_hugging_face_model_key"

        /**
         * Stable storage key `key_stability_ai_api_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_STABILITY_AI_API_KEY = "key_stability_ai_api_key"

        /**
         * Stable storage key `key_fal_ai_api_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_FAL_AI_API_KEY = "key_fal_ai_api_key"

        /**
         * Stable storage key `key_arli_ai_api_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_ARLI_AI_API_KEY = "key_arli_ai_api_key"

        /**
         * Stable storage key `key_arli_ai_model_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_ARLI_AI_MODEL_KEY = "key_arli_ai_model_key"

        /**
         * Stable storage key `key_stability_ai_engine_id_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_STABILITY_AI_ENGINE_ID_KEY = "key_stability_ai_engine_id_key"

        /**
         * Stable storage key `key_on_boarding_complete`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_ON_BOARDING_COMPLETE = "key_on_boarding_complete"

        /**
         * Stable storage key `force_upd_setup_v0.x.x-v0.6.2`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_FORCE_SETUP_AFTER_UPDATE = "force_upd_setup_v0.x.x-v0.6.2"

        /**
         * Stable storage key `key_mediapipe_model_id`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_MEDIA_PIPE_MODEL_ID = "key_mediapipe_model_id"

        /**
         * Stable storage key `key_core_ml_model_id`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_CORE_ML_MODEL_ID = "key_core_ml_model_id"

        /**
         * Stable storage key `key_bonsai_model_id`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BONSAI_MODEL_ID = "key_bonsai_model_id"
        const val KEY_MNN_MODEL_PATH = "key_mnn_model_path"
        const val KEY_MNN_CUSTOM_ACTIVATION_ID = "key_mnn_custom_activation_id"
        const val KEY_MNN_ACTIVATION_ID = "key_mnn_activation_id"
        const val KEY_MNN_MODEL_ID = "key_mnn_model_id"
        const val KEY_MNN_BACKEND = "key_mnn_backend"

        /**
         * Stable storage key `key_sdxl_model_id`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_SDXL_MODEL_ID = "key_sdxl_model_id"

        /**
         * Stable storage key `key_local_model_id`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_MODEL_ID = "key_local_model_id"

        /**
         * Stable storage key `key_local_nn_api`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_NN_API = "key_local_nn_api"

        /**
         * Stable storage key `key_design_dynamic_colors`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_DYNAMIC_COLORS = "key_design_dynamic_colors"

        /**
         * Stable storage key `key_design_system_dark_theme`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_SYSTEM_DARK_THEME = "key_design_system_dark_theme"

        /**
         * Stable storage key `key_design_dark_theme`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_DARK_THEME = "key_design_dark_theme"

        /**
         * Stable storage key `key_design_color_token_theme`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_COLOR_TOKEN = "key_design_color_token_theme"

        /**
         * Stable storage key `key_design_dark_color_token_theme`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_DESIGN_DARK_TOKEN = "key_design_dark_color_token_theme"

        /**
         * Stable storage key for the generation resource overlay preference.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_PERFORMANCE_OVERLAY = "key_performance_overlay"

        /**
         * Stable storage key for the opt-in foreground display dimming preference.
         * @author Dmitriy Moroz
         */
        const val KEY_DIM_SCREEN = "key_dim_screen"

        /** Stable storage key for background generation scheduling.
         * @author Dmitriy Moroz */
        const val KEY_BACKGROUND_GENERATION = "key_background_generation"

        /**
         * Stable storage key `key_background_process_count`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BACKGROUND_PROCESS_COUNT = "key_background_process_count"

        /**
         * Stable storage key `key_local_generation_benchmark_prompt_answered`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LOCAL_GENERATION_BENCHMARK_PROMPT_ANSWERED =
            "key_local_generation_benchmark_prompt_answered"

        /**
         * Stable storage key `key_benchmark_recommendation_warning_suppressed`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_BENCHMARK_RECOMMENDATION_WARNING_SUPPRESSED =
            "key_benchmark_recommendation_warning_suppressed"

        /**
         * Stable storage key `key_gallery_grid`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_GALLERY_GRID = "key_gallery_grid"

        /**
         * Stable storage key `key_language_code`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_LANGUAGE_CODE = "key_language_code"
    }
}

/**
 * Returns the platform default for exporting saved results to the system photo store.
 *
 * @author Dmitriy Moroz
 */
internal expect fun defaultSaveToMediaStore(): Boolean
