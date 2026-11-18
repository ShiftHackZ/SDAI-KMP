package com.shifthackz.aisdv1.presentation.debug

import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Developer controls derived from platform capabilities, preferences and capture state.
 *
 * @param isUiDiagnosticsSupported Whether this build can collect bounded UI diagnostics.
 * @param isUiDiagnosticsCapturing Whether explicit diagnostic capture is active.
 * @param screenModal Scheduler selection sheet currently displayed.
 * @param localDiffusionAllowCancel Saved local ONNX cancellation preference.
 * @param localDiffusionSchedulerThread Saved dispatcher selection for local ONNX work.
 * @param showWorkManagerSection Whether platform background-work controls are available.
 * @param showLocalDiffusionSection Whether local ONNX debug controls are available.
 * @param showQualityAssuranceSection Whether malformed-data insertion controls are shown.
 *
 * @author Dmitriy Moroz
 */
data class DebugMenuState(
    val isUiDiagnosticsSupported: Boolean = false,
    val isUiDiagnosticsCapturing: Boolean = false,
    val screenModal: DebugMenuModal = DebugMenuModal.None,
    val localDiffusionAllowCancel: Boolean = false,
    val localDiffusionSchedulerThread: SchedulersToken = SchedulersToken.COMPUTATION,
    val showWorkManagerSection: Boolean = false,
    val showLocalDiffusionSection: Boolean = false,
    val showQualityAssuranceSection: Boolean = true,
) : MviState

/**
 * Selection sheet displayed above the developer menu.
 *
 * @author Dmitriy Moroz
 */
sealed interface DebugMenuModal {
    /**
     * No debug selection sheet is open.
     *
     * @author Dmitriy Moroz
     */
    data object None : DebugMenuModal

    /**
     * Scheduler selection initialized from the persisted ONNX preference.
     *
     * @param scheduler Scheduler highlighted when the sheet opens.
     *
     * @author Dmitriy Moroz
     */
    data class LDScheduler(val scheduler: SchedulersToken) : DebugMenuModal
}
