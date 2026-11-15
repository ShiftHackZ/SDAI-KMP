package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Balances a synchronous span without introducing a lambda allocation at the call site.
 * The inline boundary also allows composable content under the pinned Compose compiler.
 *
 * @param context Fixed-label correlation, null for inactive/hidden work.
 * @param operation Code-owned synchronous operation category.
 * @param block Actual non-suspending work, including the compiler-generated composition call.
 * @return Work result; failures propagate after closing the platform span.
 * @author Dmitriy Moroz
 */
internal inline fun <T> withUiDiagnosticTrace(
    context: UiDiagnosticContext?,
    operation: UiDiagnosticOperation,
    block: () -> T,
): T {
    val span = context?.let { it.diagnostics.beginTrace(it.owner, it.subtree, operation) }
    try {
        return block()
    } finally {
        context?.diagnostics?.finishTrace(span)
    }
}
