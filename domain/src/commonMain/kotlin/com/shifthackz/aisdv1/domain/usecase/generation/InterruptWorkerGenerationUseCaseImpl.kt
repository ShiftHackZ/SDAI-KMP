package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.InterruptEasyDiffusionUseCase

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway

/**
 * Preserves worker ownership when interrupting an engine or remote source.
 *
 * @param nativeGenerationGateway Cancels only the exact native worker owner.
 * @param interruptSourceGenerationUseCase Routes the immutable remote source to its repository.
 * @param buildInfoProvider Keeps shared Bonsai cancellation on its platform-specific runtime path.
 * @param interruptEasyDiffusionUseCase Cancels the matching remote operation while preserving its captured task identity.
 *
 * @author Dmitriy Moroz
 */
internal class InterruptWorkerGenerationUseCaseImpl(
    private val nativeGenerationGateway: NativeGenerationGateway,
    private val interruptSourceGenerationUseCase: InterruptSourceGenerationUseCase,
    private val buildInfoProvider: BuildInfoProvider,
    private val interruptEasyDiffusionUseCase: InterruptEasyDiffusionUseCase,
) : InterruptWorkerGenerationUseCase {
    /**
     * Requests interruption without reading the selected provider preference.
     *
     * @param source Source captured by the work request.
     * @param ownerId Exact native owner associated with the worker.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(source: ServerSource, ownerId: String) {
        when (source) {
            ServerSource.EASY_DIFFUSION -> interruptEasyDiffusionUseCase(ownerId)
            ServerSource.LOCAL_MNN,
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> nativeGenerationGateway.cancelActive(ownerId)

            ServerSource.LOCAL_APPLE_BONSAI -> if (buildInfoProvider.platform == Platform.ANDROID) {
                nativeGenerationGateway.cancelActive(ownerId)
            } else {
                interruptSourceGenerationUseCase(source)
            }

            else -> interruptSourceGenerationUseCase(source)
        }
    }
}
