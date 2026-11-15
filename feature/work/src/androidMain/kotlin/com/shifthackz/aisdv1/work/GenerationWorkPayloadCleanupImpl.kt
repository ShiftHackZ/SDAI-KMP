package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.work.di.WorkManagerProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Performs persisted-state cleanup off main; an unavailable state conservatively retains retry data.
 *
 * @param workManager Persisted work state.
 * @param payloads Immutable request files.
 * @author Dmitriy Moroz
 */
internal class GenerationWorkPayloadCleanupImpl(
    private val workManager: WorkManagerProvider,
    private val payloads: GenerationWorkPayloadStore,
) : GenerationWorkPayloadCleanup {
    /**
     * Deletes terminal work snapshots off main in a noncancellable cleanup section.
     * Stopped work retains its snapshot unless persisted WorkManager state confirms it has finished.
     *
     * @param id Immutable work identity.
     * @param isStopped Whether this invocation was stopped before returning a terminal result.
     * @author Dmitriy Moroz
     */
    override suspend fun complete(id: UUID, isStopped: Boolean) =
        withContext(NonCancellable + Dispatchers.IO) {
            runCatching {
                val canDelete =
                    !isStopped || workManager().getWorkInfoById(id).get()?.state?.isFinished == true
                if (canDelete) payloads.delete(id.toString())
            }
            Unit
        }
}
