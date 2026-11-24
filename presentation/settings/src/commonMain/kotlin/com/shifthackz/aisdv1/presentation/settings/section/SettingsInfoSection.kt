package com.shifthackz.aisdv1.presentation.settings.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.settings.SettingsIntent
import com.shifthackz.aisdv1.presentation.settings.startIcon
import com.shifthackz.aisdv1.presentation.settings.SettingsListItem
import com.shifthackz.aisdv1.presentation.settings.text
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem

/**
 * Renders application information links without observing unrelated preferences.
 *
 * @param row Logical list item to compose; siblings remain owned by the parent lazy list.
 * @param isLoading Whether preference placeholders are visible.
 * @param headerModifier Layout applied to the section header.
 * @param itemModifier Layout applied to each link row.
 * @param processIntent Receives link and version actions from the section.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsInfoSection(
    row: SettingsListItem,
    isLoading: Boolean,
    headerModifier: Modifier,
    itemModifier: Modifier,
    processIntent: (SettingsIntent) -> Unit,
) {
    when (row) {
        SettingsListItem.INFO_HEADER -> {
            SettingsHeader(
                modifier = headerModifier,
                loading = isLoading,
                text = text("settings_header_info"),
            )
        }

        SettingsListItem.DONATE -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_donate"),
                onClick = { processIntent(SettingsIntent.Action.Donate) },
            )
        }

        SettingsListItem.ONBOARDING -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_on_boarding"),
                onClick = { processIntent(SettingsIntent.Action.OnBoarding) },
            )
        }

        SettingsListItem.FAQ -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = row.startIcon,
                text = text("faq_title"),
                onClick = { processIntent(SettingsIntent.NavigateFaq) },
            )
        }

        SettingsListItem.REPORT_PROBLEM -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_report_problem"),
                onClick = { processIntent(SettingsIntent.Action.ReportProblem) },
            )
        }

        SettingsListItem.PRIVACY -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_policy"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenPolicy) },
            )
        }

        SettingsListItem.LICENSE -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_license"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenLicense) },
            )
        }

        SettingsListItem.SOURCE_CODE -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_source"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenSourceCode) },
            )
        }

        else -> Unit
    }
}
