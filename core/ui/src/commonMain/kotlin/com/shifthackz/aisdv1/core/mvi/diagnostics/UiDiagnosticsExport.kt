package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Formats a bounded detached snapshot without application values or exception messages.
 * Emissions are collector observations, commits include initial compositions; neither is frame time.
 *
 * @receiver Snapshot copied only for explicit local export.
 * @author Dmitriy Moroz
 */
fun UiDiagnosticsSnapshot.toExportText(): String = buildString {
    appendLine("SDAI UI diagnostics v2; elapsed_ms=$elapsedMillis; discarded_owners=$discardedOwners; dropped_events=$droppedEvents")
    appendLine("Counts are observations, not an ANR diagnosis or frame timing.")
    appendLine("Synchronous durations include capture overhead; nested spans must not be summed as CPU time.")
    appendLine("owner,screen,subtree,mutation_requests,observed_emissions,completed_commits,synchronous_executions")
    counters.forEach { appendLine("${it.owner},${it.screen.name},${it.subtree},${it.mutationRequests},${it.observedEmissions},${it.compositionCommits},${it.executions}") }
    appendLine("elapsed_ms,owner,screen,subtree,event,operation,observed_revision,duration_ns")
    events.forEach { appendLine("${it.elapsedMillis},${it.owner},${it.screen.name},${it.subtree},${it.type},${it.operation ?: ""},${it.revision},${it.durationNanos ?: ""}") }
}
