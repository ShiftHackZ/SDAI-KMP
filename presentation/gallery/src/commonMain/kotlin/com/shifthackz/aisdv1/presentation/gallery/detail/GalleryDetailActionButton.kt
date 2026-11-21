package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpCircleIconButton

/**
 * Wraps a platform circular icon action with persistent long-press help and an accessible name.
 *
 * @param icon Vector shared by the action and help title.
 * @param label Localized action name used by accessibility and help.
 * @param helpMessage Localized explanation including any data changes.
 * @param onClick Requests the operation on a short tap.
 * @param modifier Size and placement of the complete action anchor.
 * @param tint Icon color for normal, selected or destructive actions.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailActionButton(
    icon: ImageVector,
    label: String,
    helpMessage: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    ContextualHelpCircleIconButton(
        title = label,
        message = helpMessage,
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        tint = tint,
    )
}
