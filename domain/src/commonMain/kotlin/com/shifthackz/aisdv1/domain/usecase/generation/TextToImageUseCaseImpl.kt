package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.CaptureEasyDiffusionConnectionUseCase

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GenerateEasyDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.toEasyDiffusionRequest

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ArliAiGenerationRepository
import kotlin.random.Random
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import com.shifthackz.aisdv1.domain.repository.BonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceGenerationRepository
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.MediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.OpenAiCompatibleGenerationRepository
import com.shifthackz.aisdv1.domain.repository.OpenAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.OpenAiOAuthGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository

/**
 * Routes captured text requests to their provider and preserves native batches or sequential single-image semantics as appropriate.
 *
 * @param stableDiffusionGenerationRepository Generation and result persistence for AUTOMATIC1111.
 * @param hordeGenerationRepository Generation and result persistence for AI Horde.
 * @param huggingFaceGenerationRepository Generation and result persistence for Hugging Face.
 * @param openAiGenerationRepository Generation and result persistence for official OpenAI.
 * @param openAiCompatibleGenerationRepository Generation and result persistence for OpenAI-compatible.
 * @param openAiOAuthGenerationRepository Generation and result persistence for ChatGPT OAuth.
 * @param stabilityAiGenerationRepository Generation and result persistence for Stability AI.
 * @param falAiGenerationRepository Generation and result persistence for Fal.ai.
 * @param arliAiGenerationRepository Generation and result persistence for ArliAI.
 * @param swarmUiGenerationRepository Generation and result persistence for SwarmUI.
 * @param localDiffusionGenerationRepository Generation and result persistence for legacy ONNX.
 * @param mediaPipeGenerationRepository Generation and result persistence for MediaPipe.
 * @param stableDiffusionCppGenerationRepository Generation and result persistence for stable-diffusion.cpp.
 * @param coreMlGenerationRepository Generation and result persistence for Core ML.
 * @param bonsaiGenerationRepository Generation and result persistence for Bonsai.
 * @param mnnGenerationRepository Isolated MNN request execution after fixed-profile and seed admission.
 * @param preferenceManager Current provider selection and independent generation preferences.
 * @param generateEasyDiffusionUseCase Owns captured-task batching, stream recovery, cancellation and deadlines.
 * @param captureEasyDiffusionConnectionUseCase Captures endpoint/checkpoint under the provider-selection lock, retaining the submitted revision.
 *
 * @author Dmitriy Moroz
 */
