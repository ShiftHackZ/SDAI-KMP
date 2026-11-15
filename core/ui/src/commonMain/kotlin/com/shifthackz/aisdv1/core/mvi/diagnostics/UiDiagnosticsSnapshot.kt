package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Detached bounded capture, suitable for explicit export away from the main thread.
 *
 * @param elapsedMillis Duration since capture/reset.
 * @param counters At most fixed owner capacity times the fixed subtree count.
 * @param events At most the configured fixed event capacity, in append/completion order; execution start timestamps can overlap.
 * @param discardedOwners Number of owners evicted from the fixed registry.
 * @param droppedEvents Ring entries overwritten plus observations or spans dropped because their owner is unavailable.
 * @author Dmitriy Moroz
 */
data class UiDiagnosticsSnapshot(
    val elapsedMillis: Long,
    val counters: List<UiDiagnosticCounter>,
    val events: List<UiDiagnosticEvent>,
    val discardedOwners: Long,
    val droppedEvents: Long = 0,
)
