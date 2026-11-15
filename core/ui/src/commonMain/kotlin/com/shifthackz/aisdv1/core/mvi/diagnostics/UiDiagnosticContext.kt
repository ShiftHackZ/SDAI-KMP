package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Data-only correlation passed from a composed screen to bounded synchronous decode work.
 *
 * @param diagnostics Injected process observer; never obtained through a service locator.
 * @param owner Process-local fixed-label screen identity.
 * @param subtree Aggregate subtree; never derived from image IDs or user data.
 * @author Dmitriy Moroz
 */
class UiDiagnosticContext(
    val diagnostics: UiDiagnostics,
    val owner: Long,
    val subtree: UiDiagnosticSubtree
)
