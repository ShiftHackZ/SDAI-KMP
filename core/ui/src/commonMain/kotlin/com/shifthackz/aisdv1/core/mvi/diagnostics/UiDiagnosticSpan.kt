package com.shifthackz.aisdv1.core.mvi.diagnostics

import kotlin.time.TimeMark

/**
 * Opaque synchronous span containing only code-owned metadata and monotonic timing.
 * Never survives a suspension or retains a ViewModel, state, image, model path or user label.
 *
 * @param owner Process-local screen identity.
 * @param screen Fixed screen label.
 * @param subtree Fixed aggregate subtree.
 * @param operation Fixed synchronous operation.
 * @param revision Last observed emission revision when the operation began, not an application version.
 * @param epoch Capture/reset generation used to reject obsolete completions.
 * @param elapsedMillis Start relative to capture/reset.
 * @param mark Monotonic operation start.
 * @author Dmitriy Moroz
 */
class UiDiagnosticSpan internal constructor(
    internal val owner: Long,
    internal val screen: UiDiagnosticScreen,
    internal val subtree: UiDiagnosticSubtree,
    internal val operation: UiDiagnosticOperation,
    internal val revision: Long,
    internal val epoch: Long,
    internal val elapsedMillis: Long,
    internal val mark: TimeMark,
)
