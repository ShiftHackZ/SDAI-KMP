package com.shifthackz.aisdv1.presentation.components.modal.language

import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Updates the shared localization source for subsequent compositions.
 *
 * @param languageCode Supported application localization code.
 *
 * @author Dmitriy Moroz
 */
actual fun applyAppLanguage(languageCode: String) {
    Localization.setLanguageCode(languageCode)
}
