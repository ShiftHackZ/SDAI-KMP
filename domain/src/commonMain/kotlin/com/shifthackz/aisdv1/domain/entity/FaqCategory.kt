package com.shifthackz.aisdv1.domain.entity

/**
 * Groups related questions in the order published by the SDAI website.
 *
 * @param id Stable identifier used to filter categories.
 * @param label Published category name.
 * @param items Questions and answers in publication order.
 * @author Dmitriy Moroz
 */
data class FaqCategory(
    val id: String,
    val label: String,
    val items: List<FaqQuestion>,
)
