package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway

/**
 * Preserves worker ownership when interrupting an engine or remote source.
 *
 * @param nativeGenerationGateway Cancels only the exact native worker owner.
 * @param interruptSourceGenerationUseCase Routes the immutable remote source to its repository.
 * @author Dmitriy Moroz
 */
internal class InterruptWorkerGenerationUseCaseImpl(
    private val nativeGenerationGateway: NativeGenerationGateway,
    private val interruptSourceGenerationUseCase: InterruptSourceGenerationUseCase,
) : InterruptWorkerGenerationUseCase {
    /**
     * Requests interruption without reading the selected provider preference.
     *
     * @param source Source captured by the work request.
     * @param ownerId Exact native owner associated with the worker.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(source: ServerSource, ownerId: String) {
        when (source) {
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> nativeGenerationGateway.cancelActive(ownerId)

            else -> interruptSourceGenerationUseCase(source)
        }
    }
}
