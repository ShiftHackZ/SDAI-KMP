package com.shifthackz.aisdv1.core.mvi.diagnostics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility

/**
 * Traces this instrumented wrapper's executions and records its applied SideEffects.
 * Descendant restart scopes can recompose independently and are not counted by this wrapper.
 * Counts do not measure skips or frame timing. Inactive normal builds invoke content directly.
 *
 * @param subtree Fixed aggregate label, shared by all cells of the same subtree type.
 * @param content Actual composable subtree; the synchronous span always closes on failure.
 * @author Dmitriy Moroz
 */
@Composable
fun UiDiagnosticSection(subtree: UiDiagnosticSubtree, content: @Composable () -> Unit) {
    val parent = LocalUiDiagnosticContext.current
    if (parent == null) {
        content()
        return
    }
    val context = remember(parent.diagnostics, parent.owner, subtree) {
        UiDiagnosticContext(parent.diagnostics, parent.owner, subtree)
    }
    val isVisible = LocalMviVisibility.current
    withUiDiagnosticTrace(context.takeIf { isVisible }, UiDiagnosticOperation.COMPOSE_EXECUTION) {
        CompositionLocalProvider(LocalUiDiagnosticContext provides context) { content() }
    }
    SideEffect {
        if (isVisible) parent.diagnostics.record(
            parent.owner,
            UiDiagnosticEventType.COMPOSITION_COMMIT,
            subtree
        )
    }
}
