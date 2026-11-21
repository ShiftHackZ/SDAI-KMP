package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** Shared route transition, inherited by route-specific Back handlers. @author Dmitriy Moroz */
val LocalBackTransitionState = staticCompositionLocalOf<BackTransitionState?> { null }

/** Whether this visible route owns navigation input rather than serving as a preview. @author Dmitriy Moroz */
val LocalNavigationInputEnabled = staticCompositionLocalOf { true }

/**
 * Handles platform Back with screen-owned permission and optional cancellable visual progress.
 * Android keeps OS Back input; iOS uses the host's native leading-edge navigation events.
 *
 * @param isEnabled Whether this handler owns Back, including a no-op blocking action.
 * @param onBack Existing screen action invoked once after completion, never during preview.
 * @param canStartGesture Whether iOS may begin a gesture; Android Back remains independently enabled.
 * @param transitionState Explicit local-step animation state, or the enclosing route transition.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformBackHandler(
    isEnabled: Boolean,
    onBack: () -> Unit,
    canStartGesture: Boolean = isEnabled,
    transitionState: BackTransitionState? = null,
)
