package com.shifthackz.aisdv1.presentation.components.widget.item

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * Shared spacing for main Settings and its account-management pages.
 *
 * @author Dmitriy Moroz
 */
object SettingsListDefaults {
    /**
     * Space around a section heading before the next row supplies its own top gap.
     *
     * @author Dmitriy Moroz
     */
    val headerPadding = PaddingValues(top = 28.dp, bottom = 8.dp)

    /**
     * Space preceding each preference or navigation row.
     *
     * @author Dmitriy Moroz
     */
    val itemPadding = PaddingValues(top = 8.dp)

    /**
     * Aligns supporting text with the existing Settings warnings beneath a row.
     *
     * @author Dmitriy Moroz
     */
    val supportingTextPadding = PaddingValues(top = 4.dp, start = 4.dp)
}
