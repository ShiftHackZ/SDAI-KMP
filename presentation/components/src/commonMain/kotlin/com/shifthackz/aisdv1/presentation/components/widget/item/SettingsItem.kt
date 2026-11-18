package com.shifthackz.aisdv1.presentation.components.widget.item

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString

/**
 * Renders a settings row with optional loading, selection emphasis and trailing value controls.
 *
 * @param modifier Layout modifiers for the complete row or loading placeholder.
 * @param loading Replaces content with a shimmer while the setting loads.
 * @param enabled Whether row clicks are accepted.
 * @param selected Whether a selection border highlights the row.
 * @param startIcon Optional leading vector icon.
 * @param text Localized setting label resolved during composition.
 * @param animateBackground Whether the row cycles between theme highlight colors.
 * @param showChevron Displays a navigation chevron when no custom trailing content is supplied.
 * @param endValueText Textual value displayed at the trailing edge.
 * @param endValueContent Custom trailing content replacing the textual value and chevron.
 * @param textSuffixContent Additional inline content after the main label.
 * @param startIconContent Custom leading content beside the label.
 * @param onClick Requests the setting's action. Return values and persistence stay caller-owned.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    selected: Boolean = false,
    startIcon: ImageVector? = null,
    text: UiText,
    animateBackground: Boolean = false,
    showChevron: Boolean = true,
    endValueText: UiText = UiText.empty,
    endValueContent: (@Composable () -> Unit)? = null,
    textSuffixContent: (@Composable () -> Unit)? = null,
    startIconContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    SettingsItem(
        modifier = modifier,
        loading = loading,
        enabled = enabled,
        selected = selected,
        text = text.asString(),
        animateBackground = animateBackground,
        showChevron = showChevron,
        endValueText = endValueText.asString(),
        endValueContent = endValueContent,
        textSuffixContent = textSuffixContent,
        startIconContent = settingsStartIconContent(
            startIcon = startIcon,
            customContent = startIconContent,
        ),
        onClick = onClick,
    )
}

/**
 * Renders the localized leading icon and label portion of a settings row.
 *
 * @param modifier Layout modifiers for the label group.
 * @param icon Optional leading vector icon.
 * @param text Localized setting label.
 * @param textSuffixContent Additional inline content after the label.
 * @param iconContent Custom leading content composed beside the optional vector icon.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsItemContent(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: UiText,
    textSuffixContent: (@Composable () -> Unit)? = null,
    iconContent: (@Composable () -> Unit)? = null,
) {
    SettingsItemContent(
        modifier = modifier,
        text = text.asString(),
        textSuffixContent = textSuffixContent,
        startIconContent = settingsStartIconContent(
            startIcon = icon,
            customContent = iconContent,
        ),
    )
}

private fun settingsStartIconContent(
    startIcon: ImageVector?,
    customContent: (@Composable () -> Unit)?,
): (@Composable () -> Unit)? {
    if (startIcon == null && customContent == null) return null
    return {
        startIcon?.let {
            Icon(
                modifier = Modifier.padding(horizontal = 8.dp),
                imageVector = it,
                contentDescription = null,
            )
        }
        customContent?.invoke()
    }
}
