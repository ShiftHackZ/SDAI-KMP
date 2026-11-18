package com.shifthackz.aisdv1.domain.entity

import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Complete provider configuration read by setup and generation screens.
 *
 * @param authorizationStorageFailure Non-persisted credential error; authCredentials is unavailable when set.
 * @param providerSelectionRevision Durable revision of the selected source; changes on source replacement.
 * @param serverUrl AUTOMATIC1111 server root used for metadata and generation.
 * @param easyDiffusionUrl HTTP(S) root or reverse-proxy prefix of the anonymous Easy Diffusion instance.
 * @param easyDiffusionModel Complete Easy Diffusion checkpoint identifier, including folder separators.
 * @param swarmUiUrl SwarmUI server root, independent from other self-hosted connections.
 * @param swarmUiModel Exact selected SwarmUI checkpoint name.
 * @param demoMode Whether AUTOMATIC1111 uses the configured demonstration endpoint.
 * @param source Selected provider; replacement increments the durable selection revision.
 * @param hordeApiKey AI Horde credential; the anonymous key is supported independently of other services.
 * @param openAiApiKey Official OpenAI API key; separate from compatible servers and OAuth sessions.
 * @param openAiCompatibleModel Editable image model identifier supplied to the compatible server.
 * @param openAiCompatibleApiKey Optional bearer token for the independent compatible server.
 * @param openAiCompatibleUrl Complete HTTP(S) API prefix, including any version and reverse-proxy path.
 * @param huggingFaceApiKey Hugging Face inference token.
 * @param huggingFaceModel Selected supported Hugging Face inference model alias.
 * @param stabilityAiApiKey Stability AI API credential.
 * @param stabilityAiEngineId Selected Stability AI engine identifier.
 * @param falAiApiKey Fal.ai API credential.
 * @param arliAiApiKey ArliAI API credential.
 * @param authCredentials Shared self-hosted HTTP credentials; anonymous providers publish None.
 * @param localOnnxModelId Selected downloaded ONNX model identifier.
 * @param localOnnxModelPath Filesystem path to the selected custom ONNX model.
 * @param localMediaPipeModelId Selected downloaded MediaPipe model identifier.
 * @param localMediaPipeModelPath Filesystem path to the selected custom MediaPipe model.
 * @param localSdxlModelId Selected stable-diffusion.cpp model identifier.
 * @param localSdxlModelPath Filesystem path to the selected custom stable-diffusion.cpp model.
 * @param localCoreMlModelId Selected downloaded Core ML model identifier.
 * @param localCoreMlModelPath Filesystem path to the selected custom Core ML model.
 * @param localBonsaiModelId Selected downloaded Bonsai model identifier.
 * @param localMnnCustomActivationId Last committed owned custom manifest identity, retained across catalog selections.
 * @param localMnnActivationId SHA256 of the selected immutable MNN manifest.
 * @param localMnnModelId Selected complete MNN catalog or custom snapshot identity.
 * @param localMnnModelPath External custom bundle source shown in setup.
 * @param localMnnBackend Independent requested CPU/OpenCL backend.
 * @param localBonsaiModelPath Filesystem path to the selected custom Bonsai model.
 *
 * @author Dmitriy Moroz
 */
