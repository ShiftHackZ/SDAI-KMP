package com.shifthackz.aisdv1.presentation.debug.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.presentation.debug.DebugMenuState

/**
 * Supplies platform capability combinations, preference states and each saved scheduler choice.
 *
 * @author Dmitriy Moroz
 */
internal class DebugMenuPreviewParameterProvider : PreviewParameterProvider<DebugMenuPreviewCase> {
    private val android = DebugMenuState(
        isUiDiagnosticsSupported = true,
        showWorkManagerSection = true,
        showLocalDiffusionSection = true,
    )
    private val logsOnly = DebugMenuState(showQualityAssuranceSection = false)
    private val cases = listOf(
        DebugMenuPreviewCase("Android · Capture off", android),
        DebugMenuPreviewCase("Android · Capturing", android.copy(isUiDiagnosticsCapturing = true)),
        DebugMenuPreviewCase(
            "iOS · Supported controls",
            DebugMenuState(isUiDiagnosticsSupported = true)
        ),
        DebugMenuPreviewCase("Logs only", logsOnly),
        DebugMenuPreviewCase("Diagnostics only", logsOnly.copy(isUiDiagnosticsSupported = true)),
        DebugMenuPreviewCase("Background work", logsOnly.copy(showWorkManagerSection = true)),
        DebugMenuPreviewCase(
            "Local · Cancellation off",
            logsOnly.copy(showLocalDiffusionSection = true)
        ),
        DebugMenuPreviewCase(
            "Local · Cancellation on", logsOnly.copy(
                showLocalDiffusionSection = true, localDiffusionAllowCancel = true,
            )
        ),
        DebugMenuPreviewCase(
            "Diagnostics unavailable",
            android.copy(isUiDiagnosticsSupported = false)
        ),
        DebugMenuPreviewCase("Lower sections", android, scrollOffset = 650),
    ) + SchedulersToken.entries.map { scheduler ->
        DebugMenuPreviewCase(
            "Saved scheduler · ${scheduler.type}",
            logsOnly.copy(
                showLocalDiffusionSection = true,
                localDiffusionSchedulerThread = scheduler
            ),
        )
    }

    /**
     * Fixed snapshots composed with inert actions in both palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<DebugMenuPreviewCase> = cases.asSequence()

    /**
     * Identifies the visible controls, preference or scroll position.
     *
     * @param index Position in the ordered menu-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
