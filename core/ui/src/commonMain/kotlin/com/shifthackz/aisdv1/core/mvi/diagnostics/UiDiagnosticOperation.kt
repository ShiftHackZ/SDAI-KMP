package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Synchronous operations measured independently from emitted values and completed commits.
 *
 * @author Dmitriy Moroz
 */
enum class UiDiagnosticOperation {
    STATE_UPDATE,
    STATE_REPLACEMENT,
    COMPOSE_EXECUTION,
    IMAGE_DECODE,
}
