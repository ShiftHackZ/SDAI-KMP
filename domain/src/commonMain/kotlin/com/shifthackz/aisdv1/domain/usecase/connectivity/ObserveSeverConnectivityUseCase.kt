package com.shifthackz.aisdv1.domain.usecase.connectivity

import com.shifthackz.aisdv1.domain.gateway.ServerConnectivityGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observes connectivity of the configured generation server.
 *
 * @author Dmitriy Moroz
 */
fun interface ObserveSeverConnectivityUseCase {
    /**
     * Emits the current connectivity state and later distinct changes.
     *
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Flow<Boolean>
}

/**
 * Observes server connectivity while suppressing consecutive duplicate states.
 *
 * @param serverConnectivityGateway Supplies connectivity changes for the configured server.
 *
 * @author Dmitriy Moroz
 */
internal class ObserveSeverConnectivityUseCaseImpl(
    private val serverConnectivityGateway: ServerConnectivityGateway,
) : ObserveSeverConnectivityUseCase {

    /**
     * Emits the current connectivity state and subsequent distinct changes.
     *
     * @author Dmitriy Moroz
     */
    override fun invoke() = serverConnectivityGateway
        .observe()
        .distinctUntilChanged()
}
