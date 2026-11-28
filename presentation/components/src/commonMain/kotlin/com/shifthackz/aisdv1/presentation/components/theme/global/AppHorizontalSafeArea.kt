package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Reserves horizontal system controls and cutouts once inside each retained route. Native bar
 * routes also consume their own measured navigation edge without resizing neighboring route layers.
 *
 * @return Route placement with horizontal safe areas handled for all descendant content.
 * @author Dmitriy Moroz
 */
@Composable
expect fun Modifier.appHorizontalSafeArea(): Modifier
