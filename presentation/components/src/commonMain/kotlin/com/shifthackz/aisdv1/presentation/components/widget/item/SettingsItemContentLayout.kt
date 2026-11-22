package com.shifthackz.aisdv1.presentation.components.widget.item

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import kotlinx.coroutines.delay

/**
 * Renders a settings row with optional loading, selection emphasis and trailing value controls.
 *
 * @param modifier Layout modifiers for the complete row or loading placeholder.
 * @param loading Replaces content with a shimmer while the setting loads.
 * @param enabled Whether row clicks are accepted.
 * @param selected Whether a selection border highlights the row.
 * @param text Setting label.
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
    text: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    selected: Boolean = false,
    animateBackground: Boolean = false,
    showChevron: Boolean = true,
    endValueText: String = "",
    endValueContent: (@Composable () -> Unit)? = null,
    textSuffixContent: (@Composable () -> Unit)? = null,
    startIconContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val primary = MaterialTheme.colorScheme.primaryContainer
    val secondary = MaterialTheme.colorScheme.tertiaryContainer
    var colorFrom by remember { mutableStateOf(primary) }
    var colorTo by remember { mutableStateOf(secondary) }
    val colorState = remember { Animatable(colorFrom) }
    if (animateBackground) {
        LaunchedEffect(Unit) {
            while (true) {
                colorState.animateTo(colorTo, animationSpec = tween(500))
                delay(500)
                val tmp = colorTo
                colorTo = colorFrom
                colorFrom = tmp
            }
        }
    }

    if (loading) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .defaultMinSize(minHeight = 50.dp)
                .shimmer(),
        )
    }
    AnimatedVisibility(
        visible = !loading,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val rowColor =
            if (animateBackground) colorState.value else MaterialTheme.colorScheme.surfaceTint.copy(
                alpha = 0.8f
            )
        PlatformSurface(
            color = rowColor,
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .defaultMinSize(minHeight = 50.dp)
                .clickable(enabled = enabled) { onClick() }
                .border(
                    width = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SettingsItemContent(
                    modifier = endValueContent
                        ?.let { Modifier.fillMaxWidth(0.8f) }
                        ?: Modifier,
                    text = text,
                    textSuffixContent = textSuffixContent,
                    startIconContent = startIconContent,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    endValueContent?.invoke() ?: run {
                        if (endValueText.isNotEmpty()) {
                            Text(
                                modifier = Modifier.fillMaxWidth(0.5f),
                                text = endValueText,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Right,
                            )
                        }
                        if (showChevron) {
                            Icon(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders a settings label with caller-provided leading and inline content.
 *
 * @param text Already localized label, truncated after two lines when necessary.
 * @param modifier Layout modifiers for the leading-content row.
 * @param textSuffixContent Additional inline content after the label.
 * @param startIconContent Optional leading icon or other compact content.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsItemContent(
    text: String,
    modifier: Modifier = Modifier,
    textSuffixContent: (@Composable () -> Unit)? = null,
    startIconContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        startIconContent?.invoke()
        Text(
            modifier = if (textSuffixContent == null) {
                Modifier
            } else {
                Modifier.weight(1f, fill = false)
            },
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        textSuffixContent?.invoke()
    }
}
