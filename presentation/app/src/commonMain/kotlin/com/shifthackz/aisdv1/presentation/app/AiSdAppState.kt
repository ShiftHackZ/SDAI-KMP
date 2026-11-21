package com.shifthackz.aisdv1.presentation.app

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.core.navigation.AppRoute

/**
 * Snapshot rendered by the application shell; the router remains the navigation authority.
 * Previously visited home tabs stay composed underneath standalone overlay routes.
 *
 * @param route Current destination, including any overlay arguments.
 * @param homeRoute Home tab displayed beneath the current destination, if one has been visited.
 * @param renderedHomeRoutes Home tabs whose compositions must remain alive.
 * @param shouldShowHomeChrome Whether the destination belongs to the home scaffold.
 * @param canNavigateBack Whether Back can dismiss the drawer or leave the destination.
 * @param isForegroundGenerating Whether generation requires foreground screen protection.
 * @param shouldDimScreen Whether the persisted preference permits generation dimming.
 * @author Dmitriy Moroz
 */
internal data class AiSdAppState(
    val route: AppRoute = AppRoute.Splash,
    val homeRoute: AppRoute? = null,
    val renderedHomeRoutes: List<AppRoute> = emptyList(),
    val shouldShowHomeChrome: Boolean = false,
    val canNavigateBack: Boolean = false,
    val isForegroundGenerating: Boolean = false,
    val shouldDimScreen: Boolean = false,
) : MviState
