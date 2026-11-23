package com.shifthackz.aisdv1.presentation.history.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.presentation.history.HistoryState

/**
 * Supplies read outcomes, thumbnail shapes and metadata edge cases using the real history state.
 * Refreshing and failed snapshots retain cached entries while the renderer prioritizes status.
 *
 * @author Dmitriy Moroz
 */
internal class HistoryPreviewParameterProvider : PreviewParameterProvider<HistoryPreviewCase> {
    private val square = HistoryPreviewFixtures.square
    private val ready = HistoryState(loading = false, items = listOf(square))
    private val cases = listOf(
        HistoryPreviewCase("Initial loading", HistoryState()),
        HistoryPreviewCase("Refreshing cached entries", ready.copy(loading = true)),
        HistoryPreviewCase("Empty history", HistoryState(loading = false)),
        HistoryPreviewCase(
            "Read failure",
            HistoryState(loading = false, error = "Unable to load history")
        ),
        HistoryPreviewCase(
            "Long read failure", HistoryState(
                loading = false,
                error = "History could not be read because storage is temporarily unavailable. Your saved images are still on the device. Try refreshing again.",
            )
        ),
        HistoryPreviewCase(
            "Failure with cached entries",
            ready.copy(error = "Unable to refresh history")
        ),
        HistoryPreviewCase("Square thumbnail", ready),
        HistoryPreviewCase(
            "Portrait thumbnail",
            ready.copy(items = listOf(HistoryPreviewFixtures.portrait))
        ),
        HistoryPreviewCase(
            "Landscape thumbnail",
            ready.copy(items = listOf(HistoryPreviewFixtures.landscape))
        ),
        HistoryPreviewCase(
            "Image loading or unavailable",
            ready,
            unavailableImageIds = setOf(square.id)
        ),
        HistoryPreviewCase(
            "Long prompt",
            ready.copy(items = listOf(HistoryPreviewFixtures.longPrompt))
        ),
        HistoryPreviewCase(
            "Blank text-to-image prompt",
            ready.copy(items = listOf(square.copy(prompt = "")))
        ),
        HistoryPreviewCase(
            "Blank image-to-image prompt", ready.copy(
                items = listOf(
                    square.copy(
                        prompt = " \n ",
                        type = AiGenerationResult.Type.IMAGE_TO_IMAGE
                    )
                )
            )
        ),
        HistoryPreviewCase(
            "Unknown dimensions",
            ready.copy(items = listOf(square.copy(width = 0, height = 0)))
        ),
        HistoryPreviewCase(
            "Mixed history list", ready.copy(items = HistoryPreviewFixtures.mixedItems),
            unavailableImageIds = setOf(2, 5, 8, 11),
        ),
    )

    /**
     * Ordered history snapshots rendered by both palette annotations.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<HistoryPreviewCase> = cases.asSequence()

    /**
     * Identifies the read outcome, image shape or metadata edge case above its preview.
     *
     * @param index Position in the ordered scenario sequence.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
