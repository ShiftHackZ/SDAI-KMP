package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.CaptureEasyDiffusionConnectionUseCase

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GenerateEasyDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.toEasyDiffusionRequest

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ArliAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository

/**
 * Routes captured image requests to supported providers, delegating Easy Diffusion batches to one operation with a shared deadline.
 *
 * @param stableDiffusionGenerationRepository Generation and result persistence for AUTOMATIC1111.
 * @param swarmUiGenerationRepository Generation and result persistence for SwarmUI.
 * @param hordeGenerationRepository Generation and result persistence for AI Horde.
 * @param huggingFaceGenerationRepository Generation and result persistence for Hugging Face.
 * @param stabilityAiGenerationRepository Generation and result persistence for Stability AI.
 * @param coreMlGenerationRepository Generation and result persistence for Core ML.
 * @param falAiGenerationRepository Generation and result persistence for Fal.ai.
 * @param arliAiGenerationRepository Generation and result persistence for ArliAI.
 * @param preferenceManager Current provider selection and independent generation preferences.
 * @param generationRestoreGate Serializes generation against portable backup restoration.
 * @param captureEasyDiffusionConnectionUseCase Captures endpoint/checkpoint under the provider-selection lock, retaining the submitted revision.
 * @param generateEasyDiffusionUseCase Owns captured-task batching, stream recovery, cancellation and deadlines.
 * @author Dmitriy Moroz
 */
internal class ImageToImageUseCaseImpl(

    private val stableDiffusionGenerationRepository: StableDiffusionGenerationRepository,

    private val swarmUiGenerationRepository: SwarmUiGenerationRepository,

    private val hordeGenerationRepository: HordeGenerationRepository,

    private val huggingFaceGenerationRepository: HuggingFaceGenerationRepository,

    private val stabilityAiGenerationRepository: StabilityAiGenerationRepository,

    private val coreMlGenerationRepository: CoreMlGenerationRepository,

    private val falAiGenerationRepository: FalAiGenerationRepository,

    private val arliAiGenerationRepository: ArliAiGenerationRepository,

    private val preferenceManager: PreferenceManager,
    private val generationRestoreGate: GenerationRestoreGate,
    private val generateEasyDiffusionUseCase: GenerateEasyDiffusionUseCase,
    private val captureEasyDiffusionConnectionUseCase: CaptureEasyDiffusionConnectionUseCase,
) : ImageToImageUseCase {

    /**
     * Routes the captured image request and executes a provider batch or sequential single-image calls without changing unrelated settings.
     *
     * @param payload Immutable supported generation inputs and captured provider identity.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(payload: ImageToImagePayload): List<AiGenerationResult> =
        generationRestoreGate.generating { generate(payload) }

    /**
     * Executes a captured image request while this operation owns generation admission.
     * @param payload Immutable provider generation inputs and source image.
     * @author Dmitriy Moroz
     */
    private suspend fun generate(payload: ImageToImagePayload): List<AiGenerationResult> {
        coroutineContext.ensureActive()
        val source = payload.sourceSnapshot ?: preferenceManager.source
        check(source == preferenceManager.source) { "Generation source changed" }
        return when (source) {
            ServerSource.EASY_DIFFUSION -> generateEasyDiffusionUseCase(
                payload.toEasyDiffusionRequest(captureEasyDiffusionConnectionUseCase(payload.providerSelectionRevision))
            )

            ServerSource.AUTOMATIC1111 -> stableDiffusionGenerationRepository.generateFromImage(
                payload
            )

            ServerSource.FAL_AI -> falAiGenerationRepository.generateFromImage(payload)
            ServerSource.ARLI_AI -> arliAiGenerationRepository.generateFromImage(payload)
            else -> List(payload.batchCount.coerceAtLeast(1)) {
                coroutineContext.ensureActive()
                check(source == preferenceManager.source) { "Generation source changed" }
                generateSingle(payload)
            }
        }

    }

    /**
     * Dispatches one image to the captured provider; providers with dedicated batch paths are rejected here.
     *
     * @param payload Immutable supported generation inputs and captured provider identity.
     * @author Dmitriy Moroz
     */
    private suspend fun generateSingle(payload: ImageToImagePayload) =
        when (payload.sourceSnapshot ?: preferenceManager.source) {
            ServerSource.OPEN_AI_COMPATIBLE -> error("This image server supports text-to-image only")
            ServerSource.OPEN_AI_OAUTH -> error("ChatGPT supports text-to-image only")
            ServerSource.SWARM_UI -> swarmUiGenerationRepository.generateFromImage(payload)
            ServerSource.HORDE -> hordeGenerationRepository.generateFromImage(payload)
            ServerSource.HUGGING_FACE -> huggingFaceGenerationRepository.generateFromImage(payload)
            ServerSource.STABILITY_AI -> stabilityAiGenerationRepository.generateFromImage(payload)
            ServerSource.LOCAL_APPLE_CORE_ML -> coreMlGenerationRepository.generateFromImage(payload)
            ServerSource.AUTOMATIC1111 -> error("Automatic1111 batch must be generated through generateFromImage(payload).")
            ServerSource.FAL_AI -> error("Fal.ai batch must be generated through generateFromImage(payload).")
            ServerSource.ARLI_AI -> error("ArliAI batch must be generated through generateFromImage(payload).")
            else -> throw IllegalStateException("Img2Img not yet supported on ${preferenceManager.source}!")
        }
}
