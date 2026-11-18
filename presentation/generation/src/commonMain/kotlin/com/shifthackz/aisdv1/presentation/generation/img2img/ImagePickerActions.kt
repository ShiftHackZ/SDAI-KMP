package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Shows a large image-source tile for the empty input-image state.
 *
 * @param text Localized acquisition-action label.
 * @param icon Camera/gallery/action icon shown beside the label.
 * @param enabled Whether this acquisition action can be started.
 * @param onClick Requests native image acquisition through the form owner.
 * @param modifier Size and placement of the button in its action group.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImagePickerTileButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        modifier = modifier.height(112.dp),
        enabled = enabled,
        shape = RoundedCornerShape(size = 28.dp),
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

/**
 * Shows a compact image-source action beside an existing input image.
 *
 * @param text Localized acquisition-action label.
 * @param icon Camera/gallery/action icon shown beside the label.
 * @param enabled Whether this acquisition action can be started.
 * @param onClick Requests native image acquisition through the form owner.
 * @param modifier Size and placement of the button in its action group.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImagePickerButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(horizontal = 12.dp),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(ButtonDefaults.IconSize),
            imageVector = icon,
            contentDescription = null,
        )
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
