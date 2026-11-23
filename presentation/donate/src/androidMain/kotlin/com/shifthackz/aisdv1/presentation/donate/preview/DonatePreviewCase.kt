package com.shifthackz.aisdv1.presentation.donate.preview

import com.shifthackz.aisdv1.presentation.donate.DonateState

/**
 * Fixed donation-page content and its initial supporter-list position.
 *
 * @param name Describes the supporter data or loading state in the preview picker.
 * @param state Supporter records and loading status rendered without fetching data.
 * @param firstVisibleItem Initial lazy-list index, including the thanks heading at index zero.
 * @author Dmitriy Moroz
 */
internal data class DonatePreviewCase(
    val name: String,
    val state: DonateState,
    val firstVisibleItem: Int = 0,
)
