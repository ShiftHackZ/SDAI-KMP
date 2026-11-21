package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import kotlinx.coroutines.CancellationException

/**
 * Uses only Android's system Back stream, including predictive progress and cancellation.
 * Button navigation continues to work when no physical gesture progress is supplied.
 *
 * @param isEnabled Whether this screen owns Android Back.
 * @param onBack Existing screen action on completion.
 * @param canStartGesture Whether animation may preview leaving; blocked Back still reaches its handler.
 * @param transitionState Local-step animation state, otherwise inherited route progress.
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
    DisposableEffect(animation) {
        isAttached.value = true
        onDispose {
            isAttached.value = false
            if (animation?.isActive == true) animation.cancel()
        }
    }
    val canHandleBack by rememberUpdatedState(isEnabled && LocalMviVisibility.current && LocalNavigationInputEnabled.current)
    val canPreview by rememberUpdatedState(canStartGesture)
    val currentOnBack by rememberUpdatedState(onBack)
    PredictiveBackHandler(enabled = canHandleBack) { events ->
        try {
            events.collect { if (canPreview) animation?.updateProgress(it.progress) }
            if (canHandleBack) {
                if (animation == null || !canPreview) currentOnBack()
                else animation.complete(
                    { canHandleBack && canPreview && isAttached.value },
                    { currentOnBack() })
            }
        } catch (cancelled: CancellationException) {
            animation?.cancel()
            throw cancelled
        }
    }
}
