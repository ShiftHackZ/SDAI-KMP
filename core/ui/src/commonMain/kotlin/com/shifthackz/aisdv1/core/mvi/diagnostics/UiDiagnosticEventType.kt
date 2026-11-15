package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Distinct diagnostic signals; observed emissions may be conflated by StateFlow.
 *
 * @author Dmitriy Moroz
 */
enum class UiDiagnosticEventType {
    MUTATION_REQUEST,
    INITIAL_STATE,
    OBSERVED_EMISSION,
    COMPOSITION_COMMIT,
    EXECUTION,
}
