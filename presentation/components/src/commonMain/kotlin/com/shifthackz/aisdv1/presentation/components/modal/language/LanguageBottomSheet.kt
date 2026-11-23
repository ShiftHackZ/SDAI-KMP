package com.shifthackz.aisdv1.presentation.components.modal.language

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem

/**
 * Lists supported languages alphabetically by their native display names and highlights the active locale.
 * Selecting a row reports its language code and then requests dismissal.
 *
 * @param modifier Layout modifiers for the language list.
 * @param onLanguageSelected Applies/persists the selected language; the default invokes the platform adapter.
 * @param selectedLanguageCode Locale whose row receives selected styling.
 * @param onDismissRequest Closes the owning sheet after selection.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun LanguageBottomSheet(
    modifier: Modifier = Modifier,
    onLanguageSelected: (String) -> Unit = { applyAppLanguage(it) },
    onDismissRequest: () -> Unit = {},
    selectedLanguageCode: String = Localization.currentLanguageCode(),
) {
    val languages = remember { Localization.entries.sortedBy { it.name.lowercase() } }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        languages.forEach { language ->
            val locale = language.code
            SettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                selected = locale == selectedLanguageCode,
                text = language.name.asUiText(),
                showChevron = false,
                startIconContent = {
                    LanguageFlagIcon(locale)
                },
                onClick = {
                    onLanguageSelected(locale)
                    onDismissRequest()
                }
            )
        }
    }
}

/**
 * Applies a selected application locale through the current platform adapter.
 *
 * @param languageCode Supported localization entry code, used for platform locale integration.
 *
 * @author Dmitriy Moroz
 */
expect fun applyAppLanguage(languageCode: String)
