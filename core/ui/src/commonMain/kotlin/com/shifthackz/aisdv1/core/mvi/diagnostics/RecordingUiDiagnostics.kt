package com.shifthackz.aisdv1.core.mvi.diagnostics

import kotlin.time.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fixed owner-by-subtree counters and an overwrite ring, containing no application object references.
 * Spans correlate synchronous work with observed state revisions, not runtime skips or frame outcomes.
 *
 * @param eventCapacity Maximum retained observations/completions.
 * @param ownerCapacity Maximum screen instances; subtree dimensions are fixed by the enum.
 * @param trace Injected synchronous platform bridge, called outside the memory lock.
 * @author Dmitriy Moroz
 */
internal class RecordingUiDiagnostics(
    eventCapacity: Int,
    ownerCapacity: Int,
    private val trace: UiDiagnosticTrace
) : UiDiagnostics {
    private val lock = UiDiagnosticsLock()
    private val events = arrayOfNulls<UiDiagnosticEvent>(eventCapacity.coerceAtLeast(1))
    private val owners = arrayOfNulls<Array<UiDiagnosticCounter>>(ownerCapacity.coerceAtLeast(1))
    private val mutableCaptureState = MutableStateFlow(false)
    override val captureState = mutableCaptureState.asStateFlow()
    override val isSupported = true
    private var origin = TimeSource.Monotonic.markNow()
    private var captureEpoch = 0L
    private var nextOwner = 0L
    private var eventIndex = 0
    private var eventCount = 0
    private var discardedOwners = 0L
    private var droppedEvents = 0L

    /**
     * Reserves fixed subtree slots for a screen, never one owner per gallery cell.
     *
     * @param screen Code-owned screen identity.
     * @author Dmitriy Moroz
     */
    override fun register(screen: UiDiagnosticScreen): Long = lock.withLock {
        val owner = ++nextOwner
        val slot = (owner % owners.size).toInt()
        if (owners[slot] != null) discardedOwners++
        owners[slot] = Array(UiDiagnosticSubtree.entries.size) { index ->
            UiDiagnosticCounter(owner, screen, subtree = UiDiagnosticSubtree.entries[index])
        }
        owner
    }

    /**
     * Releases only the matching live screen, without retaining its ViewModel or image content.
     *
     * @param owner Registered screen identity.
     * @author Dmitriy Moroz
     */
    override fun unregister(owner: Long) = lock.withLock {
        val slot = (owner % owners.size).toInt()
        if (owners[slot]?.first()?.owner == owner) owners[slot] = null
    }

    /**
     * Records a count observation only during capture; all cells share their fixed subtree row.
     *
     * @param owner Registered screen identity.
     * @param type Observation category; execution completions use finishTrace.
     * @param subtree Code-owned aggregate label.
     * @author Dmitriy Moroz
     */
    override fun record(owner: Long, type: UiDiagnosticEventType, subtree: UiDiagnosticSubtree) {
        if (!mutableCaptureState.value) return
        lock.withLock {
            if (!mutableCaptureState.value) return@withLock
            val rows = rows(owner) ?: run { droppedEvents++; return@withLock }
            val previous = rows[subtree.ordinal]
            rows[subtree.ordinal] = when (type) {
                UiDiagnosticEventType.MUTATION_REQUEST -> previous.copy(mutationRequests = previous.mutationRequests + 1)
                UiDiagnosticEventType.OBSERVED_EMISSION -> previous.copy(observedEmissions = previous.observedEmissions + 1)
                UiDiagnosticEventType.COMPOSITION_COMMIT -> previous.copy(compositionCommits = previous.compositionCommits + 1)
                UiDiagnosticEventType.INITIAL_STATE, UiDiagnosticEventType.EXECUTION -> previous
            }
            append(
                UiDiagnosticEvent(
                    origin.elapsedNow().inWholeMilliseconds, owner, previous.screen, type,
                    rows[UiDiagnosticSubtree.SCREEN.ordinal].observedEmissions, subtree
                )
            )
        }
    }

    /**
     * Captures only numeric/fixed-label correlation, then starts a same-thread platform section.
     *
     * @param owner Registered screen identity.
     * @param subtree Fixed aggregate subtree label.
     * @param operation Synchronous operation that cannot suspend.
     * @return Span that must be finished in a finally block, or null on the inactive path.
     * @author Dmitriy Moroz
     */
    override fun beginTrace(
        owner: Long,
        subtree: UiDiagnosticSubtree,
        operation: UiDiagnosticOperation
    ): UiDiagnosticSpan? {
        if (!mutableCaptureState.value) return null
        val span = lock.withLock {
            if (!mutableCaptureState.value) return@withLock null
            val rows = rows(owner) ?: run { droppedEvents++; return@withLock null }
            val screen = rows[UiDiagnosticSubtree.SCREEN.ordinal]
            UiDiagnosticSpan(
                owner, screen.screen, subtree, operation, screen.observedEmissions, captureEpoch,
                origin.elapsedNow().inWholeMilliseconds, TimeSource.Monotonic.markNow()
            )
        } ?: return null
        // Enum names and two Long values keep this correlation label below Android's 127-character limit.
        trace.beginSection("SDAI/${span.screen.name}/${span.subtree.name}/${span.operation.name}/o${span.owner}/r${span.revision}")
        return span
    }

    /**
     * Always closes the platform span, recording only completions belonging to the current capture.
     *
     * @param span Same-thread token from beginTrace, or null for inactive work.
     * @author Dmitriy Moroz
     */
    override fun finishTrace(span: UiDiagnosticSpan?) {
        if (span == null) return
        val duration = span.mark.elapsedNow().inWholeNanoseconds
        try {
            trace.endSection()
        } finally {
            lock.withLock {
                if (!mutableCaptureState.value || span.epoch != captureEpoch) return@withLock
                val rows = rows(span.owner) ?: run { droppedEvents++; return@withLock }
                val previous = rows[span.subtree.ordinal]
                rows[span.subtree.ordinal] = previous.copy(executions = previous.executions + 1)
                append(
                    UiDiagnosticEvent(
                        span.elapsedMillis,
                        span.owner,
                        span.screen,
                        UiDiagnosticEventType.EXECUTION,
                        span.revision,
                        span.subtree,
                        span.operation,
                        duration
                    )
                )
            }
        }
    }

    /**
     * Starts a new empty capture without emitting observable per-event state.
     *
     * @author Dmitriy Moroz
     */
    override fun startCapture() {
        reset(); mutableCaptureState.value = true
    }

    /**
     * Stops new recording; already-started synchronous platform spans still close correctly.
     *
     * @author Dmitriy Moroz
     */
    override fun stopCapture() {
        mutableCaptureState.value = false
    }

    /**
     * Clears counts/events and advances the epoch so older span completions cannot enter a new capture.
     *
     * @author Dmitriy Moroz
     */
    override fun reset() = lock.withLock {
        origin = TimeSource.Monotonic.markNow()
        captureEpoch++
        events.fill(null)
        eventIndex = 0
        eventCount = 0
        discardedOwners = 0
        droppedEvents = 0
        for (slot in owners.indices) owners[slot] = owners[slot]?.let { previous ->
            Array(UiDiagnosticSubtree.entries.size) { index ->
                UiDiagnosticCounter(
                    previous[0].owner,
                    previous[0].screen,
                    subtree = UiDiagnosticSubtree.entries[index]
                )
            }
        }
    }

    /**
     * Copies at most ownerCapacity times subtree-count rows plus the fixed-capacity event ring.
     *
     * @author Dmitriy Moroz
     */
    override fun snapshot(): UiDiagnosticsSnapshot = lock.withLock {
        UiDiagnosticsSnapshot(
            origin.elapsedNow().inWholeMilliseconds, owners.filterNotNull().flatMap { it.toList() },
            List(eventCount) { index -> events[(eventIndex - eventCount + index + events.size) % events.size]!! },
            discardedOwners, droppedEvents
        )
    }

    /**
     * Finds bounded code-owned rows; the caller owns the memory lock.
     *
     * @param owner Process-local identity to validate against slot reuse.
     * @author Dmitriy Moroz
     */
    private fun rows(owner: Long): Array<UiDiagnosticCounter>? =
        owners[(owner % owners.size).toInt()]?.takeIf { it.first().owner == owner }

    /**
     * Appends one sanitized observation, accounting for every overwritten sample.
     *
     * @param event Detached fixed-label observation; never contains application data.
     * @author Dmitriy Moroz
     */
    private fun append(event: UiDiagnosticEvent) {
        if (eventCount == events.size) droppedEvents++
        events[eventIndex] = event
        eventIndex = (eventIndex + 1) % events.size
        eventCount = (eventCount + 1).coerceAtMost(events.size)
    }
}
