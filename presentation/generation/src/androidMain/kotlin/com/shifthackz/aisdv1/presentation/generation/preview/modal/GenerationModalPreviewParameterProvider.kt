package com.shifthackz.aisdv1.presentation.generation.preview.modal

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.HordeProcessStatus
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkLimitExceeded
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkRecommendation
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal

/**
 * Supplies remote queue, native phases, determinate/indeterminate progress and admission decisions.
 *
 * @author Dmitriy Moroz
 */
internal class GenerationModalPreviewParameterProvider :
    PreviewParameterProvider<GenerationModalPreviewCase> {
    private val recommendation = BenchmarkRecommendation(
        width = 512, height = 512, samplingSteps = 20, cfgScale = 7f, batchCount = 1,
        providers = listOf(ServerSource.LOCAL_MICROSOFT_ONNX), backgroundGeneration = true,
        sdxlBackend = SdxlBackend.AUTO,
    )
    private val cases = listOf(
        GenerationModalPreviewCase("Remote request", GenerationModal.Communicating(), "00:12"),
        GenerationModalPreviewCase(
            "Remote request without cancellation",
            GenerationModal.Communicating(canCancel = false)
        ),
        GenerationModalPreviewCase(
            "Horde queue",
            GenerationModal.Communicating(hordeProcessStatus = HordeProcessStatus(95, 4)),
            "01:05"
        ),
        GenerationModalPreviewCase(
            "Horde unknown queue position",
            GenerationModal.Communicating(hordeProcessStatus = HordeProcessStatus(30, null))
        ),
        GenerationModalPreviewCase("Indeterminate generation", GenerationModal.Generating()),
        GenerationModalPreviewCase(
            "Sampling steps",
            GenerationModal.Generating(canCancel = true, status = LocalDiffusionStatus(7, 20)),
            "00:42"
        ),
        GenerationModalPreviewCase(
            "Sampling complete",
            GenerationModal.Generating(canCancel = true, status = LocalDiffusionStatus(20, 20)),
            "01:02"
        ),
        GenerationModalPreviewCase(
            "MNN overall progress",
            GenerationModal.Generating(
                canCancel = true,
                status = LocalDiffusionStatus(43, 100),
                nativeEngine = NativeGenerationEngine.MNN,
                nativeStage = NativeGenerationStage.RUNNING,
                isOverallProgress = true
            ),
            "01:23"
        ),
        GenerationModalPreviewCase(
            "Overall progress unknown",
            GenerationModal.Generating(
                canCancel = true,
                status = LocalDiffusionStatus(0, 0),
                isOverallProgress = true
            )
        ),
        GenerationModalPreviewCase(
            "Custom provider title",
            GenerationModal.Generating(
                title = "Remote image engine".asUiText(),
                canCancel = true,
                status = LocalDiffusionStatus(4, 12)
            )
        ),
        GenerationModalPreviewCase(
            "Hour elapsed time",
            GenerationModal.Generating(canCancel = true),
            "01:02:03"
        ),
        GenerationModalPreviewCase(
            "Resource overlay",
            GenerationModal.Generating(canCancel = true, status = LocalDiffusionStatus(6, 20)),
            "00:38",
            shouldShowPerformance = true
        ),
        GenerationModalPreviewCase(
            "Background already running",
            GenerationModal.Background.Running
        ),
        GenerationModalPreviewCase("Background scheduled", GenerationModal.Background.Scheduled),
        GenerationModalPreviewCase(
            "First local benchmark",
            GenerationModal.Benchmark.FirstLocalGeneration
        ),
        GenerationModalPreviewCase(
            "All benchmark warnings",
            GenerationModal.Benchmark.ExceedsRecommendation(
                BenchmarkLimitExceeded.entries,
                recommendation
            )
        ),
        GenerationModalPreviewCase(
            "Generation error",
            GenerationModal.Error("The provider could not complete this request.".asUiText())
        ),
        GenerationModalPreviewCase(
            "Long generation error",
            GenerationModal.Error("The requested generation could not be completed because the selected model is unavailable. Select an available model in Configuration and try again.".asUiText())
        ),
    ) + NativeGenerationStage.entries.map { stage ->
        GenerationModalPreviewCase(
            "Native stage · ${stage.name}", GenerationModal.Generating(
                canCancel = true,
                nativeEngine = if (stage == NativeGenerationStage.RUNNING) NativeGenerationEngine.MNN else NativeGenerationEngine.ONNX,
                nativeStage = stage,
                status = LocalDiffusionStatus(5, 20),
            ), "00:25"
        )
    } + NativeGenerationEngine.entries.map { engine ->
        GenerationModalPreviewCase(
            "Engine counter policy · ${engine.name}", GenerationModal.Generating(
                canCancel = true, nativeEngine = engine,
                nativeStage = if (engine == NativeGenerationEngine.ONNX || engine == NativeGenerationEngine.MEDIA_PIPE) NativeGenerationStage.DENOISING else NativeGenerationStage.RUNNING,
                status = when (engine) {
                    NativeGenerationEngine.ONNX -> LocalDiffusionStatus(5, 20)
                    NativeGenerationEngine.MEDIA_PIPE -> LocalDiffusionStatus(0, 20)
                    else -> LocalDiffusionStatus(25, 100)
                },
                isOverallProgress = engine != NativeGenerationEngine.ONNX && engine != NativeGenerationEngine.MEDIA_PIPE,
            )
        )
    } + BenchmarkLimitExceeded.entries.map { reason ->
        GenerationModalPreviewCase(
            "Benchmark warning · ${reason.name}",
            GenerationModal.Benchmark.ExceedsRecommendation(listOf(reason), recommendation)
        )
    }

    /**
     * Fixed operation overlays without generation, workers, benchmark execution or cancellation effects.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<GenerationModalPreviewCase> = cases.asSequence()

    /**
     * Identifies the provider progress phase, admission decision or failure.
     *
     * @param index Position in the ordered operation-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
