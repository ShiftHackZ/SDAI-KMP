package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Allows existing concurrent generation while admitting restore only when every generation owner is idle.
 * New generation waits cancellably while restore owns the exclusive transition.
 *
 * @author Dmitriy Moroz
 */
internal class DefaultGenerationRestoreGate : GenerationRestoreGate {
    private val state = Mutex()
    private var activeGenerations = 0
    private var restoration: CompletableDeferred<Unit>? = null

    override suspend fun <T> generating(block: suspend () -> T): T {
        while (true) {
            val pendingRestore = state.withLock {
                restoration.also { if (it == null) activeGenerations++ }
            }
            if (pendingRestore == null) break
            pendingRestore.await()
        }
        return try {
            block()
        } finally {
            withContext(NonCancellable) { state.withLock { activeGenerations-- } }
        }
    }

    override suspend fun <T> restoring(block: suspend () -> T): T {
        state.withLock {
            check(activeGenerations == 0 && restoration == null) { "Wait for generation to finish before restoring" }
            restoration = CompletableDeferred()
        }
        return try {
            block()
        } finally {
            withContext(NonCancellable) {
                state.withLock { restoration?.complete(Unit); restoration = null }
            }
        }
    }
}
