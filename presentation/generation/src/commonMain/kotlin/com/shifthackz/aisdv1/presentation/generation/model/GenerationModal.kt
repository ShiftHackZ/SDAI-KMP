package com.shifthackz.aisdv1.presentation.generation.model

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.HordeProcessStatus
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkLimitExceeded
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkRecommendation

/**
 * Dialog state for generation progress, background work, decisions and completed results.
 *
 * @author Dmitriy Moroz
 */
sealed interface GenerationModal {

    /**
     * Keeps the generation surface free of modal content.
     *
     * @author Dmitriy Moroz
     */
    data object None : GenerationModal

    /**
     * Notices confirming background work admission or an already running operation.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Background : GenerationModal {
        /**
         * Explains that the existing background operation is still running.
         *
         * @author Dmitriy Moroz
         */
        data object Running : Background

        /**
         * Confirms that background work was scheduled for later execution.
         *
         * @author Dmitriy Moroz
         */
        data object Scheduled : Background
    }

    /**
     * Captures benchmark decisions shown before local generation starts.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Benchmark : GenerationModal {
        /**
         * Asks the user whether to run a benchmark before first local generation.
         *
         * @author Dmitriy Moroz
         */
        data object FirstLocalGeneration : Benchmark

        /**
         * Warns that the selected local parameters exceed the benchmark guidance.
         *
         * @param reasons exceeded recommendation limits.
         * @param recommendation latest benchmark recommendation.
         * @author Dmitriy Moroz
         */
        @Immutable
        data class ExceedsRecommendation(
            val reasons: List<BenchmarkLimitExceeded>,
            val recommendation: BenchmarkRecommendation,
        ) : Benchmark
    }

    /**
     * Captures active generation progress and cancellation controls for the shared dialog.
     *
     * @param title Provider-specific heading, or null for the standard local-generation heading.
     * @param canCancel Whether cancellation is available for this operation.
     * @param status Current provider counters in their declared progress unit.
     * @param nativeEngine Captured Android engine used for lifecycle-specific rendering.
     * @param nativeStage Sanitized native lifecycle stage, absent for generic provider progress.
     * @param isOverallProgress Whether counters represent overall work rather than sampling steps.
     *
     * @author Dmitriy Moroz
     */
    @Immutable
    data class Generating(
        val title: UiText? = null,
        val canCancel: Boolean = false,
        val status: LocalDiffusionStatus? = null,
        val nativeEngine: NativeGenerationEngine? = null,
        val nativeStage: NativeGenerationStage? = null,
        val isOverallProgress: Boolean = false,
    ) : GenerationModal {
        /**
         * Sampling counters shown during denoising when the provider reports intermediate steps.
         * MediaPipe only reports initial/completed counts, so its indicator remains indeterminate.
         *
         * @author Dmitriy Moroz
         */
        val pair: Pair<Int, Int>?
            get() = status?.takeIf { !isOverallProgress && nativeEngine != NativeGenerationEngine.MEDIA_PIPE && it.total > 0 && (nativeStage == null || nativeStage == NativeGenerationStage.DENOISING) }
                ?.let { (current, total) -> current to total }

        /**
         * Bounded overall completion percentage, independent of the requested sampling count.
         *
         * @author Dmitriy Moroz
         */
        val overallPercentage: Int?
            get() = status?.takeIf { isOverallProgress && it.total > 0 && (nativeStage == null || nativeStage == NativeGenerationStage.RUNNING) }
                ?.let { (it.current.toLong() * 100 / it.total).coerceIn(0, 100).toInt() }
    }

    /**
     * Captures remote queue status before generation results become available.
     *
     * @param canCancel Whether the remote operation offers cancellation.
     * @param hordeProcessStatus Latest queue position and wait estimate.
     *
     * @author Dmitriy Moroz
     */
    @Immutable
    data class Communicating(
        val canCancel: Boolean = true,
        val hordeProcessStatus: HordeProcessStatus? = null,
    ) : GenerationModal

    /**
     * Displays a controlled generation failure and its recovery message.
     *
     * @param error Localized user-visible failure message.
     * @author Dmitriy Moroz
     */
    @Immutable
    data class Error(val error: UiText) : GenerationModal

    /**
     * Completed results offered for inspection, export and optional reporting.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Image : GenerationModal {

        /**
         * Presents one complete generated image and its available result actions.
         *
         * @param result Complete image and captured generation metadata.
         * @param autoSaveEnabled Whether normal persistence already exported this image.
         * @param reportEnabled Whether reporting is available for this result.
         *
         * @author Dmitriy Moroz
         */
        @Immutable
        data class Single(
            val result: AiGenerationResult,
            val autoSaveEnabled: Boolean,
            val reportEnabled: Boolean,
        ) : Image

        /**
         * Presents a complete generated image batch and shared result actions.
         *
         * @param results Complete images and their captured generation metadata.
         * @param autoSaveEnabled Whether normal persistence already exported these images.
         *
         * @author Dmitriy Moroz
         */
        @Immutable
        data class Batch(
            val results: List<AiGenerationResult>,
            val autoSaveEnabled: Boolean,
        ) : Image

        /**
         * Selects an individual or batch result dialog from successful images.
         *
         * @author Dmitriy Moroz
         */
        companion object {
            /**
             * Chooses the single-image or batch dialog from complete successful results.
             *
             * @param list Complete generated images; a single item uses the individual result dialog.
             * @param autoSaveEnabled Whether normal persistence already exported these images.
             * @param reportEnabled Whether reporting is available for the results.
             * @author Dmitriy Moroz
             */
            fun create(
                list: List<AiGenerationResult>,
                autoSaveEnabled: Boolean,
                reportEnabled: Boolean = false,
            ): Image = if (list.size > 1) {
                Batch(list, autoSaveEnabled)
            } else {
                Single(list.first(), autoSaveEnabled, reportEnabled)
            }
        }
    }
}
