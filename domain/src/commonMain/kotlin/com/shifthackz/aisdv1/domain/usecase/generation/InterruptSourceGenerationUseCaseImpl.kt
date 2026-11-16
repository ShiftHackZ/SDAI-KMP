package com.shifthackz.aisdv1.domain.usecase.generation


import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.repository.BonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository

/**
 * Delegates legacy source interruption without consulting preferences; Easy Diffusion requires captured job or worker ownership.
 *
 * @param stableDiffusionGenerationRepository Owns interruption of StableDiffusion generation.
 * @param hordeGenerationRepository Owns interruption of Horde generation.
 * @param localDiffusionGenerationRepository Owns interruption of LocalDiffusion generation.
 * @param stableDiffusionCppGenerationRepository Owns interruption of StableDiffusionCpp generation.
 * @param coreMlGenerationRepository Owns interruption of CoreMl generation.
 * @param bonsaiGenerationRepository Owns interruption of Bonsai generation.
 * @author Dmitriy Moroz
 */
internal class InterruptSourceGenerationUseCaseImpl(
    private val stableDiffusionGenerationRepository: StableDiffusionGenerationRepository,
    private val hordeGenerationRepository: HordeGenerationRepository,
    private val localDiffusionGenerationRepository: LocalDiffusionGenerationRepository,
    private val stableDiffusionCppGenerationRepository: StableDiffusionCppGenerationRepository,
    private val coreMlGenerationRepository: CoreMlGenerationRepository,
    private val bonsaiGenerationRepository: BonsaiGenerationRepository,
) : InterruptSourceGenerationUseCase {
    /**
     * Requests interruption of a supported source and propagates repository failures.
     *
     * @param source Explicit source whose generation should be interrupted.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(source: ServerSource) {
        when (source) {
            ServerSource.EASY_DIFFUSION -> Unit // Captured coroutine cancellation or exact worker interruption owns this task.
            ServerSource.AUTOMATIC1111 -> stableDiffusionGenerationRepository.interruptGeneration()
            ServerSource.HORDE -> hordeGenerationRepository.interruptGeneration()
            ServerSource.LOCAL_MICROSOFT_ONNX -> localDiffusionGenerationRepository.interruptGeneration()
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> stableDiffusionCppGenerationRepository.interruptGeneration()
            ServerSource.LOCAL_APPLE_CORE_ML -> coreMlGenerationRepository.interruptGeneration()
            ServerSource.LOCAL_APPLE_BONSAI -> bonsaiGenerationRepository.interruptGeneration()
            else -> Unit
        }
    }
}
