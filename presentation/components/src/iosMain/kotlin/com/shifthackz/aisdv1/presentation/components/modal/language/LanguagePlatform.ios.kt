package com.shifthackz.aisdv1.presentation.components.modal.language

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