data class Configuration(
    val authorizationStorageFailure: AuthorizationStorageFailure.Reason? = null,
    /**
     * Revision read from provider-selection storage; configuration writes never restore an old revision.
     *
     * @author Dmitriy Moroz
     */
    val providerSelectionRevision: Long = 0L,
    /**
     * AUTOMATIC1111 server root used for metadata and generation.
     *
     * @author Dmitriy Moroz
     */
    val serverUrl: String = "",
    /**
     * HTTP(S) root or reverse-proxy prefix of the anonymous Easy Diffusion instance.
     *
     * @author Dmitriy Moroz
     */
    val easyDiffusionUrl: String = "",
    /**
     * Complete checkpoint identifier from the Easy Diffusion stable-diffusion catalog.
     *
     * @author Dmitriy Moroz
     */
    val easyDiffusionModel: String = "",
    /**
     * SwarmUI server root, independent from other self-hosted connections.
     *
     * @author Dmitriy Moroz
     */
    val swarmUiUrl: String = "",
    /**
     * Exact selected SwarmUI checkpoint name.
     *
     * @author Dmitriy Moroz
     */
    val swarmUiModel: String = "",
    /**
     * Whether AUTOMATIC1111 uses the configured demonstration endpoint.
     *
     * @author Dmitriy Moroz
     */
    val demoMode: Boolean = false,
    /**
     * Selected provider; replacement increments the durable selection revision.
     *
     * @author Dmitriy Moroz
     */
    val source: ServerSource = ServerSource.AUTOMATIC1111,
    /**
     * AI Horde credential; the anonymous key is supported independently of other services.
     *
     * @author Dmitriy Moroz
     */
    val hordeApiKey: String = "",
    /**
     * Official OpenAI API key; separate from compatible servers and OAuth sessions.
     *
     * @author Dmitriy Moroz
     */
    val openAiApiKey: String = "",
    /**
     * Editable image model identifier supplied to the compatible server.
     *
     * @author Dmitriy Moroz
     */
    val openAiCompatibleModel: String = "gpt-image-2",
    /**
     * Optional bearer token for the independent compatible server.
     *
     * @author Dmitriy Moroz
     */
    val openAiCompatibleApiKey: String = "",
    /**
     * Complete HTTP(S) API prefix, including any version and reverse-proxy path.
     *
     * @author Dmitriy Moroz
     */
    val openAiCompatibleUrl: String = "",
    /**
     * Hugging Face inference token.
     *
     * @author Dmitriy Moroz
     */
    val huggingFaceApiKey: String = "",
    /**
     * Selected supported Hugging Face inference model alias.
     *
     * @author Dmitriy Moroz
     */
    val huggingFaceModel: String = "",
    /**
     * Stability AI API credential.
     *
     * @author Dmitriy Moroz
     */
    val stabilityAiApiKey: String = "",
    /**
     * Selected Stability AI engine identifier.
     *
     * @author Dmitriy Moroz
     */
    val stabilityAiEngineId: String = "",
    /**
     * Fal.ai API credential.
     *
     * @author Dmitriy Moroz
     */
    val falAiApiKey: String = "",
    /**
     * ArliAI API credential.
     *
     * @author Dmitriy Moroz
     */
    val arliAiApiKey: String = "",
    /**
     * Shared self-hosted HTTP credentials; anonymous providers publish None.
     *
     * @author Dmitriy Moroz
     */
    val authCredentials: AuthorizationCredentials = AuthorizationCredentials.None,
    /**
     * Selected downloaded ONNX model identifier.
     *
     * @author Dmitriy Moroz
     */
    val localOnnxModelId: String = "",
    /**
     * Filesystem path to the selected custom ONNX model.
     *
     * @author Dmitriy Moroz
     */
    val localOnnxModelPath: String = "",
    /**
     * Selected downloaded MediaPipe model identifier.
     *
     * @author Dmitriy Moroz
     */
    val localMediaPipeModelId: String = "",
    /**
     * Filesystem path to the selected custom MediaPipe model.
     *
     * @author Dmitriy Moroz
     */
    val localMediaPipeModelPath: String = "",
    /**
     * Selected stable-diffusion.cpp model identifier.
     *
     * @author Dmitriy Moroz
     */
    val localSdxlModelId: String = "",
    /**
     * Filesystem path to the selected custom stable-diffusion.cpp model.
     *
     * @author Dmitriy Moroz
     */
    val localSdxlModelPath: String = "",
    /**
     * Selected downloaded Core ML model identifier.
     *
     * @author Dmitriy Moroz
     */
    val localCoreMlModelId: String = "",
    /**
     * Filesystem path to the selected custom Core ML model.
     *
     * @author Dmitriy Moroz
     */
    val localCoreMlModelPath: String = "",
    /**
     * Selected downloaded Bonsai model identifier.
     *
     * @author Dmitriy Moroz
     */
    val localBonsaiModelId: String = "",
    val localMnnCustomActivationId: String = "",
    val localMnnActivationId: String = "",
    val localMnnModelId: String = "",
    val localMnnModelPath: String = "",
    val localMnnBackend: MnnBackend = MnnBackend.CPU,
    /**
     * Filesystem path to the selected custom Bonsai model.
     *
     * @author Dmitriy Moroz
     */
    val localBonsaiModelPath: String = "",
)
