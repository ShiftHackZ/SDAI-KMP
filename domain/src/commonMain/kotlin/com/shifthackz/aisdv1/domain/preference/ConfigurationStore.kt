package com.shifthackz.aisdv1.domain.preference

import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Independent provider preference contract plus complete checked configuration replacement; credentials are supplied separately.
 *
 * @author Dmitriy Moroz
 */
interface ConfigurationStore {
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

    /** HTTP(S) root or reverse-proxy prefix; changing the selected instance advances its revision atomically.
     * @author Dmitriy Moroz
     */
    var easyDiffusionUrl: String

    /** Complete checkpoint identifier; changing the selected Easy Diffusion model invalidates captured work.
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
     * Selected provider; replacement increments the durable selection revision.
     *
     * @author Dmitriy Moroz
     */
    var source: ServerSource

    /**
     * Durable selection revision; source replacement or selected Easy Diffusion URL/model changes advance it.
     *
     * @author Dmitriy Moroz
     */
    val providerSelectionRevision: Long

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
     * Selected Stability AI engine identifier.
     *
     * @author Dmitriy Moroz
     */
    var stabilityAiEngineId: String

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
     * Selected downloaded ONNX model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxModelId: String

    /**
     * Filesystem path to the selected custom ONNX model.
     *
     * @author Dmitriy Moroz
     */
    var localOnnxModelPath: String

    /**
     * Selected downloaded MediaPipe model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localMediaPipeModelId: String

    /**
     * Filesystem path to the selected custom MediaPipe model.
     *
     * @author Dmitriy Moroz
     */
    var localMediaPipeModelPath: String

    /**
     * Selected stable-diffusion.cpp model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localSdxlModelId: String

    /**
     * Filesystem path to the selected custom stable-diffusion.cpp model.
     *
     * @author Dmitriy Moroz
     */
    var localSdxlModelPath: String

    /**
     * Selected downloaded Core ML model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localCoreMlModelId: String

    /**
     * Filesystem path to the selected custom Core ML model.
     *
     * @author Dmitriy Moroz
     */
    var localCoreMlModelPath: String

    /**
     * Selected downloaded Bonsai model identifier.
     *
     * @author Dmitriy Moroz
     */
    var localBonsaiModelId: String

    /**
     * Filesystem path to the selected custom Bonsai model.
     *
     * @author Dmitriy Moroz
     */
    var localBonsaiModelPath: String

    /**
     * Returns a trusted persisted credential/configuration association, empty for legacy data.
     * Fails while a previous configuration write has an untrusted memory snapshot.
     *
     * @author Dmitriy Moroz
     */
    fun getAuthorizationCommitId(): String

    /**
     * Persists all existing configuration keys and the association in one checked atomic batch.
     * Called on IO by the serialized configuration transition owner.
     *
     * @param configuration Complete candidate configuration.
     * @param commitId Opaque association supplied by protected credential staging.
     * @author Dmitriy Moroz
     */
    fun commitConfiguration(configuration: Configuration, commitId: String)

    /**
     * Retries an exact failed configuration batch on IO before its association can be trusted.
     * Returns false when there was no pending write; failures remain quarantined.
     *
     * @author Dmitriy Moroz
     */
    fun retryPendingConfiguration(): Boolean

    /**
     * Combines independent provider preferences with the supplied protected authorization snapshot.
     *
     * @param authCredentials Protected credential snapshot associated with this configuration read.
     * @author Dmitriy Moroz
     */
    fun getConfiguration(
        authCredentials: AuthorizationCredentials,
    ): Configuration = Configuration(
        serverUrl = automatic1111ServerUrl,
        swarmUiUrl = swarmUiServerUrl,
        easyDiffusionUrl = easyDiffusionUrl,
        easyDiffusionModel = easyDiffusionModel,
        swarmUiModel = swarmUiModel,
        demoMode = demoMode,
        source = source,
        providerSelectionRevision = providerSelectionRevision,
        hordeApiKey = hordeApiKey,
        openAiApiKey = openAiApiKey,
        openAiCompatibleModel = openAiCompatibleModel,
        openAiCompatibleApiKey = openAiCompatibleApiKey,
        openAiCompatibleUrl = openAiCompatibleUrl,
        huggingFaceApiKey = huggingFaceApiKey,
        huggingFaceModel = huggingFaceModel,
        stabilityAiApiKey = stabilityAiApiKey,
        stabilityAiEngineId = stabilityAiEngineId,
        falAiApiKey = falAiApiKey,
        arliAiApiKey = arliAiApiKey,
        authCredentials = authCredentials,
        localOnnxModelId = localOnnxModelId,
        localOnnxModelPath = localOnnxModelPath,
        localMediaPipeModelId = localMediaPipeModelId,
        localMediaPipeModelPath = localMediaPipeModelPath,
        localSdxlModelId = localSdxlModelId,
        localSdxlModelPath = localSdxlModelPath,
        localCoreMlModelId = localCoreMlModelId,
        localCoreMlModelPath = localCoreMlModelPath,
        localBonsaiModelId = localBonsaiModelId,
        localBonsaiModelPath = localBonsaiModelPath,
    )

    /**
     * Writes all provider fields while retaining independent settings for inactive sources; production checked publication uses commitConfiguration.
     *
     * @param configuration Complete replacement containing independent settings for every provider.
     * @author Dmitriy Moroz
     */
    fun setConfiguration(configuration: Configuration) {
        source = configuration.source
        automatic1111ServerUrl = configuration.serverUrl
        swarmUiServerUrl = configuration.swarmUiUrl
        easyDiffusionUrl = configuration.easyDiffusionUrl
        easyDiffusionModel = configuration.easyDiffusionModel
        swarmUiModel = configuration.swarmUiModel
        demoMode = configuration.demoMode
        hordeApiKey = configuration.hordeApiKey
        openAiApiKey = configuration.openAiApiKey
        openAiCompatibleModel = configuration.openAiCompatibleModel
        openAiCompatibleApiKey = configuration.openAiCompatibleApiKey
        openAiCompatibleUrl = configuration.openAiCompatibleUrl
        huggingFaceApiKey = configuration.huggingFaceApiKey
        huggingFaceModel = configuration.huggingFaceModel
        stabilityAiApiKey = configuration.stabilityAiApiKey
        stabilityAiEngineId = configuration.stabilityAiEngineId
        falAiApiKey = configuration.falAiApiKey
        arliAiApiKey = configuration.arliAiApiKey
        localOnnxModelId = configuration.localOnnxModelId
        localOnnxModelPath = configuration.localOnnxModelPath
        localMediaPipeModelId = configuration.localMediaPipeModelId
        localMediaPipeModelPath = configuration.localMediaPipeModelPath
        localSdxlModelId = configuration.localSdxlModelId
        localSdxlModelPath = configuration.localSdxlModelPath
        localCoreMlModelId = configuration.localCoreMlModelId
        localCoreMlModelPath = configuration.localCoreMlModelPath
        localBonsaiModelId = configuration.localBonsaiModelId
        localBonsaiModelPath = configuration.localBonsaiModelPath
    }
}
