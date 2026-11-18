package com.shifthackz.aisdv1.presentation.generation.model

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asUiText

/** Validates the v3 protocol's pixel alignment before the normal dimension range validator runs.
 * @author Dmitriy Moroz */
internal fun String.easyDiffusionSizeError(): UiText? =
    Localization.string("error_size_multiple", 8).asUiText()
        .takeIf { (toIntOrNull() ?: 0) % 8 != 0 }
