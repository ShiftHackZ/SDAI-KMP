@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CancelScheduleSend
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsHeaderContent
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar

/**
 * Already localized developer-menu labels used by its stateless renderer.
 *
 * @param diagnosticSection Heading for bounded UI capture controls.
 * @param diagnosticCapture Label for the explicit capture switch.
 * @param diagnosticReset Action label for clearing captured diagnostics.
 * @param diagnosticShare Action label for exporting the diagnostic snapshot.
 * @param title Developer-menu toolbar title.
 * @param mainSection Heading for log operations.
 * @param actionLogger Label for opening the log viewer.
 * @param actionLoggerClear Label for deleting cached logs.
 * @param workManagerSection Heading for background-generation controls.
 * @param actionWorkRestartTxt2Img Label for resubmitting captured text-generation work.
 * @param actionWorkRestartImg2Img Label for resubmitting captured image-generation work.
 * @param actionWorkCancelAll Label for cancelling scheduled generation work.
 * @param localDiffusionSection Heading for local ONNX debug preferences.
 * @param actionLocalDiffusionAllowCancel Label for the local cancellation preference.
 * @param actionLocalDiffusionScheduler Label for scheduler selection.
 * @param qualityAssuranceSection Heading for malformed-data diagnostics.
 * @param actionBadBase64 Label for deliberately inserting invalid image data.
 * @param backContentDescription Accessible label for returning to the preceding route.
 *
 * @author Dmitriy Moroz
 */
data class DebugMenuScreenStrings(
    val diagnosticSection: String = "",
    val diagnosticCapture: String = "",
    val diagnosticReset: String = "",
    val diagnosticShare: String = "",
    val title: String,
    val mainSection: String,
    val actionLogger: String,
    val actionLoggerClear: String,
    val workManagerSection: String,
    val actionWorkRestartTxt2Img: String,
    val actionWorkRestartImg2Img: String,
    val actionWorkCancelAll: String,
    val localDiffusionSection: String,
    val actionLocalDiffusionAllowCancel: String,
    val actionLocalDiffusionScheduler: String,
    val qualityAssuranceSection: String,
    val actionBadBase64: String,
    val backContentDescription: String,
)

/**
 * Rendered debug preferences and platform capability visibility.
 *
 * @param isUiDiagnosticsSupported Whether bounded UI capture is available in this build.
 * @param isUiDiagnosticsCapturing Whether diagnostic capture is currently active.
 * @param localDiffusionAllowCancel Current local ONNX cancellation preference.
 * @param localDiffusionSchedulerThread Localized name of the current scheduler.
 * @param showWorkManagerSection Whether background-work controls are supported.
 * @param showLocalDiffusionSection Whether local ONNX preferences are supported.
 * @param showQualityAssuranceSection Whether malformed-data insertion controls are visible.
 *
 * @author Dmitriy Moroz
 */
data class DebugMenuScreenContentState(
    val isUiDiagnosticsSupported: Boolean = false,
    val isUiDiagnosticsCapturing: Boolean = false,
    val localDiffusionAllowCancel: Boolean = false,
    val localDiffusionSchedulerThread: String = "",
    val showWorkManagerSection: Boolean = true,
    val showLocalDiffusionSection: Boolean = true,
    val showQualityAssuranceSection: Boolean = true,
)

/**
 * User requests emitted by the stateless developer-menu renderer.
 *
 * @author Dmitriy Moroz
 */
sealed interface DebugMenuAction {

    data object ShareUiCapture : DebugMenuAction

    data object ResetUiCapture : DebugMenuAction

    data object StopUiCapture : DebugMenuAction

    data object StartUiCapture : DebugMenuAction

    data object NavigateBack : DebugMenuAction

    data object ViewLogs : DebugMenuAction

    data object ClearLogs : DebugMenuAction

    data object RestartTxt2Img : DebugMenuAction

    data object RestartImg2Img : DebugMenuAction

    data object CancelAllWork : DebugMenuAction

    data object ToggleLocalDiffusionCancel : DebugMenuAction

    data object RequestLocalDiffusionScheduler : DebugMenuAction

    data object InsertBadBase64 : DebugMenuAction
}

