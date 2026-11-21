package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.mappers.mapToBasicHttpAuthorization
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.repository.Automatic1111ProgressRepository
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111GenerationApi

/**
 * Fetches strict progress responses using the same authenticated connection provider as generation.
 *
 * @param api AUTOMATIC1111 transport with preview images disabled.
 * @param connections Captures the selected server and credentials.
 * @author Dmitriy Moroz
 */
internal class Automatic1111ProgressRepositoryImpl(
    private val api: Automatic1111GenerationApi,
    private val connections: AuthenticatedConnectionProvider,
) : Automatic1111ProgressRepository {
    /**
     * Returns only a finite measurement correlated with this submission.
     *
     * @param taskId Identity sent with the generation request.
     * @author Dmitriy Moroz
     */
    override suspend fun getProgress(taskId: String): Float? {
        val connection = connections.snapshot()
        val response = api.progress(
            connection.automatic1111Url,
            connection.credentials.mapToBasicHttpAuthorization()
        )
        return response.progress?.takeIf { response.currentTask == taskId && it.isFinite() }
            ?.coerceIn(0f, 1f)
    }
}
