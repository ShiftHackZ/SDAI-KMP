package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Platform lock for short bounded diagnostic memory operations.
 *
 * @author Dmitriy Moroz
 */
internal actual class UiDiagnosticsLock actual constructor() {
    private val monitor = Any()

    /**
     * Serializes short memory-only access.
     *
     * @param block Bounded operation that must not perform IO or suspend.
     * @author Dmitriy Moroz
     */
    actual fun <T> withLock(block: () -> T): T = synchronized(monitor, block)
}
