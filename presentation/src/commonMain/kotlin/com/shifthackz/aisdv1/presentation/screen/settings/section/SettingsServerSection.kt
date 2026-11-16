@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.settings.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.math.roundTo
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsIntent
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsListItem
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsState
import com.shifthackz.aisdv1.presentation.screen.settings.model.label
import com.shifthackz.aisdv1.presentation.screen.settings.model.shortTitle
import com.shifthackz.aisdv1.presentation.screen.settings.model.text
import com.shifthackz.aisdv1.presentation.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.widget.item.SettingsItem
import com.shifthackz.aisdv1.presentation.widget.item.SettingsToggleItem
import com.shifthackz.aisdv1.presentation.widget.source.BetaBadge

/**
 * Renders one server or application preference row selected by the Settings lazy list.
 * Usage summaries and actions retain their existing state and navigation contracts.
 *
 * @param row Logical list item to compose; siblings remain owned by the parent lazy list.
 * @param state Settings state that provides provider, usage summary, and loading values.
 * @param headerModifier Modifier shared by Settings section headers.
 * @param itemModifier Modifier shared by Settings rows.
 * @param warningModifier Modifier used by explanatory warnings below preference rows.
 * @param backgroundGenerationWarningKey Localization key for the current warning copy.
 * @param processIntent Intent sink used to route row actions back to SettingsViewModel.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsServerSection(
    row: SettingsListItem,
    state: SettingsState,
    headerModifier: Modifier,
    itemModifier: Modifier,
    warningModifier: Modifier,
    backgroundGenerationWarningKey: String,
    processIntent: (SettingsIntent) -> Unit,
) {
    val isLoading = state.loading
    val isBackgroundGenerationEnabled = state.backgroundGeneration
    val isDeveloperModeEnabled = state.developerMode
    when (row) {
        SettingsListItem.SERVER_HEADER -> {
            SettingsHeader(
                modifier = headerModifier,
                loading = isLoading,
                text = text("settings_header_server"),
            )
        }

        SettingsListItem.CONFIGURATION -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = Icons.Default.SettingsEthernet,
                text = text("settings_item_config"),
                endValueText = state.serverSource.shortTitle().asUiText(),
                onClick = { processIntent(SettingsIntent.NavigateConfiguration) },
            )
        }

        SettingsListItem.BENCHMARK -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = Icons.Default.Speed,
                text = text("settings_item_benchmark"),
                textSuffixContent = { BetaBadge() },
                onClick = { processIntent(SettingsIntent.NavigateBenchmark) },
            )
        }

        SettingsListItem.CREDITS -> {

            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                enabled = false,
                startIcon = Icons.Default.Circle,
                text = text("settings_item_stability_ai_credits"),
                endValueText = state.stabilityAiCredits.roundTo(4).toString().asUiText(),
            )

        }

        SettingsListItem.CONNECTION_TIMEOUT -> {

            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = Icons.Default.Timer,
                text = text("settings_connection_timeout"),
                endValueText = state.connectionTimeout.label().asUiText(),
                onClick = { processIntent(SettingsIntent.PickConnectionTimeout) },
            )

        }

        SettingsListItem.GENERATION_TIMEOUT -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = Icons.Default.Timer,
                text = text("settings_generation_timeout"),
                endValueText = state.generationRequestTimeout.label().asUiText(),
                onClick = { processIntent(SettingsIntent.PickGenerationTimeout) },
            )
        }

        SettingsListItem.MODEL -> {

            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = Icons.Default.AutoFixNormal,
                text = text("settings_item_sd_model"),
                endValueText = state.sdModelSelected.asUiText(),
                onClick = { processIntent(SettingsIntent.SdModel.OpenChooser) },
            )

        }

        SettingsListItem.NNAPI -> {
            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = Icons.Default.AccountTree,
                text = (Localization.string("settings_item_local_nnapi") +
                        if (state.localUseNNAPI) "*" else "").asUiText(),
                isChecked = state.localUseNNAPI,
                onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.NNAPI(it)) },
            )
        }

        SettingsListItem.NNAPI_WARNING -> {

            Text(
                modifier = warningModifier,
                text = Localization.string("settings_item_local_nnapi_warning"),
                style = MaterialTheme.typography.labelMedium,
            )

        }

        SettingsListItem.BACKGROUND_GENERATION -> {
            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = Icons.Default.MiscellaneousServices,
                text = text("settings_item_background_generation"),
                textSuffixContent = { BetaBadge() },
                isChecked = isBackgroundGenerationEnabled,
                onCheckedChange = {
                    processIntent(
                        SettingsIntent.UpdateFlag.BackgroundGeneration(
                            it
                        )
                    )
                },
            )
        }

        SettingsListItem.BACKGROUND_WARNING -> {

            Text(
                modifier = warningModifier,
                text = Localization.string(backgroundGenerationWarningKey),
                style = MaterialTheme.typography.labelMedium,
            )

        }

        SettingsListItem.DEBUG_MENU -> {

            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = Icons.Default.DeveloperMode,
                text = text("title_debug_menu"),
                onClick = { processIntent(SettingsIntent.NavigateDeveloperMode) },
            )

        }

        SettingsListItem.APP_HEADER -> {
            SettingsHeader(
                modifier = headerModifier,
                loading = isLoading,
                text = text("settings_header_app"),
            )
        }

        SettingsListItem.MONITOR_CONNECTION -> {

            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = Icons.Default.Refresh,
                text = text("settings_item_monitor_connection"),
                isChecked = state.monitorConnectivity,
                onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.MonitorConnection(it)) },
            )

        }

        SettingsListItem.AUTO_SAVE -> {
            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = Icons.Default.Save,
                text = (Localization.string("settings_item_auto_save") +
                        if (isBackgroundGenerationEnabled) "*" else "").asUiText(),
                isChecked = state.autoSaveAiResults,
                onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.AutoSaveResult(it)) },
            )
        }

        SettingsListItem.AUTO_SAVE_WARNING -> {

            Text(
                modifier = warningModifier,
                text = Localization.string("settings_item_auto_save_warning"),
                style = MaterialTheme.typography.labelMedium,
            )

        }

        SettingsListItem.MEDIA_STORE -> {
            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = Icons.Default.Folder,
                text = text("settings_item_auto_save_media_store"),
                isChecked = state.saveToMediaStore,
                onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.SaveToMediaStore(it)) },
            )
        }

        SettingsListItem.TAGGED_INPUT -> {
            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = Icons.Default.Tag,
                text = text("settings_item_tagged_input"),
                isChecked = state.formPromptTaggedInput,
                onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.TaggedInput(it)) },
            )
        }

        SettingsListItem.ADVANCED_DEFAULT -> {

            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = Icons.Default.DynamicForm,
                text = text("settings_item_advanced_form_default"),
                isChecked = state.formAdvancedOptionsAlwaysShow,
                onCheckedChange = {
                    processIntent(
                        SettingsIntent.UpdateFlag.AdvancedFormVisibility(
                            it
                        )
                    )
                },
            )

        }

        SettingsListItem.STORAGE_USAGE -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = Icons.Default.Storage,
                text = text("settings_item_storage_usage"),
                endValueText = state.storageUsageBytes
                    .takeIf { it > 0L }
                    ?.formatSettingsUsageBytes()
                    .orEmpty()
                    .asUiText(),
                onClick = {
                    processIntent(SettingsIntent.NavigateStorageUsage)
                },
            )
        }

        SettingsListItem.NETWORK_USAGE -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = Icons.Default.DataUsage,
                text = text("settings_item_network_usage"),
                endValueText = state.networkUsageBytes
                    .takeIf { it > 0L }
                    ?.formatSettingsUsageBytes()
                    .orEmpty()
                    .asUiText(),
                onClick = {
                    processIntent(SettingsIntent.NavigateNetworkUsage)
                },
            )
        }

        else -> Unit
    }
}

/**
 * Compact byte formatter for Settings list row summaries.
 *
 * @receiver Raw byte count to render as a short Settings list value.
 *
 * @author Dmitriy Moroz
 */
private fun Long.formatSettingsUsageBytes(): String {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var unitIndex = 0
    var display = toDouble()
    while (display >= 1000.0 && unitIndex < units.lastIndex) {
        display /= 1000.0
        unitIndex++
    }
    return if (unitIndex == 0 || display >= 100.0) {
        "${display.toLong()} ${units[unitIndex]}"
    } else {
        "${(display * 10.0).toInt() / 10.0} ${units[unitIndex]}"
    }
}
