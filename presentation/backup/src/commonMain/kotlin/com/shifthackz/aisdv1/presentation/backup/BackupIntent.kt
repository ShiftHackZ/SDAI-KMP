package com.shifthackz.aisdv1.presentation.backup

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory

/**
 * Explicit backup actions and native document results; no action contains decoded archive data.
 *
 * @author Dmitriy Moroz
 */
sealed interface BackupIntent : MviIntent {
    /**
     * Returns to Settings or mandatory first-launch configuration after resolving staged ownership.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : BackupIntent

    /**
     * Consumes the first-launch invitation and proceeds through provider configuration.
     *
     * @author Dmitriy Moroz
     */
    data object Continue : BackupIntent

    /**
     * Opens restore-only content while persisting first-launch recovery progression.
     *
     * @author Dmitriy Moroz
     */
    data object BeginInitialRestore : BackupIntent

    /**
     * Changes visible tab while retaining each page selection.
     *
     * @param isRestore Whether the Restore tab should be selected.
     * @author Dmitriy Moroz
     */
    data class SelectTab(val isRestore: Boolean) : BackupIntent

    /**
     * Changes one available category selection.
     *
     * @param category Stable category represented by the checklist row.
     * @author Dmitriy Moroz
     */
    data class ToggleCategory(val category: BackupCategory) : BackupIntent

    /**
     * Changes one supported provider child selection.
     *
     * @param key Stable provider wire identifier.
     * @author Dmitriy Moroz
     */
    data class ToggleProvider(val key: String) : BackupIntent

    /**
     * Expands or collapses provider choices without changing selection.
     *
     * @author Dmitriy Moroz
     */
    data object ToggleProvidersExpanded : BackupIntent

    /**
     * Opens a native picker while retaining the previous inspection until a new selection succeeds.
     *
     * @author Dmitriy Moroz
     */
    data object ChooseFile : BackupIntent

    /**
     * Starts export or submits the selected restore plan with its current retry identity.
     *
     * @author Dmitriy Moroz
     */
    data object Submit : BackupIntent

    /**
     * Confirms only the current displayed conflict fingerprint; changed destination values require renewed consent.
     *
     * @author Dmitriy Moroz
     */
    data object ConfirmProviderReplacement : BackupIntent

    /**
     * Cancels replacement before any category mutation and returns to editable selection.
     *
     * @author Dmitriy Moroz
     */
    data object DismissConflict : BackupIntent

    /**
     * Acknowledges feedback without clearing retry state; successful first-launch restore proceeds to setup.
     *
     * @author Dmitriy Moroz
     */
    data object DismissFeedback : BackupIntent

    /**
     * Cancels active bounded work; already committed categories remain journal-owned.
     *
     * @author Dmitriy Moroz
     */
    data object CancelWork : BackupIntent

    /**
     * Delivers a native import result; null is neutral picker cancellation.
     *
     * @param document Opaque native source reference or null.
     * @author Dmitriy Moroz
     */
    data class DocumentSelected(val document: String?) : BackupIntent

    /**
     * Delivers a destination or completed UIKit export; neither means cancellation.
     *
     * @param destination Android write-granted output reference, or null on iOS/cancel.
     * @param hasExported Whether UIKit confirmed completed native export.
     * @param handle Nonsecret staged identity saved by the launcher across host recreation.
     * @author Dmitriy Moroz
     */
    data class ExportFinished(
        val destination: String?,
        val hasExported: Boolean,
        val handle: String
    ) : BackupIntent
}
