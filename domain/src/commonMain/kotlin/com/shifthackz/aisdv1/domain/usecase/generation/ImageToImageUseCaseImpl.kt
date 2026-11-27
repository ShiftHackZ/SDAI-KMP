package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.usecase.civitai.AutoUploadToCivitaiUseCase
import com.shifthackz.aisdv1.domain.gateway.BenchmarkDeviceProbe
import com.shifthackz.aisdv1.domain.entity.GenerationMetadataContext
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgressContext
import kotlinx.coroutines.withContext
import com.shifthackz.aisdv1.domain.entity.GenerationFinalizationContext

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.CaptureEasyDiffusionConnectionUseCase

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GenerateEasyDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.toEasyDiffusionRequest

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate
import com.shifthackz.aisdv1.domain.entity.isUpscalerInputSizeValid
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
 * @param upscaleImageUseCase Applies the captured final-stage super-resolution selection.
 * @param finalizeGenerationUseCase Saves only completed upscaled pixels using normal gallery policy.
 * @param deviceProbe Captures originating hardware and OS without loading model runtimes.
 * @param autoUploadToCivitaiUseCase Applies optional public sharing only after a generation completes.
 * @param preferenceManager Current provider selection and independent generation preferences.
 * @param trackRemoteGenerationUseCase Owns request-correlated remote telemetry and its cleanup.
 * @param generationRestoreGate Serializes generation against portable backup restoration.
 * @param generateEasyDiffusionUseCase Owns captured-task batching, stream recovery, cancellation and deadlines.
 * @param captureEasyDiffusionConnectionUseCase Captures endpoint/checkpoint under the provider-selection lock, retaining the submitted revision.
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

    private val upscaleImageUseCase: UpscaleImageUseCase,
    private val finalizeGenerationUseCase: FinalizeGenerationUseCase,
    private val deviceProbe: BenchmarkDeviceProbe,
    private val autoUploadToCivitaiUseCase: AutoUploadToCivitaiUseCase,
    private val preferenceManager: PreferenceManager,
    private val trackRemoteGenerationUseCase: TrackRemoteGenerationUseCase,
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
        generationRestoreGate.generating {
            require(payload.isUpscalerInputSizeValid) { "With upscaling enabled, generation size must not exceed 1024 × 1024" }
            require(payload.upscaler?.modelId?.isBlank() != true) { "Configure an upscaler model first" }
            val source = payload.sourceSnapshot ?: preferenceManager.source
            val device = deviceProbe.capture()
            withGenerationMetadata(
                GenerationMetadataContext(
                    source.key,
                    if (source == ServerSource.LOCAL_APPLE_CORE_ML) coreMlAcceleration() else "None",
                    deviceName = device.generationDeviceName(),
                    osVersion = device.generationOsVersion(),
                )
            ) {
                suspend fun execute() =
                    if (source == ServerSource.AUTOMATIC1111 || source == ServerSource.SWARM_UI) {
                        trackRemoteGenerationUseCase(source) { generate(payload) }
                    } else generate(payload)

                val upscaler = payload.upscaler
                if (upscaler == null) execute()
                else withContext(GenerationFinalizationContext()) {
                    execute().map { result ->
                        finalizeGenerationUseCase(upscaleImageUseCase(result, upscaler))
                    }
                }
            }.map { autoUploadToCivitaiUseCase(it) }
        }

    /**
     * Executes a captured image request while this operation owns generation admission.
     *
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
            else -> List(payload.batchCount.coerceAtLeast(1)) { index ->
                coroutineContext.ensureActive()
                check(source == preferenceManager.source) { "Generation source changed" }
                val context = coroutineContext[RemoteGenerationProgressContext]
                if (context == null) generateTimedSingle(payload) else withContext(
                    RemoteGenerationProgressContext(context.taskId) { fraction ->
                        context.report(fraction?.let {
                            (index + it) / payload.batchCount.coerceAtLeast(
                                1
                            )
                        })
                    }
                ) { generateTimedSingle(payload) }
            }
        }

    }

    /**
     * Starts an independent duration for a sequential image, retaining the submitted provenance.
     *
     * @param payload Captured image inputs.
     * @author Dmitriy Moroz
     */
    private suspend fun generateTimedSingle(payload: ImageToImagePayload): AiGenerationResult {
        val metadata = requireNotNull(coroutineContext[GenerationMetadataContext])
        return withGenerationMetadata(
            GenerationMetadataContext(
                metadata.providerKey,
                metadata.acceleration,
                deviceName = metadata.deviceName,
                osVersion = metadata.osVersion
            )
        ) { generateSingle(payload) }
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
