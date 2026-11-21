package com.shifthackz.aisdv1.presentation.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.presentation.components.widget.icon.BrandIcons

/**
 * Shares each setting's vector icon between its list row and explanatory tooltip title.
 * Section headings and rows with custom image content retain their own rendering and return null.
 *
 * @receiver Stable Settings row identity, independent of preference values.
 * @return Decorative icon paired with the setting label, or null when no vector is used.
 * @author Dmitriy Moroz
 */
internal val SettingsListItem.startIcon: ImageVector?
    get() = when (this) {
        SettingsListItem.CONFIGURATION -> Icons.Default.SettingsEthernet
        SettingsListItem.BACKUP -> Icons.Default.Backup
        SettingsListItem.BENCHMARK -> Icons.Default.Speed
        SettingsListItem.CREDITS -> Icons.Default.Circle
        SettingsListItem.CONNECTION_TIMEOUT -> Icons.Default.Timer
        SettingsListItem.GENERATION_TIMEOUT -> Icons.Default.Timer
        SettingsListItem.MODEL -> Icons.Default.AutoFixNormal
        SettingsListItem.NNAPI -> Icons.Default.AccountTree
        SettingsListItem.BACKGROUND_GENERATION -> Icons.Default.MiscellaneousServices
        SettingsListItem.DEBUG_MENU -> Icons.Default.DeveloperMode
        SettingsListItem.PERFORMANCE_OVERLAY -> Icons.Default.Speed
        SettingsListItem.DIM_SCREEN -> Icons.Default.BrightnessLow
        SettingsListItem.MONITOR_CONNECTION -> Icons.Default.Refresh
        SettingsListItem.AUTO_SAVE -> Icons.Default.Save
        SettingsListItem.MEDIA_STORE -> Icons.Default.Folder
        SettingsListItem.TAGGED_INPUT -> Icons.Default.Tag
        SettingsListItem.ADVANCED_DEFAULT -> Icons.Default.DynamicForm
        SettingsListItem.STORAGE_USAGE -> Icons.Default.Storage
        SettingsListItem.NETWORK_USAGE -> Icons.Default.DataUsage
        SettingsListItem.LANGUAGE -> Icons.Default.Translate
        SettingsListItem.DYNAMIC_COLORS -> Icons.Default.Save
        SettingsListItem.SYSTEM_DARK -> Icons.Default.InvertColors
        SettingsListItem.MANUAL_DARK -> Icons.Default.DarkMode
        SettingsListItem.DARK_PALETTE -> Icons.Default.FormatColorFill
        SettingsListItem.ACCENT_COLOR -> Icons.Default.ColorLens
        SettingsListItem.TELEGRAM -> BrandIcons.Telegram
        SettingsListItem.DISCORD -> BrandIcons.Discord
        SettingsListItem.LINKED_IN -> BrandIcons.LinkedIn
        SettingsListItem.DEVELOPER_WEBSITE -> BrandIcons.Moroz
        SettingsListItem.DONATE -> Icons.Default.MonetizationOn
        SettingsListItem.ONBOARDING -> Icons.Default.AllInclusive
        SettingsListItem.REPORT_PROBLEM -> Icons.Default.Report
        SettingsListItem.PRIVACY -> Icons.Default.Policy
        SettingsListItem.LICENSE -> Icons.Default.Gavel
        SettingsListItem.SOURCE_CODE -> Icons.Default.Code
        SettingsListItem.SERVER_HEADER,
        SettingsListItem.NNAPI_WARNING,
        SettingsListItem.BACKGROUND_WARNING,
        SettingsListItem.APP_HEADER,
        SettingsListItem.DIM_SCREEN_WARNING,
        SettingsListItem.AUTO_SAVE_WARNING,
        SettingsListItem.LOOK_HEADER,
        SettingsListItem.GALLERY_GRID,
        SettingsListItem.COMMUNITY_HEADER,
        SettingsListItem.PROJECT_WEBSITE,
        SettingsListItem.INFO_HEADER,
        SettingsListItem.VERSION -> null
    }
