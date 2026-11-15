package com.shifthackz.aisdv1.presentation.theme.global

/**
 * Holds the last committed theme without observable writes that would schedule another frame.
 * Only the retaining composable's SideEffect replaces this value after a visible composition.
 *
 * @param snapshot Last committed visible theme; initially absent until the first composition.
 * @author Dmitriy Moroz
 */
internal data class HomeRouteThemeState(var snapshot: HomeRouteThemeSnapshot? = null)
