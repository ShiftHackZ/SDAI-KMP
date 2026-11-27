package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.generation.widget.dialog.GenerationImageResultDialog
import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewTheme
import com.shifthackz.aisdv1.presentation.generation.preview.rememberGenerationPreviewBitmap
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerScreenContent
import com.shifthackz.aisdv1.presentation.generation.widget.imageinput.GenerationImageInput

/**
 * Displays the production standalone renderer with fixed source pixels and inert intents.
 *
 * @param scenario Source shape, model choices, operation feedback and initial scroll position.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerScreenPreview(scenario: UpscalerScreenPreviewCase) {
    val modal = scenario.state.screenModal
    val state =
        if (modal is GenerationModal.Image.Single) scenario.state.copy(screenModal = GenerationModal.None) else scenario.state
    val longestEdge = maxOf(state.width, state.height, 1)
    val image = rememberGenerationPreviewBitmap(
        width = (state.width.coerceAtLeast(1) * 192 / longestEdge).coerceIn(1, 192),
        height = (state.height.coerceAtLeast(1) * 192 / longestEdge).coerceIn(1, 192),
    )
    GenerationPreviewTheme {
        UpscalerScreenContent(
            state = state, upscalerFormState = scenario.formState, processIntent = {},
            listState = rememberLazyListState(initialFirstVisibleItemIndex = scenario.firstVisibleItemIndex),
            inputImageContent = {
                GenerationImageInput(
                    image = image.takeIf { scenario.shouldShowImage },
                    hasImage = state.image.isNotEmpty(), isPickingImage = state.isPickingImage,
                    isEnabled = state.isSupported && !state.isRunning,
                    onGalleryClick = {}, onCameraClick = {}, onRandomClick = {},
                    onHistoryImageSelected = {}, onClearClick = {},
                )
            },
        )
        if (modal is GenerationModal.Image.Single) {
            GenerationImageResultDialog(
                imageBase64 = modal.result.image,
                showSaveButton = !modal.autoSaveEnabled,
                showReportButton = modal.reportEnabled,
                imageContent = { _, modifier ->
                    Image(bitmap = image, modifier = modifier, contentDescription = null)
                },
            )
        }
    }
}
