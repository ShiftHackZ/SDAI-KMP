package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.Entity

/**
 * One cached question with enough category metadata to restore the published grouping and order.
 *
 * @param categoryId Stable category slug.
 * @param questionId Stable question slug within its category.
 * @param categoryLabel Published category name.
 * @param categoryPosition Zero-based category position in the feed.
 * @param questionPosition Zero-based question position within the category.
 * @param question Published question text.
 * @param answer Ordered answer paragraphs.
 * @param tags Additional search terms.
 * @author Dmitriy Moroz
 */
@Entity(tableName = "faq_questions", primaryKeys = ["categoryId", "questionId"])
data class FaqQuestionEntity(
    val categoryId: String,
    val questionId: String,
    val categoryLabel: String,
    val categoryPosition: Int,
    val questionPosition: Int,
    val question: String,
    val answer: List<String>,
    val tags: List<String>,
)
