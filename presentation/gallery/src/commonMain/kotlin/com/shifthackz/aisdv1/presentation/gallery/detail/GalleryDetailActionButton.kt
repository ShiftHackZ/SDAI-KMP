package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp

/**
 * Keeps compact gallery-action geometry with persistent help and caller-owned primary behavior.
 *
 * @param icon Vector shared by the action and help title.
 * @param label Localized compact action name.
 * @param helpMessage Localized explanation including any data changes.
 * @param onClick Requests the operation on a short tap.
 * @param modifier Size and placement of the complete action anchor.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailActionButton(
    icon: ImageVector,
    label: String,
    helpMessage: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        ContextualHelp(
            title = label,
            message = helpMessage,
            titleStartIcon = icon,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(42.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                onClick = onClick,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Text(
                    modifier = Modifier.padding(start = 6.dp),
                    text = label,
                    color = LocalContentColor.current,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
