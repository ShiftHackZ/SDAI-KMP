package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether this route reserves the adaptive navigation bar's measured edges. Standalone routes use
 * system safe areas, so hiding another route's bar cannot change their viewport during navigation.
 *
 * @author Dmitriy Moroz
 */
val LocalNavigationBarInsetsEnabled = staticCompositionLocalOf { false }

/**
 * Whether this route owns native navigation, independently of temporary transition input blocking.
 * The incoming route takes ownership before moving onscreen; retained previews never replace it.
 *
 * @author Dmitriy Moroz
 */
val LocalNavigationBarOwnerEnabled = staticCompositionLocalOf { true }
