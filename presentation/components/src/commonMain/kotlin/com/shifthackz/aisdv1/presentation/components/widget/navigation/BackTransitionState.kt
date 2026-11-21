package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Holds only visual Back progress. Business navigation runs once after a completed gesture settles.
 * Cancellation restores the current screen without invoking its Back action.
 *
 * @param scope Composition-owned scope cancelling settling animations with their UI owner.
 * @author Dmitriy Moroz
 */
class BackTransitionState internal constructor(private val scope: CoroutineScope) {
    /** Fraction of the current screen revealed toward its Back destination. @author Dmitriy Moroz */
    var progress by mutableFloatStateOf(0f)
        private set

    /** Whether the finger or a settling animation owns the transition. @author Dmitriy Moroz */
    var isActive by mutableStateOf(false)
        private set

    /** Whether navigation has committed after the foreground reached the far edge. @author Dmitriy Moroz */
    var hasCommittedGesture by mutableStateOf(false)
        private set

    private var settling: Job? = null

    /**
     * Resets visual state after the owning route or local step changes.
     *
     * @author Dmitriy Moroz
     */
    fun reset() {
        settling?.cancel()
        settling = null
        progress = 0f
        isActive = false
        hasCommittedGesture = false
    }

    /**
     * Follows the active pointer, interrupting a previous cancellation settle when a new drag starts.
     *
     * @param value Platform gesture completion fraction, clamped to zero through one.
     * @author Dmitriy Moroz
     */
    internal fun updateProgress(value: Float) {
        if (hasCommittedGesture) return
        settling?.cancel()
        settling = null
        isActive = true
        progress = value.coerceIn(0f, 1f)
    }

    /** Returns the current screen to rest without dispatching navigation. @author Dmitriy Moroz */
    internal fun cancel() {
        settling?.cancel()
        settling = scope.launch {
            animate(progress, 0f, animationSpec = tween(180)) { value, _ -> progress = value }
            isActive = false
            hasCommittedGesture = false
        }
    }

    /**
     * Settles a gesture or button-requested preview before dispatching its existing Back action.
     *
     * @param canCommit Rechecks current permission after settling and before any navigation effect.
     * @param onBack Commits one route or local-step change after successful completion.
     * @author Dmitriy Moroz
     */
    fun complete(canCommit: () -> Boolean, onBack: () -> Unit) {
        if (settling?.isActive == true) return
        if (!canCommit()) {
            cancel()
            return
        }
        isActive = true
        settling = scope.launch {
            animate(progress, 1f, animationSpec = tween(160)) { value, _ -> progress = value }
            if (canCommit()) {
                hasCommittedGesture = true
                isActive = false
                onBack()
            } else {
                animate(progress, 0f, animationSpec = tween(180)) { value, _ -> progress = value }
                isActive = false
            }
        }
    }
}

/**
 * Remembers gesture animation state without putting transient progress into navigation history.
 *
 * @return State released with the containing composition.
 * @author Dmitriy Moroz
 */
@Composable
fun rememberBackTransitionState(): BackTransitionState {
    val scope = rememberCoroutineScope()
    return remember(scope) { BackTransitionState(scope) }
}
