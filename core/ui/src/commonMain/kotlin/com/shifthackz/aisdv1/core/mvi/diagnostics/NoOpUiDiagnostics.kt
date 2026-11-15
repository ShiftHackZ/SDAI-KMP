package com.shifthackz.aisdv1.core.mvi.diagnostics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unsupported-platform/release observer; records, exports and screen registration remain inactive.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpUiDiagnostics : UiDiagnostics {
    override val isSupported = false
    override val captureState = MutableStateFlow(false).asStateFlow()

    /**
     * Returns an inactive owner without allocating diagnostic state.
     *
     * @param screen Fixed application-owned label.
     * @author Dmitriy Moroz
     */
    override fun register(screen: UiDiagnosticScreen): Long = 0L

    /**
     * Ignores release because recording is disabled.
     *
     * @param owner Process-local identity returned by register.
     * @author Dmitriy Moroz
     */
    override fun unregister(owner: Long) = Unit

    /**
     * Discards the signal without allocating or reading application state.
     *
     * @param owner Registered screen instance.
     * @param type Fixed signal category.
     * @param subtree Fixed aggregate label; unused in normal builds.
     * @author Dmitriy Moroz
     */
    override fun record(owner: Long, type: UiDiagnosticEventType, subtree: UiDiagnosticSubtree) =
        Unit

    /**
     * Returns no span and performs no allocation or platform trace work.
     *
     * @param owner Ignored screen identity.
     * @param subtree Ignored fixed subtree.
     * @param operation Ignored fixed operation.
     * @author Dmitriy Moroz
     */
    override fun beginTrace(
        owner: Long,
        subtree: UiDiagnosticSubtree,
        operation: UiDiagnosticOperation
    ): UiDiagnosticSpan? = null

    /**
     * Performs no trace or export work.
     *
     * @param span Inactive null token.
     * @author Dmitriy Moroz
     */
    override fun finishTrace(span: UiDiagnosticSpan?) = Unit

    /**
     * Keeps recording disabled in normal builds.
     *
     * @author Dmitriy Moroz
     */
    override fun startCapture() = Unit

    /**
     * Keeps recording disabled without retaining a capture.
     *
     * @author Dmitriy Moroz
     */
    override fun stopCapture() = Unit

    /**
     * Does nothing because normal builds retain no capture.
     *
     * @author Dmitriy Moroz
     */
    override fun reset() = Unit

    /**
     * Returns an empty snapshot without accessing application state.
     *
     * @author Dmitriy Moroz
     */
    override fun snapshot() = UiDiagnosticsSnapshot(0, emptyList(), emptyList(), 0)
}
