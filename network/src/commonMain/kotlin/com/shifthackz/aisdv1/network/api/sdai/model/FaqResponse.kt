package com.shifthackz.aisdv1.network.api.sdai.model

import kotlinx.serialization.Serializable

/**
 * Public website FAQ feed served by the SDAI metadata host.
 *
 * @param categories Category groups in website order.
 * @author Dmitriy Moroz
 */
@Serializable
data class FaqResponse(val categories: List<Category>) {
    /**
     * A website section with a stable ID and an ordered question list.
     *
     * @param id Stable category slug.
     * @param label Display label published by the website.
     * @param items Questions in website order.
     * @author Dmitriy Moroz
     */
    @Serializable
    data class Category(val id: String, val label: String, val items: List<Question>)

    /**
     * Published plain-text question, answer, and search metadata.
     *
     * @param id Stable question slug.
     * @param question Text shown in the collapsed row.
     * @param answer Ordered plain-text paragraphs.
     * @param tags Additional search terms.
     * @author Dmitriy Moroz
     */
    @Serializable
    data class Question(
        val id: String,
        val question: String,
        val answer: List<String>,
        val tags: List<String>,
    )
}
