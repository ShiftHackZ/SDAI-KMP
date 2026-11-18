package com.shifthackz.aisdv1.presentation.components.widget.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString

/**
 * Resolves a localized section heading for the shared gradient-divider header.
 *
 * @param modifier Layout modifiers for the section header.
 * @param loading Replaces the title with a shimmer placeholder.
 * @param text Localized section title.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsHeader(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    text: UiText,
) {
    SettingsHeaderContent(
        modifier = modifier,
        loading = loading,
        text = text.asString(),
    )
}
