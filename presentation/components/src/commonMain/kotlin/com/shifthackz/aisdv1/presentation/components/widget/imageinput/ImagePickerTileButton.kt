package com.shifthackz.aisdv1.presentation.components.widget.imageinput

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Shows a large image-source tile for the empty input-image state.
 *
 * @param text Localized acquisition-action label.
 * @param helpMessage Localized purpose of this acquisition route.
 * @param icon Camera/gallery/action icon shown beside the label.
 * @param isEnabled Whether this acquisition action can be started.
 * @param onClick Requests the acquisition route represented by this tile.
 * @param modifier Size and placement of the button in its action group.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImagePickerTileButton(
    text: String,
    helpMessage: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        ContextualHelp(
            title = text,
            message = helpMessage,
            titleStartIcon = icon,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(112.dp),
                isEnabled = isEnabled,
                shape = RoundedCornerShape(size = 28.dp),
                shouldClipBackground = true,
                contentPadding = PaddingValues(8.dp),
                onClick = onClick,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = icon,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = text,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
