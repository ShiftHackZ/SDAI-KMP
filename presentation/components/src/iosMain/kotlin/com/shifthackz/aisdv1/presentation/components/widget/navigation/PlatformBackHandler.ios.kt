package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility

/**
 * Connects the existing UIKit edge recognizer to a foreground screen's Back policy.
 * Rechecks permission after settling so a new modal or operation cannot be bypassed.
 *
 * @param isEnabled Whether this screen owns Back.
 * @param onBack Screen action committed after a successful gesture.
 * @param canStartGesture Whether the current screen state permits leaving.
 * @param transitionState Explicit local-step progress, otherwise inherited route progress.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformBackHandler(
    isEnabled: Boolean,
    onBack: () -> Unit,
    canStartGesture: Boolean,
    transitionState: BackTransitionState?,
) {
    val animation = transitionState ?: LocalBackTransitionState.current
    val isAttached = remember { mutableStateOf(true) }
    val canHandleBack by rememberUpdatedState(isEnabled && canStartGesture && LocalMviVisibility.current && LocalNavigationInputEnabled.current)
    val currentOnBack by rememberUpdatedState(onBack)
    val state = rememberNavigationEventState(NavigationEventInfo.None)
    DisposableEffect(animation) {
        isAttached.value = true
        onDispose {
            isAttached.value = false
            if (animation?.isActive == true) animation.cancel()
        }
    }
    LaunchedEffect(state, animation) {
        snapshotFlow { state.transitionState }.collect { transition ->
            if (transition is NavigationEventTransitionState.InProgress && canHandleBack) {
                animation?.updateProgress(transition.latestEvent.progress)
            }
        }
    }
    LaunchedEffect(canHandleBack) {
        if (!canHandleBack && animation?.isActive == true) animation.cancel()
    }
    NavigationBackHandler(
        state = state,
        isBackEnabled = canHandleBack,
        onBackCancelled = { animation?.cancel() },
        onBackCompleted = {
            if (canHandleBack) {
                if (animation == null) currentOnBack()
                else animation.complete({ canHandleBack && isAttached.value }, { currentOnBack() })
            }
        },
    )
}
