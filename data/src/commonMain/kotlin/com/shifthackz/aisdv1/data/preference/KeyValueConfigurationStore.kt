package com.shifthackz.aisdv1.data.preference

import com.shifthackz.aisdv1.domain.entity.MnnBackend

import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore

/**
 * Persists independent provider settings and publishes complete checked batches using the authorization commit marker.
 *
 * @param keyValueStore Platform preference store supporting complete durable configuration batches.
 *
 * @author Dmitriy Moroz
 */
internal class KeyValueConfigurationStore(

    private val keyValueStore: KeyValueStore,
) : ConfigurationStore {

    /**
     * Persisted AUTOMATIC1111 server root, independent from other providers.
     *
     * @author Dmitriy Moroz
     */
    override var automatic1111ServerUrl: String
        get() = keyValueStore.getString(KEY_SERVER_URL).fixUrlSlashes()
        set(value) = keyValueStore.putString(KEY_SERVER_URL, value.fixUrlSlashes())

    /**
     * Persisted SwarmUI server root; normalized without changing the protocol delimiter.
     *
     * @author Dmitriy Moroz
     */
    override var swarmUiServerUrl: String
        get() = keyValueStore.getString(KEY_SWARM_SERVER_URL).fixUrlSlashes()
        set(value) = keyValueStore.putString(KEY_SWARM_SERVER_URL, value.fixUrlSlashes())

    /**
     * HTTP(S) root or reverse-proxy prefix; changing the selected instance advances its revision atomically.
     *
     * @author Dmitriy Moroz
     */
    override var easyDiffusionUrl: String
        get() = keyValueStore.getString(KEY_EASY_DIFFUSION_URL, "")
        set(value) = keyValueStore.writeEasyDiffusionSetting(KEY_EASY_DIFFUSION_URL, value)

    /**
     * Complete checkpoint identifier; changing the selected Easy Diffusion model invalidates captured work.
     *
     * @author Dmitriy Moroz
     */
    override var easyDiffusionModel: String
        get() = keyValueStore.getString(KEY_EASY_DIFFUSION_MODEL, "")
        set(value) = keyValueStore.writeEasyDiffusionSetting(KEY_EASY_DIFFUSION_MODEL, value)

    /**
     * Exact selected SwarmUI checkpoint name.
     *
     * @author Dmitriy Moroz
     */
    override var swarmUiModel: String
        get() = keyValueStore.getString(KEY_SWARM_MODEL)
        set(value) = keyValueStore.putString(KEY_SWARM_MODEL, value)

    /**
     * Whether AUTOMATIC1111 uses the configured demonstration endpoint.
     *
     * @author Dmitriy Moroz
     */
    override var demoMode: Boolean
        get() = keyValueStore.getBoolean(KEY_DEMO_MODE)
        set(value) = keyValueStore.putBoolean(KEY_DEMO_MODE, value)

    /**
     * Selected provider; replacement increments the durable selection revision.
     *
     * @author Dmitriy Moroz
     */
    override var source: ServerSource
        get() = keyValueStore.readProviderSelection().first
        set(value) = keyValueStore.writeProviderSelection(value)

    override val providerSelectionRevision: Long
        get() = keyValueStore.readProviderSelection().second

    /**
     * AI Horde credential; the anonymous key is supported independently of other services.
     *
     * @author Dmitriy Moroz
     */
    override var hordeApiKey: String
        get() = keyValueStore.getString(KEY_HORDE_API_KEY)
        set(value) = keyValueStore.putString(KEY_HORDE_API_KEY, value)

    /**
     * Complete HTTP(S) API prefix, including any version and reverse-proxy path.
     *
     * @author Dmitriy Moroz
     */
    override var openAiCompatibleUrl: String
        get() = keyValueStore.getString(KEY_OPEN_AI_COMPATIBLE_URL, "")
        set(value) = keyValueStore.putString(KEY_OPEN_AI_COMPATIBLE_URL, value)

    /**
     * Optional bearer token for the independent compatible server.
     *
     * @author Dmitriy Moroz
     */
    override var openAiCompatibleApiKey: String
        get() = keyValueStore.getString(KEY_OPEN_AI_COMPATIBLE_API_KEY, "")
        set(value) = keyValueStore.putString(KEY_OPEN_AI_COMPATIBLE_API_KEY, value)

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
        set(value) = keyValueStore.putString(KEY_OPEN_AI_API_KEY, value)

    /**
     * Hugging Face inference token.
     *
     * @author Dmitriy Moroz
     */
    override var huggingFaceApiKey: String
        get() = keyValueStore.getString(KEY_HUGGING_FACE_API_KEY)
        set(value) = keyValueStore.putString(KEY_HUGGING_FACE_API_KEY, value)

    /**
     * Selected supported Hugging Face inference model alias.
     *
     * @author Dmitriy Moroz
     */
    override var huggingFaceModel: String
        get() = keyValueStore.getString(KEY_HUGGING_FACE_MODEL_KEY, HuggingFaceModel.default.alias)
        set(value) = keyValueStore.putString(KEY_HUGGING_FACE_MODEL_KEY, value)

    /**
     * Stability AI API credential.
     *
     * @author Dmitriy Moroz
     */
    override var stabilityAiApiKey: String
        get() = keyValueStore.getString(KEY_STABILITY_AI_API_KEY)
        set(value) = keyValueStore.putString(KEY_STABILITY_AI_API_KEY, value)

    /**
     * Fal.ai API credential.
     *
     * @author Dmitriy Moroz
     */
    override var falAiApiKey: String
        get() = keyValueStore.getString(KEY_FAL_AI_API_KEY)
        set(value) = keyValueStore.putString(KEY_FAL_AI_API_KEY, value)

    /**
     * ArliAI API credential.
     *
     * @author Dmitriy Moroz
     */
    override var arliAiApiKey: String
        get() = keyValueStore.getString(KEY_ARLI_AI_API_KEY)
        set(value) = keyValueStore.putString(KEY_ARLI_AI_API_KEY, value)

    /**
     * Selected Stability AI engine identifier.
     *
     * @author Dmitriy Moroz
     */
    override var stabilityAiEngineId: String
        get() = keyValueStore.getString(KEY_STABILITY_AI_ENGINE_ID_KEY)
        set(value) = keyValueStore.putString(KEY_STABILITY_AI_ENGINE_ID_KEY, value)

    /**
     * Selected downloaded ONNX model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxModelId: String
        get() = keyValueStore.getString(KEY_LOCAL_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_LOCAL_MODEL_ID, value)

    /**
     * Filesystem path to the selected custom ONNX model.
     *
     * @author Dmitriy Moroz
     */
    override var localOnnxModelPath: String
        get() = keyValueStore.getString(KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH, value)

    /**
     * Selected downloaded MediaPipe model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localMediaPipeModelId: String
        get() = keyValueStore.getString(KEY_MEDIA_PIPE_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_MEDIA_PIPE_MODEL_ID, value)

    /**
     * Filesystem path to the selected custom MediaPipe model.
     *
     * @author Dmitriy Moroz
     */
    override var localMediaPipeModelPath: String
        get() = keyValueStore.getString(KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH, value)

    /**
     * Selected stable-diffusion.cpp model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localSdxlModelId: String
        get() = keyValueStore.getString(KEY_SDXL_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_SDXL_MODEL_ID, value)

    /**
     * Filesystem path to the selected custom stable-diffusion.cpp model.
     *
     * @author Dmitriy Moroz
     */
    override var localSdxlModelPath: String
        get() = keyValueStore.getString(KEY_SDXL_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_SDXL_CUSTOM_MODEL_PATH, value)

    /**
     * Selected downloaded Core ML model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localCoreMlModelId: String
        get() = keyValueStore.getString(KEY_CORE_ML_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_CORE_ML_MODEL_ID, value)

    /**
     * Filesystem path to the selected custom Core ML model.
     *
     * @author Dmitriy Moroz
     */
    override var localCoreMlModelPath: String
        get() = keyValueStore.getString(KEY_CORE_ML_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_CORE_ML_CUSTOM_MODEL_PATH, value)

    /**
     * External custom bundle source shown in setup; generation reads its immutable private snapshot.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnModelPath: String
        get() = keyValueStore.getString(KEY_MNN_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_MNN_MODEL_PATH, value)

    /**
     * Last committed immutable custom manifest identity, retained across catalog selections.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnCustomActivationId: String
        get() = keyValueStore.getString(KEY_MNN_CUSTOM_ACTIVATION_ID)
        set(value) = keyValueStore.putString(KEY_MNN_CUSTOM_ACTIVATION_ID, value)

    /**
     * Manifest SHA256 captured with the selected model and queued generation inputs.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnActivationId: String
        get() = keyValueStore.getString(KEY_MNN_ACTIVATION_ID)
        set(value) = keyValueStore.putString(KEY_MNN_ACTIVATION_ID, value)

    /**
     * Selected catalog or custom UI identity; generation also requires the captured manifest SHA256.
     *
     * @author Dmitriy Moroz
     */
    override var localMnnModelId: String
        get() = keyValueStore.getString(KEY_MNN_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_MNN_MODEL_ID, value)

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
        set(value) = keyValueStore.putString(KEY_MNN_BACKEND, value.key)

    /**
     * Selected downloaded Bonsai model identifier.
     *
     * @author Dmitriy Moroz
     */
    override var localBonsaiModelId: String
        get() = keyValueStore.getString(KEY_BONSAI_MODEL_ID)
        set(value) = keyValueStore.putString(KEY_BONSAI_MODEL_ID, value)

    /**
     * Filesystem path to the selected custom Bonsai model.
     *
     * @author Dmitriy Moroz
     */
    override var localBonsaiModelPath: String
        get() = keyValueStore.getString(KEY_BONSAI_CUSTOM_MODEL_PATH)
        set(value) = keyValueStore.putString(KEY_BONSAI_CUSTOM_MODEL_PATH, value)

    private var pendingStrings: Map<String, String>? = null
    private var pendingBooleans: Map<String, Boolean>? = null

    /**
     * Refuses to certify SharedPreferences memory following any failed checked batch.
     *
     * @author Dmitriy Moroz
     */
    override fun getAuthorizationCommitId(): String {
        if (pendingStrings != null) throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
        return keyValueStore.getString("key_authorization_commit_id")
    }

    /**
     * Persists a complete existing-format snapshot and association together, retaining failed intent.
     *
     * @param configuration Validated complete candidate.
     * @param commitId Opaque association of staged protected credentials.
     *
     * @author Dmitriy Moroz
     */
    override fun commitConfiguration(configuration: Configuration, commitId: String) {
        retryPendingConfiguration()
        val previous = keyValueStore.readProviderSelection()
        val hasEasyDiffusionConnectionChanged = previous.first == ServerSource.EASY_DIFFUSION &&
                configuration.source == ServerSource.EASY_DIFFUSION &&
                (easyDiffusionUrl != configuration.easyDiffusionUrl || easyDiffusionModel != configuration.easyDiffusionModel)
        val revision =
            if (previous.first == configuration.source && !hasEasyDiffusionConnectionChanged) previous.second else {
                check(previous.second < Long.MAX_VALUE) { "Provider selection revision exhausted" }
                previous.second + 1
            }
        pendingStrings = mapOf(
            KEY_SERVER_URL to configuration.serverUrl.fixUrlSlashes(),
            KEY_SWARM_SERVER_URL to configuration.swarmUiUrl.fixUrlSlashes(),
            KEY_EASY_DIFFUSION_URL to configuration.easyDiffusionUrl,
            KEY_EASY_DIFFUSION_MODEL to configuration.easyDiffusionModel,
            KEY_SWARM_MODEL to configuration.swarmUiModel,
            KEY_HORDE_API_KEY to configuration.hordeApiKey,
            KEY_OPEN_AI_API_KEY to configuration.openAiApiKey,
            KEY_OPEN_AI_COMPATIBLE_MODEL to configuration.openAiCompatibleModel,
            KEY_OPEN_AI_COMPATIBLE_API_KEY to configuration.openAiCompatibleApiKey,
            KEY_OPEN_AI_COMPATIBLE_URL to configuration.openAiCompatibleUrl,
            KEY_HUGGING_FACE_API_KEY to configuration.huggingFaceApiKey,
            KEY_HUGGING_FACE_MODEL_KEY to configuration.huggingFaceModel,
            KEY_STABILITY_AI_API_KEY to configuration.stabilityAiApiKey,
            KEY_STABILITY_AI_ENGINE_ID_KEY to configuration.stabilityAiEngineId,
            KEY_FAL_AI_API_KEY to configuration.falAiApiKey,
            KEY_ARLI_AI_API_KEY to configuration.arliAiApiKey,
            KEY_LOCAL_MODEL_ID to configuration.localOnnxModelId,
            KEY_LOCAL_DIFFUSION_CUSTOM_MODEL_PATH to configuration.localOnnxModelPath,
            KEY_MEDIA_PIPE_MODEL_ID to configuration.localMediaPipeModelId,
            KEY_MEDIA_PIPE_CUSTOM_MODEL_PATH to configuration.localMediaPipeModelPath,
            KEY_SDXL_MODEL_ID to configuration.localSdxlModelId,
            KEY_SDXL_CUSTOM_MODEL_PATH to configuration.localSdxlModelPath,
            KEY_CORE_ML_MODEL_ID to configuration.localCoreMlModelId,
            KEY_CORE_ML_CUSTOM_MODEL_PATH to configuration.localCoreMlModelPath,
            KEY_BONSAI_MODEL_ID to configuration.localBonsaiModelId,
            KEY_MNN_CUSTOM_ACTIVATION_ID to configuration.localMnnCustomActivationId,
            KEY_MNN_ACTIVATION_ID to configuration.localMnnActivationId,
            KEY_MNN_MODEL_ID to configuration.localMnnModelId,
            KEY_MNN_MODEL_PATH to configuration.localMnnModelPath,
            KEY_MNN_BACKEND to configuration.localMnnBackend.key,
            KEY_BONSAI_CUSTOM_MODEL_PATH to configuration.localBonsaiModelPath,
            "key_provider_selection_state" to "${configuration.source.key}|$revision",
            "key_authorization_commit_id" to commitId,
        )
        pendingBooleans = mapOf(KEY_DEMO_MODE to configuration.demoMode)
        retryPendingConfiguration()
    }

    /**
     * Repeats the exact failed batch without advancing its captured revision or accepting memory state.
     *
     * @author Dmitriy Moroz
     */
    override fun retryPendingConfiguration(): Boolean {
        val strings = pendingStrings ?: return false
        try {
            keyValueStore.putConfigurationDurably(strings, pendingBooleans.orEmpty())
        } catch (_: Exception) {
            throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
        }
        pendingStrings = null
        pendingBooleans = null
        return true
    }

    /**
     * Normalizes repeated path slashes and trailing slashes while preserving the scheme delimiter.
     *
     * @author Dmitriy Moroz
     */
    private fun String.fixUrlSlashes(): String {
        if (isEmpty()) return this
        return replace("://", URL_PROTOCOL_MARKER)
            .replace("//", "/")
            .replace(URL_PROTOCOL_MARKER, "://")
            .trimEnd('/')
    }

    /**
     * Stable preference keys and defaults for the complete independent provider configuration batch.
     *
     * @author Dmitriy Moroz
     */
    private companion object {
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
         * Stable storage key `key_stability_ai_engine_id_key`; preserve its value to retain existing user settings.
         *
         * @author Dmitriy Moroz
         */
        const val KEY_STABILITY_AI_ENGINE_ID_KEY = "key_stability_ai_engine_id_key"

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
         * Temporary delimiter used to normalize URL path slashes without damaging the scheme.
         *
         * @author Dmitriy Moroz
         */
        const val URL_PROTOCOL_MARKER = "SDAI_URL_PROTOCOL_MARKER"
    }
}
