package com.shifthackz.aisdv1.presentation.components.modal.grid

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.Grid

/**
 * Names the relative thumbnail density without promising a fixed number of adaptive columns.
 *
 * @receiver Persisted grid preset whose compact-window column count controls thumbnail density.
 * @return Density label in the current application language.
 * @author Dmitriy Moroz
 */
fun Grid.densityLabel(): String = Localization.string(
    when (this) {
        Grid.Fixed2 -> "gallery_grid_spacious"
        Grid.Fixed3 -> "gallery_grid_balanced"
        Grid.Fixed4 -> "gallery_grid_compact"
        Grid.Fixed5 -> "gallery_grid_dense"
    },
)
