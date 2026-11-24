package com.shifthackz.aisdv1.domain.entity

/**
 * FAQ content resolved for the current screen visit.
 *
 * @param categories Published groups with at least one question.
 * @param isCached Whether the online fetch failed and the last saved copy was used.
 * @author Dmitriy Moroz
 */
data class FaqContent(
    val categories: List<FaqCategory>,
    val isCached: Boolean,
)
