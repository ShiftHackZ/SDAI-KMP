package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Synchronous platform bridge; the observer admits calls only during explicit capture.
 *
 * @author Dmitriy Moroz
 */
internal class IosUiDiagnosticTrace : UiDiagnosticTrace {
    /**
     * Begins a fixed-label section without reading application values.
     *
     * @param label Observer-generated correlation below the platform size limit.
     * @author Dmitriy Moroz
     */
    override fun beginSection(label: String) = Unit

    /**
     * Balances a section on its original thread, independent of the current capture switch.
     *
     * @author Dmitriy Moroz
     */
    override fun endSection() = Unit
}
