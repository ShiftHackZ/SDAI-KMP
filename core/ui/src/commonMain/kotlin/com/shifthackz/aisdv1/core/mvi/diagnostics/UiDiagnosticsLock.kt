package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Small in-memory critical section; no IO or application callbacks execute while held.
 *
 * @author Dmitriy Moroz
 */
internal expect class UiDiagnosticsLock() {
    /**
     * Protects bounded ring/counter access across UI and background threads.
     *
     * @param block Short non-suspending in-memory operation.
     * @author Dmitriy Moroz
     */
    fun <T> withLock(block: () -> T): T
}
