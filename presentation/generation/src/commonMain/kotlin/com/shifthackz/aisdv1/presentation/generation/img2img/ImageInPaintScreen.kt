package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog
import com.shifthackz.aisdv1.presentation.components.widget.image.DecodedImageBitmap
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberOriginalImage

/**
 * Decodes the selected source image and binds inpainting to the retained image-to-image state holder.
 * An unavailable image presents the existing error acknowledgement before returning to its caller.
 *
 * @param viewModel Shared image-to-image owner retaining the selected image and mask edits.
 * @param onClose Returns to the originating image-to-image screen.
 * @param modifier Placement of the inpainting editor.
 * @author Dmitriy Moroz
 */
@Composable
fun ImageInPaintScreen(
    viewModel: ImageToImageViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val decoded = rememberOriginalImage(state.imageBase64)
    val image = (decoded as? DecodedImageBitmap.Ready)?.image
    if (decoded == DecodedImageBitmap.Unavailable) {
        ErrorDialog(
            text = Localization.string("error_generic").asUiText(),
            onDismissRequest = onClose
        )
    } else if (image != null) {
        ImageInPaintScreenContent(
            modifier = modifier.fillMaxSize(),
            image = image,
            state = state.inPaint,
            processIntent = viewModel::processIntent,
            onClose = onClose,
        )
    }
}
