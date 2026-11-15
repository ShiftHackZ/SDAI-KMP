package com.shifthackz.aisdv1.core.mvi.diagnostics

import platform.Foundation.NSLock

/**
 * Platform lock for short bounded diagnostic memory operations.
 *
 * @author Dmitriy Moroz
 */
internal actual class UiDiagnosticsLock actual constructor() {
    private val lock = NSLock()

    /**
     * Serializes short memory-only access.
     *
     * @param block Bounded operation that must not perform IO or suspend.
     * @author Dmitriy Moroz
     */
    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        return try {
            block()
        } finally {
            lock.unlock()
        }
    }
}
