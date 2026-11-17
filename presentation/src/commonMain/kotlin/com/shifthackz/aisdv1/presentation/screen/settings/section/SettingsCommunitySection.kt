@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.settings.section

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.generated.resources.Res
import com.shifthackz.aisdv1.presentation.generated.resources.sdai_logo
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsIntent
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsListItem
import com.shifthackz.aisdv1.presentation.screen.settings.model.text
import com.shifthackz.aisdv1.presentation.widget.icon.BrandIcons
import com.shifthackz.aisdv1.presentation.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.widget.item.SettingsItem
import org.jetbrains.compose.resources.painterResource

/**
 * Renders community links without observing unrelated preferences.
 *
 * @param row Logical list item to compose; siblings remain owned by the parent lazy list.
 * @param isLoading Whether preference placeholders are visible.
 * @param headerModifier Layout applied to the section header.
 * @param itemModifier Layout applied to each link row.
 * @param processIntent Receives link and version actions from the section.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsCommunitySection(
    row: SettingsListItem,
    isLoading: Boolean,
    headerModifier: Modifier,
    itemModifier: Modifier,
    processIntent: (SettingsIntent) -> Unit,
) {
    when (row) {
        SettingsListItem.COMMUNITY_HEADER -> {
            SettingsHeader(
                modifier = headerModifier,
                loading = isLoading,
                text = text("settings_header_community"),
            )
        }

        SettingsListItem.TELEGRAM -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = BrandIcons.Telegram,
                text = text("settings_item_telegram"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenTelegramCommunity) },
            )
        }

        SettingsListItem.DISCORD -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = BrandIcons.Discord,
                text = text("settings_item_discord"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenDiscordCommunity) },
            )
        }

        SettingsListItem.LINKED_IN -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = BrandIcons.LinkedIn,
                text = text("settings_item_linkedin"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenLinkedInCommunity) },
            )
        }

        SettingsListItem.PROJECT_WEBSITE -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIconContent = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.sdai_logo),
                            contentDescription = null,
                        )
                    }
                },
                text = text("settings_item_project_website"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenProjectWebsite) },
            )
        }

        SettingsListItem.DEVELOPER_WEBSITE -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = BrandIcons.Moroz,
                text = text("settings_item_developer_website"),
                onClick = { processIntent(SettingsIntent.LaunchUrl.OpenDeveloperWebsite) },
            )
        }

        else -> Unit
    }
}