/**
 * Renders capability-filtered developer controls from caller-owned state.
 *
 * @param strings Localized labels for sections, actions and accessibility.
 * @param state Current preferences and supported section visibility.
 * @param processAction Requests state changes, platform operations or navigation.
 * @param modifier Layout modifiers for the developer-menu scaffold.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun DebugMenuScreenContent(
    strings: DebugMenuScreenStrings,
    state: DebugMenuScreenContentState,
    processAction: (DebugMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = strings.title,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { processAction(DebugMenuAction.NavigateBack) },
                        content = {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = strings.backContentDescription,
                            )
                        },
                    )
                },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScrollbar(scrollState)
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            val headerModifier = Modifier.padding(vertical = 16.dp)
            val itemModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)

            SettingsHeaderContent(
                modifier = headerModifier,
                text = strings.mainSection,
            )
            DebugMenuItem(
                modifier = itemModifier,
                startIcon = Icons.AutoMirrored.Filled.TextSnippet,
                text = strings.actionLogger,
                onClick = { processAction(DebugMenuAction.ViewLogs) },
            )
            DebugMenuItem(
                modifier = itemModifier,
                startIcon = Icons.Default.CleaningServices,
                text = strings.actionLoggerClear,
                onClick = { processAction(DebugMenuAction.ClearLogs) },
            )

            if (state.isUiDiagnosticsSupported) {
                SettingsHeaderContent(text = strings.diagnosticSection, modifier = headerModifier)
                Row(
                    modifier = itemModifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f))
                        .defaultMinSize(minHeight = 50.dp)
                        .toggleable(
                            value = state.isUiDiagnosticsCapturing,
                            role = Role.Switch,
                            onValueChange = { shouldCapture ->
                                processAction(if (shouldCapture) DebugMenuAction.StartUiCapture else DebugMenuAction.StopUiCapture)
                            },
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = strings.diagnosticCapture,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = state.isUiDiagnosticsCapturing, onCheckedChange = null)
                }
                DebugMenuItem(
                    text = strings.diagnosticReset,
                    modifier = itemModifier,
                    onClick = { processAction(DebugMenuAction.ResetUiCapture) })
                DebugMenuItem(
                    text = strings.diagnosticShare,
                    modifier = itemModifier,
                    onClick = { processAction(DebugMenuAction.ShareUiCapture) })
            }

            if (state.showWorkManagerSection) {
                SettingsHeaderContent(
                    modifier = headerModifier,
                    text = strings.workManagerSection,
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.Refresh,
                    text = strings.actionWorkRestartTxt2Img,
                    onClick = { processAction(DebugMenuAction.RestartTxt2Img) },
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.Refresh,
                    text = strings.actionWorkRestartImg2Img,
                    onClick = { processAction(DebugMenuAction.RestartImg2Img) },
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.Cancel,
                    text = strings.actionWorkCancelAll,
                    onClick = { processAction(DebugMenuAction.CancelAllWork) },
                )
            }

            if (state.showLocalDiffusionSection) {
                SettingsHeaderContent(
                    modifier = headerModifier,
                    text = strings.localDiffusionSection,
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.CancelScheduleSend,
                    text = strings.actionLocalDiffusionAllowCancel,
                    onClick = { processAction(DebugMenuAction.ToggleLocalDiffusionCancel) },
                    showChevron = false,
                    endValueContent = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = state.localDiffusionAllowCancel,
                            onCheckedChange = {
                                processAction(DebugMenuAction.ToggleLocalDiffusionCancel)
                            },
                        )
                    },
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.Construction,
                    text = strings.actionLocalDiffusionScheduler,
                    onClick = { processAction(DebugMenuAction.RequestLocalDiffusionScheduler) },
                    endValueText = state.localDiffusionSchedulerThread,
                )
            }

            if (state.showQualityAssuranceSection) {
                SettingsHeaderContent(
                    modifier = headerModifier,
                    text = strings.qualityAssuranceSection,
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.SettingsEthernet,
                    text = strings.actionBadBase64,
                    onClick = { processAction(DebugMenuAction.InsertBadBase64) },
                )
            }
        }
    }
}


@Composable
private fun DebugMenuItem(
    text: String,
    modifier: Modifier = Modifier,
    startIcon: ImageVector? = null,
    showChevron: Boolean = true,
    endValueText: String = "",
    endValueContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f))
            .defaultMinSize(minHeight = 50.dp)
            .clickable { onClick() }
            .border(
                width = 2.dp,
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = endValueContent
                ?.let { Modifier.fillMaxWidth(0.8f) }
                ?: Modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            startIcon?.let {
                Icon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    imageVector = it,
                    contentDescription = null,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
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
                        tint = LocalContentColor.current,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}
