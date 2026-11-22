package com.shifthackz.aisdv1.presentation.civitai.settings

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Account and explicit public-sharing actions.
 *
 * @author Dmitriy Moroz
 */
sealed interface CivitaiIntent : MviIntent {
    /**
     * Validates and stores a user-provided key.
     *
     * @param key API key, never logged.
     * @author Dmitriy Moroz
     */
    class Connect(val key: String) : CivitaiIntent
    data object SignIn : CivitaiIntent
    data object Disconnect : CivitaiIntent

    /**
     * Opens the persistent upload history independently of account authorization.
     *
     * @author Dmitriy Moroz
     */
    data object OpenHistory : CivitaiIntent
    data object EnableAutoUpload : CivitaiIntent
    data object ConfirmAutoUpload : CivitaiIntent
    data object DisableAutoUpload : CivitaiIntent
    data object Dismiss : CivitaiIntent
    data object Back : CivitaiIntent
}
