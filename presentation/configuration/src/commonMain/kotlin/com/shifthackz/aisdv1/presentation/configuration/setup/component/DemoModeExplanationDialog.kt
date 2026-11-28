package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryActionButton
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Explains sample-image behavior before each demo connection and requires two fresh acknowledgements.
 * Outside taps and Back do nothing. The cross explicitly closes the dialog and disables demo mode.
 *
 * @param state Checkbox state owned by the current setup modal.
 * @param processIntent Updates acknowledgements, disables demo, or proceeds with configuration.
 * @param modifier Placement of the dialog surface.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DemoModeExplanationDialog(
    state: ServerSetupState.Modal.DemoExplanation,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val titleStyle = MaterialTheme.typography.headlineSmall
    val titleLineHeight = with(LocalDensity.current) { titleStyle.lineHeight.toDp() }
    val closeVerticalOffset = (titleLineHeight - 48.dp) / 2
    BasicAlertDialog(
        modifier = modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 16.dp),
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        Localization.string("demo_explanation_title"),
                        modifier = Modifier.weight(1f),
                        style = titleStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(
                        modifier = Modifier.offset(x = 12.dp, y = closeVerticalOffset),
                        onClick = { processIntent(ServerSetupIntent.CloseDemoExplanation) },
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = Localization.string("demo_explanation_close"),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier.weight(1f, fill = false)
                        .verticalScrollbar(scrollState)
                        .verticalScroll(scrollState)
                        .padding(end = 8.dp),
                ) {
                    Text(
                        Localization.string("demo_explanation_body"),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    DemoAcknowledgementRow(
                        text = Localization.string("demo_explanation_understand"),
                        isChecked = state.hasUnderstoodDemo,
                        onCheckedChange = {
                            processIntent(
                                ServerSetupIntent.AcknowledgeDemoBehavior(
                                    it
                                )
                            )
                        },
                    )
                    DemoAcknowledgementRow(
                        text = Localization.string("demo_explanation_feedback"),
                        isChecked = state.hasAgreedToAccurateFeedback,
                        onCheckedChange = { processIntent(ServerSetupIntent.AgreeToDemoFeedback(it)) },
                    )
                }
                Spacer(Modifier.height(16.dp))
                PrimaryActionButton(
                    isEnabled = state.hasUnderstoodDemo && state.hasAgreedToAccurateFeedback,
                    onClick = { processIntent(ServerSetupIntent.ConfirmDemoExplanation) },
                ) {
                    Text(
                        Localization.string("demo_explanation_continue"),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/**
 * Makes the full wrapped acknowledgement label a single accessible checkbox target.
 *
 * @param text Localized acknowledgement, allowed to wrap at larger font sizes.
 * @param isChecked Current value owned by the dialog state.
 * @param onCheckedChange Reports a single requested state transition for row or checkbox taps.
 * @param modifier Placement of the complete checkbox row.
 * @author Dmitriy Moroz
 */
@Composable
private fun DemoAcknowledgementRow(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)
            .toggleable(value = isChecked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(checked = isChecked, onCheckedChange = null)
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
    }
}
