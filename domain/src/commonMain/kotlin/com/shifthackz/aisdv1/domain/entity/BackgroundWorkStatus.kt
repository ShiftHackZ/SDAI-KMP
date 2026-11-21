package com.shifthackz.aisdv1.domain.entity

/**
 * Active background generation heading, stage and measured completion.
 *
 * @param running Whether generation is active.
 * @param statusTitle Localized operation heading.
 * @param statusSubTitle Localized queue, stage or sampling counters.
 * @param percentage Measured completion from zero to one hundred, or null when unknown.
 * @author Dmitriy Moroz
 */
data class BackgroundWorkStatus(
    val running: Boolean,
    val statusTitle: String,
    val statusSubTitle: String,
    val percentage: Int? = null,
)
