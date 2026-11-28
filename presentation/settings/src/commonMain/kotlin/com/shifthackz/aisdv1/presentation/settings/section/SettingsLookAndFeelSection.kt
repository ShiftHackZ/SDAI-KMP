package com.shifthackz.aisdv1.presentation.settings.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.presentation.components.modal.grid.densityLabel
import com.shifthackz.aisdv1.presentation.components.widget.color.AccentColorSelector
import com.shifthackz.aisdv1.presentation.components.widget.color.DarkThemeColorSelector
import com.shifthackz.aisdv1.presentation.components.widget.item.GridIcon
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItemContent
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsToggleItem
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import com.shifthackz.aisdv1.presentation.settings.SettingsIntent
import com.shifthackz.aisdv1.presentation.settings.SettingsListItem
import com.shifthackz.aisdv1.presentation.settings.startIcon
import com.shifthackz.aisdv1.presentation.settings.text

/**
 * Renders appearance preferences from only their displayed values, so unrelated settings can skip.
 *
 * @param row Logical list item to compose; siblings remain owned by the parent lazy list.
 * @param isLoading Whether preference placeholders are visible.
 * @param galleryGrid Selected adaptive gallery density preset.
 * @param shouldUseSystemColorPalette Persisted dynamic color selection.
 * @param shouldUseSystemDarkTheme Whether system appearance determines dark mode.
 * @param isDarkTheme Persisted manual dark-mode selection.
 * @param darkThemeToken Selected static dark palette.
 * @param colorToken Selected accent color.
 * @param isDark Actual current dark appearance including system selection.
 * @param headerModifier Layout applied to the section header.
 * @param itemModifier Layout applied to each preference row.
 * @param processIntent Receives appearance and gallery preference actions.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsLookAndFeelSection(
    row: SettingsListItem,
    isLoading: Boolean,
    galleryGrid: Grid,
    shouldUseSystemColorPalette: Boolean,
    shouldUseSystemDarkTheme: Boolean,
    isDarkTheme: Boolean,
    darkThemeToken: DarkThemeToken,
    colorToken: ColorToken,
    isDark: Boolean,
    headerModifier: Modifier,
    itemModifier: Modifier,
    processIntent: (SettingsIntent) -> Unit,
) {
    when (row) {
        SettingsListItem.LOOK_HEADER -> {
            SettingsHeader(
                modifier = headerModifier,
                loading = isLoading,
                text = text("settings_header_look_and_feel"),
            )
        }

        SettingsListItem.LANGUAGE -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_lf_lang"),
                endValueText = text("language"),
                onClick = { processIntent(SettingsIntent.Action.PickLanguage) },
            )
        }

        SettingsListItem.GALLERY_GRID -> {
            SettingsItem(
                modifier = itemModifier,
                loading = isLoading,
                text = text("settings_item_lf_gallery_grid"),
                endValueText = galleryGrid.densityLabel().asUiText(),
                startIconContent = {
                    GridIcon(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        grid = galleryGrid,
                        color = LocalContentColor.current,
                    )
                },
                onClick = { processIntent(SettingsIntent.Action.GalleryGrid.Pick) },
            )
        }

        SettingsListItem.DYNAMIC_COLORS -> {
            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_lf_dynamic_colors"),
                isChecked = shouldUseSystemColorPalette,
                onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.DynamicColors(it)) },
            )
        }

        SettingsListItem.SYSTEM_DARK -> {
            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_lf_system_dark_theme"),
                isChecked = shouldUseSystemDarkTheme,
                onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.SystemDarkTheme(it)) },
            )
        }

        SettingsListItem.MANUAL_DARK -> {
            SettingsToggleItem(
                modifier = itemModifier,
                isLoading = isLoading,
                startIcon = row.startIcon,
                text = text("settings_item_lf_dark_theme"),
                isChecked = isDarkTheme,
                onCheckedChange = { processIntent(SettingsIntent.UpdateFlag.DarkTheme(it)) },
            )
        }

        SettingsListItem.DARK_PALETTE -> {
            PlatformSurface(
                modifier = itemModifier.defaultMinSize(minHeight = 50.dp),
                color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsItemContent(
                        text = text("settings_item_lf_dark_theme_color"),
                        icon = row.startIcon,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DarkThemeColorSelector(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        selectedToken = darkThemeToken,
                        colorToken = colorToken,
                        onSelected = { _, token ->
                            processIntent(SettingsIntent.NewDarkThemeToken(token))
                        },
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        SettingsListItem.ACCENT_COLOR -> {
            PlatformSurface(
                modifier = itemModifier.defaultMinSize(minHeight = 50.dp),
                color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsItemContent(
                        text = text("settings_item_lf_accent_color"),
                        icon = row.startIcon,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AccentColorSelector(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        isDark = isDark,
                        darkThemeToken = darkThemeToken,
                        selectedToken = colorToken,
                        onSelected = { _, token ->
                            processIntent(SettingsIntent.NewColorToken(token))
                        },
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        else -> Unit
    }
}
