package com.shifthackz.aisdv1.presentation.components.modal.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

/**
 * Draws a bundled Android flag when the language has a matching drawable.
 *
 * @param languageCode Supported application localization code.
 * @param modifier Size and placement of the flag.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun LanguageFlagIcon(languageCode: String, modifier: Modifier) {
    Localization.getCountryFlagDrawableResId(languageCode)?.let { drawableResId ->
        Image(
            modifier = modifier.padding(horizontal = 8.dp),
            painter = painterResource(id = drawableResId),
            contentDescription = languageCode,
        )
    }
}
