package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgress
import kotlinx.coroutines.flow.Flow

/**
 * Shares active remote progress between generation callers and their UI observers.
 * @author Dmitriy Moroz
 */
interface RemoteGenerationProgressGateway {
    /** Observes all active submissions; completed owners are removed. @author Dmitriy Moroz */
    fun observe(): Flow<List<RemoteGenerationProgress>>

    /**
     * Replaces one submission's measurement, or removes it after completion.
     * @param taskId Unique submission identity.
     * @param progress Current measurement, or null to remove this submission.
     * @author Dmitriy Moroz
     */
    fun update(taskId: String, progress: RemoteGenerationProgress?)
}
