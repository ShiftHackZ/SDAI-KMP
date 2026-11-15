package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * One bounded event without state, intent values or exception details.
 *
 * @param elapsedMillis Start for execution spans, observation time otherwise, relative to capture/reset.
 * @param owner Non-sensitive process-local screen-instance counter.
 * @param screen Fixed code-owned screen identifier.
 * @param type Recorded signal, distinct from CPU/frame-time measurements.
 * @param revision Observed StateFlow revision for this screen instance.
 * @param subtree Fixed aggregate subtree, never an item identity.
 * @param operation Synchronous work type; null for count-only observations.
 * @param durationNanos Synchronous elapsed time, excluding suspension; not a frame deadline.
 * @author Dmitriy Moroz
 */
data class UiDiagnosticEvent(
    val elapsedMillis: Long,
    val owner: Long,
    val screen: UiDiagnosticScreen,
    val type: UiDiagnosticEventType,
    val revision: Long,
    val subtree: UiDiagnosticSubtree = UiDiagnosticSubtree.SCREEN,
    val operation: UiDiagnosticOperation? = null,
    val durationNanos: Long? = null,
)
