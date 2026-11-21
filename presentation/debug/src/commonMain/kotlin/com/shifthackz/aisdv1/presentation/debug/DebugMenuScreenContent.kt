@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.debug

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformSwitch
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
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
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
 * @param localDiffusionAllowCancel Saved legacy cancellation preference for local generation.
 * @param localDiffusionSchedulerThread Localized saved legacy scheduler selection.
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
    PlatformScaffold(
        canUnderlapToolbar = true,
        modifier = modifier,
        topBar = {
            PlatformTopAppBar(
                title = {
                    Text(
                        text = strings.title,
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                    )
                },
                navigationIcon = {
                    ToolbarBackButton(
                        onClick = { processAction(DebugMenuAction.NavigateBack) },
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = strings.backContentDescription,
                    )
                },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        ToolbarScrollObserver(scrollState.canScrollBackward)
        Column(
            modifier = Modifier
                .verticalScrollbar(scrollState, topInset = LocalToolbarContentInset.current)
                .verticalScroll(scrollState)
                .padding(top = LocalToolbarContentInset.current)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            val headerModifier = Modifier.padding(vertical = 16.dp)
            val itemModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)

            DebugMenuHeader(
                modifier = headerModifier,
                text = strings.mainSection,
                helpMessage = Localization.string("debug_help_section_logs"),
            )
            DebugMenuItem(
                modifier = itemModifier,
                startIcon = Icons.AutoMirrored.Filled.TextSnippet,
                text = strings.actionLogger,
                helpMessage = Localization.string("debug_help_logs_view"),
                onClick = { processAction(DebugMenuAction.ViewLogs) },
            )
            DebugMenuItem(
                modifier = itemModifier,
                startIcon = Icons.Default.CleaningServices,
                text = strings.actionLoggerClear,
                helpMessage = Localization.string("debug_help_logs_clear"),
                onClick = { processAction(DebugMenuAction.ClearLogs) },
            )

            if (state.isUiDiagnosticsSupported) {
                DebugMenuHeader(
                    text = strings.diagnosticSection, modifier = headerModifier,
                    helpMessage = Localization.string("debug_help_section_diagnostics")
                )
                ContextualHelp(
                    title = strings.diagnosticCapture,
                    message = Localization.string("debug_help_capture"),
                    modifier = itemModifier,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                        PlatformSwitch(
                            isChecked = state.isUiDiagnosticsCapturing,
                            onCheckedChange = null
                        )
                    }
                }
                DebugMenuItem(
                    text = strings.diagnosticReset,
                    helpMessage = Localization.string("debug_help_capture_reset"),
                    modifier = itemModifier,
                    onClick = { processAction(DebugMenuAction.ResetUiCapture) })
                DebugMenuItem(
                    text = strings.diagnosticShare,
                    helpMessage = Localization.string("debug_help_capture_share"),
                    modifier = itemModifier,
                    onClick = { processAction(DebugMenuAction.ShareUiCapture) })
            }

            if (state.showWorkManagerSection) {
                DebugMenuHeader(
                    modifier = headerModifier,
                    text = strings.workManagerSection,
                    helpMessage = Localization.string("debug_help_section_work"),
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.Refresh,
                    text = strings.actionWorkRestartTxt2Img,
                    helpMessage = Localization.string("debug_help_retry_txt"),
                    onClick = { processAction(DebugMenuAction.RestartTxt2Img) },
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.Refresh,
                    text = strings.actionWorkRestartImg2Img,
                    helpMessage = Localization.string("debug_help_retry_img"),
                    onClick = { processAction(DebugMenuAction.RestartImg2Img) },
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.Cancel,
                    text = strings.actionWorkCancelAll,
                    helpMessage = Localization.string("debug_help_cancel_work"),
                    onClick = { processAction(DebugMenuAction.CancelAllWork) },
                )
            }

            if (state.showLocalDiffusionSection) {
                DebugMenuHeader(
                    modifier = headerModifier,
                    text = strings.localDiffusionSection,
                    helpMessage = Localization.string("debug_help_section_local"),
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.CancelScheduleSend,
                    text = strings.actionLocalDiffusionAllowCancel,
                    helpMessage = Localization.string("debug_help_allow_cancel"),
                    onClick = { processAction(DebugMenuAction.ToggleLocalDiffusionCancel) },
                    showChevron = false,
                    endValueContent = {
                        PlatformSwitch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            isChecked = state.localDiffusionAllowCancel,
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
                    helpMessage = Localization.string("debug_help_scheduler"),
                    onClick = { processAction(DebugMenuAction.RequestLocalDiffusionScheduler) },
                    endValueText = state.localDiffusionSchedulerThread,
                )
            }

            if (state.showQualityAssuranceSection) {
                DebugMenuHeader(
                    modifier = headerModifier,
                    text = strings.qualityAssuranceSection,
                    helpMessage = Localization.string("debug_help_section_qa"),
                )
                DebugMenuItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.SettingsEthernet,
                    text = strings.actionBadBase64,
                    helpMessage = Localization.string("debug_help_bad_image"),
                    onClick = { processAction(DebugMenuAction.InsertBadBase64) },
                )
            }
        }
    }
}


/**
 * Keeps developer help on the whole row while preserving its short-tap action.
 *
 * @param text Localized action title shared by the row and tooltip.
 * @param helpMessage Localized description including any side effects.
 * @param modifier Placement of the complete row and help anchor.
 * @param startIcon Optional action icon, also shown in the tooltip title.
 * @param showChevron Whether this action displays a navigation indicator.
 * @param endValueText Current preference value displayed after the title.
 * @param endValueContent Optional trailing control replacing the value and chevron.
 * @param onClick Primary action performed only on a short tap.
 * @author Dmitriy Moroz
 */
@Composable
private fun DebugMenuItem(
    text: String,
    helpMessage: String,
    modifier: Modifier = Modifier,
    startIcon: ImageVector? = null,
    showChevron: Boolean = true,
    endValueText: String = "",
    endValueContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    ContextualHelp(
        title = text,
        message = helpMessage,
        titleStartIcon = startIcon,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
}

/**
 * Explains a developer-tool group without triggering any operation in that group.
 *
 * @param text Localized section title.
 * @param helpMessage Localized purpose of this group of tools.
 * @param modifier Placement of the heading and its help anchor.
 * @author Dmitriy Moroz
 */
@Composable
private fun DebugMenuHeader(
    text: String,
    helpMessage: String,
    modifier: Modifier = Modifier,
) {
    ContextualHelp(title = text, message = helpMessage, modifier = modifier.fillMaxWidth()) {
        SettingsHeaderContent(text = text)
    }
}
