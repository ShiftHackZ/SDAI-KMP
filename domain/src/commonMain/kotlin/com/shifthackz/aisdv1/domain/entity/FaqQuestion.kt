package com.shifthackz.aisdv1.domain.entity

/**
 * A searchable question with an answer composed of plain-text paragraphs.
 *
 * @param id Stable question identifier within its category.
 * @param question Text displayed in the expandable row.
 * @param answer Ordered paragraphs displayed when the row is expanded.
 * @param tags Additional search terms supplied by the FAQ publisher.
 * @author Dmitriy Moroz
 */
data class FaqQuestion(
    val id: String,
    val question: String,
    val answer: List<String>,
    val tags: List<String>,
)
