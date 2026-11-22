package com.shifthackz.aisdv1.presentation.settings.content

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
import com.shifthackz.aisdv1.presentation.settings.SettingsListItem
import com.shifthackz.aisdv1.presentation.settings.helpTitleKey
import com.shifthackz.aisdv1.presentation.settings.startIcon

/**
 * Adds localized, row-specific help while preserving lazy composition and each setting's short-tap action.
 * Existing inline warnings remain separate and readable without discovering long press.
 *
 * @param row Stable setting or section identity selecting the explanation.
 * @param isLoading Whether placeholders should remain free of interactive help.
 * @param modifier Placement of the complete Settings row.
 * @param content Original row, including its navigation or preference controls.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsItemHelp(
    row: SettingsListItem,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val titleKey = row.helpTitleKey()
    if (isLoading || titleKey == null) {
        Box(modifier) { content() }
    } else {
        val message = Localization.string("settings_help_" + row.name.lowercase())
        val betaHelp =
            if (row == SettingsListItem.BENCHMARK || row == SettingsListItem.BACKGROUND_GENERATION) {
                "\n\n" + Localization.string("settings_help_beta")
            } else ""
        ContextualHelp(
            title = Localization.string(titleKey).removeSuffix("*"),
            message = message + betaHelp,
            titleStartIcon = row.startIcon,
            modifier = modifier,
            content = content,
        )
    }
}
