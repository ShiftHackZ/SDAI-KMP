package com.shifthackz.aisdv1.presentation.components.extensions

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asUiText

/**
 * Resolves a boolean to the current localized Yes/No UI text.
 *
 * @receiver Value whose affirmative/negative label is requested.
 *
 * @return Localized label for display.
 *
 * @author Dmitriy Moroz
 */
fun Boolean.mapToUi(): UiText = Localization
    .string(if (this) "yes" else "no")
    .asUiText()
