package com.shifthackz.aisdv1.presentation.configuration.setup.component

/**
 * Rendering decisions for the pinned provider download action, independent of animation timing.
 *
 * @param isVisible Whether the action and its footer gap occupy space.
 * @param isEnabled Whether the action accepts a start, switch-dialog or cancellation click.
 * @param isCancelling Whether the action stops the current provider's batch.
 *
 * @author Dmitriy Moroz
 */
internal data class LocalModelDownloadActionState(
    val isVisible: Boolean,
    val isEnabled: Boolean,
    val isCancelling: Boolean,
)
