package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIScreen

/**
 * Prevents idle sleep and optionally dims the screen during active foreground generation.
 * Each resume captures current brightness again so changes made outside SDAI are preserved.
 *
 * @param isGenerating Whether foreground generation needs the display kept awake.
 * @param shouldDimScreen Whether to lower brightness until pause or generation completion.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun GenerationScreenProtection(isGenerating: Boolean, shouldDimScreen: Boolean) {
    LifecycleResumeEffect(isGenerating, shouldDimScreen) {
        val application = UIApplication.sharedApplication
        val screen = UIScreen.mainScreen
        val previousBrightness = screen.brightness
        val wasIdleTimerDisabled = application.idleTimerDisabled
        if (isGenerating) {
            application.idleTimerDisabled = true
            if (shouldDimScreen) screen.brightness = 0.01
        }
        onPauseOrDispose {
            if (isGenerating) {
                application.idleTimerDisabled = wasIdleTimerDisabled
                if (shouldDimScreen) screen.brightness = previousBrightness
            }
        }
    }
}
