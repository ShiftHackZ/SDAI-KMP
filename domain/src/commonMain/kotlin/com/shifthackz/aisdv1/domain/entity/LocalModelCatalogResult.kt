package com.shifthackz.aisdv1.domain.entity

/**
 * Refreshed or cached choices with an observable catalog transfer failure.
 *
 * @param models Current cached metadata, completion and selection; synthetic custom entries do not imply a cached catalog.
 * @param failure Transfer/persistence failure shown alongside usable choices, or null after a successful refresh.
 *
 * @author Dmitriy Moroz
 */
data class LocalModelCatalogResult(val models: List<LocalAiModel>, val failure: Throwable? = null)
