package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Allows independent installs concurrently while a replacement waits for the same model's cleanup.
 * @param downloadableModelRepository Streams archives and activates complete owned payloads.
 * @author Dmitriy Moroz
 */
internal class DownloadModelUseCaseImpl(
    private val downloadableModelRepository: DownloadableModelRepository,
) : DownloadModelUseCase {
    private val registryMutex = Mutex()
    private val transferMutexes = mutableMapOf<String, Mutex>()

    /**
     * Holds exclusive ownership of this model until activation or cancellation cleanup finishes.
     * @param id Catalog identity naming the app-owned destination.
     * @param url Selected catalog mirror for the archive.
     * @author Dmitriy Moroz
     */
    override fun invoke(id: String, url: String) = flow {
        val transferMutex = registryMutex.withLock { transferMutexes.getOrPut(id) { Mutex() } }
        transferMutex.withLock { emitAll(downloadableModelRepository.download(id, url)) }
    }
}
