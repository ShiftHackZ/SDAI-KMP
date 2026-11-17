package com.shifthackz.aisdv1.domain.entity.mnn

import com.shifthackz.aisdv1.domain.entity.LocalAiModel

/**
 * Result of catalog refresh, retaining an explicit failure alongside usable local choices.
 *
 * @param models Cached or refreshed catalog entries plus the flavor-appropriate custom setup entry.
 * @param failure Fetch failure retained for visible retry; a synthetic custom entry never implies cached metadata.
 *
 * @author Dmitriy Moroz
 */
data class MnnCatalogResult(val models: List<LocalAiModel>, val failure: Throwable? = null)
