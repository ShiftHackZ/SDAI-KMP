package com.shifthackz.aisdv1.presentation.components.widget.imageinput

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.modal.history.InputHistoryBottomSheet
import com.shifthackz.aisdv1.presentation.components.widget.section.ExpandableSectionHeader
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor

/**
 * Offers device photos, camera, random images and saved gallery images to an owning image field.
 * History selection delivers only output pixels to this instance; the caller retains its form state.
 * The header starts expanded and retains its disclosure state across recreation.
 * Native acquisition and preview rendering remain caller-owned so screens can supply their own editor.
 *
 * @param isPickingImage Whether native or remote image acquisition is running.
 * @param isEnabled Whether the owning screen allows a new image selection.
 * @param hasImage Whether to render the selected-image slot instead of the source grid.
 * @param onGalleryClick Opens the device photo picker.
 * @param onCameraClick Opens native camera capture.
 * @param onRandomClick Requests a random image through the owner's acquisition flow.
 * @param onHistoryImageSelected Receives the selected gallery output as Base64, without restoring metadata.
 * @param modifier Placement and sizing of the card.
 * @param selectedImage Preview and editing controls supplied by the owning screen.
 * @author Dmitriy Moroz
 */
@Composable
fun ImageInputCard(
    isPickingImage: Boolean,
    isEnabled: Boolean,
    hasImage: Boolean,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRandomClick: () -> Unit,
    onHistoryImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedImage: @Composable () -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    var isHistoryVisible by rememberSaveable { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        CompositionLocalProvider(LocalPlatformControlBackgroundColor provides MaterialTheme.colorScheme.surfaceVariant) {
            Column {
                ExpandableSectionHeader(
                    title = Localization.string("title_input_image"),
                    isExpanded = isExpanded,
                    onToggleExpanded = { isExpanded = !isExpanded },
                    trailingContent = {
                        if (isPickingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                            )
                        }
                    },
                )
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                        if (hasImage) selectedImage() else ImageInputActions(
                            isEnabled = isEnabled,
                            onGalleryClick = onGalleryClick,
                            onCameraClick = onCameraClick,
                            onRandomClick = onRandomClick,
                            onHistoryClick = { isHistoryVisible = true },
                        )
                    }
                }
            }
        }
    }
    if (isHistoryVisible && isEnabled) {
        InputHistoryBottomSheet(
            onClose = { isHistoryVisible = false },
            onGenerationSelected = { result ->
                isHistoryVisible = false
                onHistoryImageSelected(result.image)
            },
        )
    }
}
