package com.shifthackz.aisdv1.presentation.components.modal.language

import androidx.compose.ui.Modifier

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.core.localization.Localization
import platform.Foundation.NSUserDefaults

/**
 * Updates shared localization and the AppleLanguages preference used by native iOS UI.
 *
 * @param languageCode Supported application localization code.
 *
 * @author Dmitriy Moroz
 */
actual fun applyAppLanguage(languageCode: String) {
    Localization.setLanguageCode(languageCode)
    NSUserDefaults.standardUserDefaults.setObject(
        listOf(languageCode),
        forKey = "AppleLanguages",
    )
    NSUserDefaults.standardUserDefaults.synchronize()
}

/**
 * Reserves no flag content on iOS; the language name remains the selection affordance.
 *
 * @param languageCode Supported application localization code.
 * @param modifier Layout modifiers accepted for platform API parity.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun LanguageFlagIcon(languageCode: String, modifier: Modifier) = Unit
