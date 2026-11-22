package com.shifthackz.aisdv1.presentation.civitai.history

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload

/**
 * Retains ordered identities and only the upload receipts around the visible history viewport.
 *
 * @param itemIds Stable newest-first list positions, independent of sign-in state.
 * @param uploads Bounded metadata window; offscreen receipts are released as the viewport moves.
 * @param isLoading Whether the first identity read is pending.
 * @param hasFailed Whether a storage read failed and the user may retry it.
 * @author Dmitriy Moroz
 */
data class CivitaiHistoryState(
    val itemIds: List<Long> = emptyList(),
    val uploads: List<CivitaiUpload> = emptyList(),
    val isLoading: Boolean = true,
    val hasFailed: Boolean = false,
) : MviState
