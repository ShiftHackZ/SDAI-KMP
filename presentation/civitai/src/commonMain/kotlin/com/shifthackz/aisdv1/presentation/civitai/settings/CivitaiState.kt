package com.shifthackz.aisdv1.presentation.civitai.settings

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount

/**
 * Account settings without any credential material.
 *
 * @param account Validated saved account, absent when signed out.
 * @param isAutoUploadEnabled Explicit consent to publish subsequent generations.
 * @param isBusy Whether an account/storage operation owns the progress dialog.
 * @param hasFailed Whether the last operation failed without exposing transport or credential details.
 * @param shouldConfirmAutoUpload Whether public-sharing consent is awaiting confirmation.
 * @author Dmitriy Moroz
 */
data class CivitaiState(
    val account: CivitaiAccount? = null,
    val isAutoUploadEnabled: Boolean = false,
    val isBusy: Boolean = true,
    val hasFailed: Boolean = false,
    val shouldConfirmAutoUpload: Boolean = false,
) : MviState
