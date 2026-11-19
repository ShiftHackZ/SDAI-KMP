package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.shifthackz.aisdv1.presentation.components.extensions.LocalGenerationBrightnessInteraction
import kotlinx.coroutines.delay

/**
 * Keeps foreground generation awake and temporarily restores brightness after interaction.
 * Each touch restarts a 1,750 ms inactivity timer. Pausing, finishing or disabling dimming
 * clears the temporary override; resuming an ongoing generation applies dimming again.
 *
 * @param isGenerating Whether either form is generating with background generation disabled.
 * @param shouldDimScreen Whether the persisted opt-in permits minimum display brightness.
 * @param content Application surfaces and dialogs that can report brightness interaction.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationScreenProtection(
    isGenerating: Boolean,
    shouldDimScreen: Boolean,
    content: @Composable () -> Unit,
) {
    var interactionRevision by remember(isGenerating, shouldDimScreen) { mutableLongStateOf(0L) }
    var isTemporarilyBright by remember(isGenerating, shouldDimScreen) { mutableStateOf(false) }
    val onInteraction: (() -> Unit)? = if (isGenerating && shouldDimScreen) {
        remember(isGenerating, shouldDimScreen) {
            {
                isTemporarilyBright = true
                interactionRevision++
            }
        }
    } else null

    LaunchedEffect(interactionRevision, isGenerating, shouldDimScreen) {
        if (isTemporarilyBright) {
            delay(BRIGHTNESS_INTERACTION_TIMEOUT_MS)
            isTemporarilyBright = false
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        isTemporarilyBright = false
    }
    GenerationScreenEffect(
        isGenerating = isGenerating,
        shouldDimScreen = shouldDimScreen && !isTemporarilyBright,
    )
    CompositionLocalProvider(LocalGenerationBrightnessInteraction provides onInteraction) {
        content()
    }
}

private const val BRIGHTNESS_INTERACTION_TIMEOUT_MS = 1_750L
