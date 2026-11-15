package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Platform trace bridge used only during explicitly enabled local capture.
 * Sections must begin/end on the same thread without suspension; labels contain fixed enums/numbers.
 *
 * @author Dmitriy Moroz
 */
interface UiDiagnosticTrace {
    /**
     * Begins one synchronous platform section.
     *
     * @param label Sanitized fixed-label correlation, bounded below the platform length limit.
     * @author Dmitriy Moroz
     */
    fun beginSection(label: String)

    /**
     * Ends the most recent section on this thread, even after capture stops or work throws.
     *
     * @author Dmitriy Moroz
     */
    fun endSection()
}
