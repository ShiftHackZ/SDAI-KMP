package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Default for platforms without Android inference IPC; platform modules replace this binding.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpNativeGenerationGateway : NativeGenerationGateway {
    /**
     * No inference child exists on this platform.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun readProcessPerformance(): ProcessPerformanceReading? = null

    /**
     * Fails explicitly because this platform has no Android inference process.
     *
     * @param request Unsupported Android inference request.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(request: NativeGenerationRequest): String =
        error("Android native inference is unavailable on this platform.")

    /**
     * No process is owned on this platform.
     *
     * @param ownerId Ignored worker ownership filter.
     * @author Dmitriy Moroz
     */
    override suspend fun cancelActive(ownerId: String?): Boolean = false

    /**
     * Returns an empty flow because this platform owns no Android native progress source.
     *
     * @param engine Unsupported Android runtime.
     * @author Dmitriy Moroz
     */
    override fun observeStatus(engine: NativeGenerationEngine): Flow<LocalDiffusionStatus> =
        emptyFlow()

    /**
     * Returns an empty flow because this platform owns no Android native lifecycle source.
     *
     * @param engine Unsupported Android runtime.
     * @author Dmitriy Moroz
     */
    override fun observeStage(engine: NativeGenerationEngine): Flow<NativeGenerationStage> =
        emptyFlow()
}
