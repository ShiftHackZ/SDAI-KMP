package com.shifthackz.aisdv1.domain.entity

/**
 * Selects one confirmed archive or all missing models from a provider catalog.
 * @param type Provider owning the requested model files.
 * @param modelId Confirmed single-model identity; null downloads the provider's missing catalog models.
 * @param sourceUrl Mirror chosen for the single model; null lets batch downloads use their first mirror.
 * @author Dmitriy Moroz
 */
data class ModelDownloadRequest(
    val type: LocalAiModel.Type,
    val modelId: String? = null,
    val sourceUrl: String? = null,
) {
    init {
        require((modelId == null) == (sourceUrl == null)) { "A single download requires both model identity and source URL" }
    }

    /**
     * Whether the request covers all missing catalog models rather than one confirmed archive.
     * @author Dmitriy Moroz
     */
    val isBatch: Boolean get() = modelId == null
}