internal class TextToImageUseCaseImpl(

    private val stableDiffusionGenerationRepository: StableDiffusionGenerationRepository,

    private val hordeGenerationRepository: HordeGenerationRepository,

    private val huggingFaceGenerationRepository: HuggingFaceGenerationRepository,

    private val openAiGenerationRepository: OpenAiGenerationRepository,
    private val openAiCompatibleGenerationRepository: OpenAiCompatibleGenerationRepository,
    private val openAiOAuthGenerationRepository: OpenAiOAuthGenerationRepository,

    private val stabilityAiGenerationRepository: StabilityAiGenerationRepository,

    private val falAiGenerationRepository: FalAiGenerationRepository,

    private val arliAiGenerationRepository: ArliAiGenerationRepository,

    private val swarmUiGenerationRepository: SwarmUiGenerationRepository,

    private val localDiffusionGenerationRepository: LocalDiffusionGenerationRepository,

    private val mediaPipeGenerationRepository: MediaPipeGenerationRepository,

    private val stableDiffusionCppGenerationRepository: StableDiffusionCppGenerationRepository,

    private val coreMlGenerationRepository: CoreMlGenerationRepository,

    private val bonsaiGenerationRepository: BonsaiGenerationRepository,
    private val mnnGenerationRepository: MnnGenerationRepository,

    private val preferenceManager: PreferenceManager,
    private val generateEasyDiffusionUseCase: GenerateEasyDiffusionUseCase,
    private val captureEasyDiffusionConnectionUseCase: CaptureEasyDiffusionConnectionUseCase,
) : TextToImageUseCase {

    /**
     * Routes work to its captured provider and executes sequential single-image batches when needed.
     * Rejects a source mismatch before dispatch; OAuth repositories additionally enforce captured
     * account/session and durable selection revisions before HTTP and result persistence.
     *
     * @param payload Immutable generation inputs and token-free submission identity.
     * @return Successful images in batch order; a failure or cancellation stops remaining requests.
     * @throws IllegalStateException When the submitted source no longer matches the selected provider.
     *
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke(
        payload: TextToImagePayload,
    ): List<AiGenerationResult> {
        val source = payload.sourceSnapshot ?: preferenceManager.source
        check(source == preferenceManager.source) { "Generation source changed" }
        if (source == ServerSource.LOCAL_MNN) {
            require(payload.batchCount == 1 && payload.negativePrompt.isEmpty())
            require(payload.width == 512 && payload.height == 512 && payload.cfgScale == 7.5f)
            require(payload.samplingSteps in 1..50 && payload.sampler == "PNDM")
            require(payload.mnnProfile == "sd15-512-general-int8-v1" && !payload.mnnModelId.isNullOrBlank() && !payload.mnnActivationId.isNullOrBlank())
            val seed =
                if (payload.seed.isBlank() || payload.seed == "-1") Random.nextInt(Int.MAX_VALUE)
                    .toString()
                else payload.seed.also { require(it.toLongOrNull() in 0..Int.MAX_VALUE.toLong()) }
            return listOf(mnnGenerationRepository.generateFromText(payload.copy(seed = seed)))
        }
        return when (source) {
            ServerSource.EASY_DIFFUSION -> generateEasyDiffusionUseCase(
                payload.toEasyDiffusionRequest(captureEasyDiffusionConnectionUseCase(payload.providerSelectionRevision))
            )

            ServerSource.AUTOMATIC1111 -> stableDiffusionGenerationRepository.generateFromText(
                payload
            )

            ServerSource.FAL_AI -> falAiGenerationRepository.generateFromText(payload)
            ServerSource.ARLI_AI -> arliAiGenerationRepository.generateFromText(payload)
            else -> List(payload.batchCount.coerceAtLeast(1)) {
                coroutineContext.ensureActive()
                generateSingle(payload)
            }
        }
    }

    /**
     * Dispatches one image to the captured provider; providers with dedicated batch paths are rejected here.
     *
     * @param payload Immutable supported generation inputs and captured provider identity.
     *
     * @author Dmitriy Moroz
     */
    private suspend fun generateSingle(payload: TextToImagePayload) =
        when (payload.sourceSnapshot ?: preferenceManager.source) {
            ServerSource.EASY_DIFFUSION -> error("Easy Diffusion batch must use its captured operation")
            ServerSource.HORDE -> hordeGenerationRepository.generateFromText(payload)
            ServerSource.LOCAL_MICROSOFT_ONNX -> localDiffusionGenerationRepository.generateFromText(
                payload
            )

            ServerSource.HUGGING_FACE -> huggingFaceGenerationRepository.generateFromText(payload)
            ServerSource.OPEN_AI -> openAiGenerationRepository.generateFromText(payload)
            ServerSource.OPEN_AI_COMPATIBLE -> openAiCompatibleGenerationRepository.generateFromText(
                payload
            )

            ServerSource.OPEN_AI_OAUTH -> openAiOAuthGenerationRepository.generateFromText(payload)
            ServerSource.STABILITY_AI -> stabilityAiGenerationRepository.generateFromText(payload)
            ServerSource.SWARM_UI -> swarmUiGenerationRepository.generateFromText(payload)
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> mediaPipeGenerationRepository.generateFromText(
                payload
            )

            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> stableDiffusionCppGenerationRepository.generateFromText(
                payload
            )

            ServerSource.LOCAL_APPLE_CORE_ML -> coreMlGenerationRepository.generateFromText(payload)
            ServerSource.LOCAL_MNN -> mnnGenerationRepository.generateFromText(payload)
            ServerSource.LOCAL_APPLE_BONSAI -> bonsaiGenerationRepository.generateFromText(payload)
            ServerSource.AUTOMATIC1111 -> error("Automatic1111 batch must be generated through generateFromText(payload).")
            ServerSource.FAL_AI -> error("Fal.ai batch must be generated through generateFromText(payload).")
            ServerSource.ARLI_AI -> error("ArliAI batch must be generated through generateFromText(payload).")
        }
}
