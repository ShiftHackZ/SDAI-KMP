package com.shifthackz.aisdv1.presentation.backup

import kotlin.math.abs

/**
 * Keeps the selected page's actions stable during incidental horizontal movement in a vertical scroll.
 * Actions pause once a swipe has moved at least forty percent of one page away from that selection.
 *
 * @param selectedPage Page owning the active MVI selection and bottom action.
 * @param currentPage Pager's nearest page; this changes halfway through a swipe.
 * @param currentPageOffsetFraction Signed displacement from the nearest page in page-width units.
 * @author Dmitriy Moroz
 */
internal fun canInteractWithBackupPage(
    selectedPage: Int,
    currentPage: Int,
    currentPageOffsetFraction: Float,
): Boolean = abs(currentPage - selectedPage + currentPageOffsetFraction) < 0.4f
