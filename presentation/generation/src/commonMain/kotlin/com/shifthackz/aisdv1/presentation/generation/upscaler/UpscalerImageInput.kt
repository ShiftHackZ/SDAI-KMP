package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberPreviewImage
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImagePickSource
import com.shifthackz.aisdv1.presentation.generation.widget.imageinput.GenerationImageInput

/**
 * Connects the shared image card to retained upscaler inputs and bounded preview decoding.
 *
 * @param state Original encoded input and acquisition/operation availability.
 * @param processIntent Receives native acquisition, history selection and clear actions.
 * @param modifier Placement of the complete acquisition and image-preview card.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerImageInput(
    state: UpscalerState,
    processIntent: (UpscalerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        val edge = with(LocalDensity.current) { maxWidth.roundToPx() }
        GenerationImageInput(
            image = rememberPreviewImage(state.image, edge),
            hasImage = state.image.isNotEmpty(),
            isPickingImage = state.isPickingImage,
            isEnabled = state.isSupported && !state.isRunning,
            onGalleryClick = {
                processIntent(
                    UpscalerIntent.PickImage(
                        ImageToImagePickSource.Gallery
                    )
                )
            },
            onCameraClick = { processIntent(UpscalerIntent.PickImage(ImageToImagePickSource.Camera)) },
            onRandomClick = { processIntent(UpscalerIntent.RandomImage) },
            onHistoryImageSelected = { processIntent(UpscalerIntent.SelectImage(it)) },
            onClearClick = { processIntent(UpscalerIntent.ClearImage) },
        )
    }
}
