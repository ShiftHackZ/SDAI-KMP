package com.shifthackz.aisdv1.work.fixture

import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow

/**
 * Separates each request's transfer events so a cancelled stream cannot stop a neighboring model.
 *
 * @author Dmitriy Moroz
 */
internal class ControlledModelDownloads : DownloadModelsUseCase {
    val started = mutableListOf<ModelDownloadRequest>()
    val streams = mutableMapOf<ModelDownloadRequest, Channel<ModelDownloadBatch>>()

    /**
     * Records actual collection and forwards only this request's controlled transfer events.
     *
     * @param request Captured provider/model selection used as the stream identity.
     * @author Dmitriy Moroz
     */
    override fun invoke(request: ModelDownloadRequest) = flow {
        started += request
        val stream = streams.getOrPut(request) { Channel(Channel.UNLIMITED) }
        for (snapshot in stream) emit(snapshot)
    }
}
