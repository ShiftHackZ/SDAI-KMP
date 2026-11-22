package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.PerformanceReading

/**
 * Reads platform app resource counters without retaining a sampler or starting background work.
 *
 * @author Dmitriy Moroz
 */
interface PerformanceGateway {
    /**
     * Returns current counters for the host and its inference processes. Platform read failures
     * propagate to the observing use case; callers must invoke this operation off the UI thread.
     *
     * @author Dmitriy Moroz
     */
    suspend fun read(): PerformanceReading
}
