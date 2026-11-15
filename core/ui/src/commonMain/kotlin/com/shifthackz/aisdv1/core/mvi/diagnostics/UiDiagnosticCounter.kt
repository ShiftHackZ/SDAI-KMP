package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Aggregated observations for an instrumented screen or subtree wrapper.
 * Commits include its initial composition; independent descendant recompositions are not counted.
 * Emissions exclude the collector's initial state.
 *
 * @param owner Process-local screen instance.
 * @param screen Fixed screen label.
 * @param mutationRequests Calls requesting state mutation, regardless of equality suppression.
 * @param observedEmissions Distinct values delivered to this diagnostic StateFlow collector.
 * @param compositionCommits Applied SideEffects of the instrumented wrapper.
 * @param subtree Fixed aggregate subtree shared by matching cells.
 * @param executions Completed synchronous spans, not rendered frames or runtime skips.
 * @author Dmitriy Moroz
 */
data class UiDiagnosticCounter(
    val owner: Long,
    val screen: UiDiagnosticScreen,
    val mutationRequests: Long = 0,
    val observedEmissions: Long = 0,
    val compositionCommits: Long = 0,
    val subtree: UiDiagnosticSubtree = UiDiagnosticSubtree.SCREEN,
    val executions: Long = 0,
)
