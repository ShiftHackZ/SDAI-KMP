package com.shifthackz.aisdv1.presentation.components.widget.imageinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArtTrack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Displays the four acquisition routes in equally sized two-column rows.
 *
 * @param isEnabled Whether image selection can begin.
 * @param onGalleryClick Opens device photos.
 * @param onCameraClick Opens native camera capture.
 * @param onRandomClick Requests a remote random image.
 * @param onHistoryClick Opens the saved-image history selector.
 * @param modifier Placement of the source grid.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImageInputActions(
    isEnabled: Boolean,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRandomClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ImagePickerTileButton(
                text = Localization.string("action_image_picker_gallery"),
                helpMessage = Localization.string("help_input_photo"),
                icon = Icons.Default.Image,
                isEnabled = isEnabled,
                onClick = onGalleryClick,
                modifier = Modifier.weight(1f),
            )
            ImagePickerTileButton(
                text = Localization.string("action_image_picker_camera"),
                helpMessage = Localization.string("help_input_camera"),
                icon = Icons.Default.Camera,
                isEnabled = isEnabled,
                onClick = onCameraClick,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ImagePickerTileButton(
                text = Localization.string("action_choose_generation"),
                helpMessage = Localization.string("help_input_saved_image"),
                icon = Icons.Default.Collections,
                isEnabled = isEnabled,
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f),
            )
            ImagePickerTileButton(
                text = Localization.string("action_image_picker_random"),
                helpMessage = Localization.string("help_input_random_image"),
                icon = Icons.Default.ArtTrack,
                isEnabled = isEnabled,
                onClick = onRandomClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
