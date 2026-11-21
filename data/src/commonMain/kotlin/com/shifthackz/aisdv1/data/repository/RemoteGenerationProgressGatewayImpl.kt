package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgress
import com.shifthackz.aisdv1.domain.gateway.RemoteGenerationProgressGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Retains independent measurements so overlapping workers cannot overwrite each other. @author Dmitriy Moroz */
internal class RemoteGenerationProgressGatewayImpl : RemoteGenerationProgressGateway {
    private val progress = MutableStateFlow<Map<String, RemoteGenerationProgress>>(emptyMap())

    /** Observes only currently owned submissions. @author Dmitriy Moroz */
    override fun observe() = progress.map { it.values.toList() }

    /**
     * Atomically replaces or removes a single owner without disturbing other workers.
     * @param taskId Unique submission identity.
     * @param progress Current measurement, or null after termination.
     * @author Dmitriy Moroz
     */
    override fun update(taskId: String, progress: RemoteGenerationProgress?) {
        this.progress.update { if (progress == null) it - taskId else it + (taskId to progress) }
    }
}
