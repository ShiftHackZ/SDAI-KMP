package com.shifthackz.aisdv1.presentation.components.widget.help

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.shifthackz.aisdv1.core.localization.Localization
import kotlinx.coroutines.launch

/**
 * Anchors persistent explanatory help without invoking the wrapped control's primary action.
 * Long press, pointer hover and a named accessibility action expose the same localized content.
 * The popup stays open until dismissed so longer translations can be read at the user's pace.
 *
 * @param title Localized name of the setting, section or badge.
 * @param message Natural-language explanation of the action or its enabled/disabled behavior.
 * @param modifier Placement and accessibility of the tooltip anchor.
 * @param titleStartIcon Optional decorative vector preceding the title at a smaller size than list-row icons.
 * @param content Original control retaining its normal short-tap behavior when help is long-press only.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextualHelp(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    titleStartIcon: ImageVector? = null,
    content: @Composable () -> Unit,
) {
    ContextualHelpAnchor(
        title = title,
        message = message,
        modifier = modifier,
        titleStartIcon = titleStartIcon,
        state = rememberTooltipState(isPersistent = true),
        content = content,
    )
}

/**
 * Shares tooltip rendering between Compose gestures and native controls that request help themselves.
 *
 * @param title Localized destination or setting name.
 * @param message Localized explanation shown in the persistent popup.
 * @param modifier Bounds and accessibility of the anchor.
 * @param titleStartIcon Optional decorative icon beside the help title.
 * @param state Popup lifecycle owned by the Compose wrapper or native-control adapter.
 * @param shouldHandleGestures Whether Compose recognizes long press and hover on this anchor.
 * @param shouldExposeAccessibilityAction Whether Compose supplies help or the native control owns it.
 * @param content Anchor content, which may be an empty native-control accessibility placeholder.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContextualHelpAnchor(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    titleStartIcon: ImageVector? = null,
    state: TooltipState = rememberTooltipState(isPersistent = true),
    shouldHandleGestures: Boolean = true,
    shouldExposeAccessibilityAction: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val showHelp = { scope.launch { state.show() }; Unit }
    val helpLabel = Localization.string("action_show_help", title)
    TooltipBox(
        modifier = modifier.semantics {
            if (shouldExposeAccessibilityAction) {
                customActions = listOf(CustomAccessibilityAction(helpLabel) { showHelp(); true })
            }
        },
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        state = state,
        enableUserInput = shouldHandleGestures,
        hasAction = true,
        onDismissRequest = { state.dismiss() },
        tooltip = {
            RichTooltip(
                colors = TooltipDefaults.richTooltipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        titleStartIcon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(title, modifier = Modifier.weight(1f))
                        IconButton(
                            // Keep the glyph near the edge without shrinking its touch target.
                            modifier = Modifier.offset(x = 12.dp),
                            onClick = { state.dismiss() },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = Localization.string("close"),
                            )
                        }
                    }
                },
            ) {
                Text(message)
            }
        },
    ) {
        content()
    }
}
