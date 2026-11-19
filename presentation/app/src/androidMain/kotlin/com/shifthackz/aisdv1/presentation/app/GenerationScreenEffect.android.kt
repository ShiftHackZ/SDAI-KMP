package com.shifthackz.aisdv1.presentation.app

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.LifecycleResumeEffect
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Owns the Activity display override only while generation and the host are active. Brightness
 * remains window-local, preserving the system slider and automatic brightness configuration.
 *
 * @param isGenerating Whether foreground generation needs the display kept awake.
 * @param shouldDimScreen Whether to set the window to its lowest visible brightness.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun GenerationScreenEffect(isGenerating: Boolean, shouldDimScreen: Boolean) {
    val view = LocalView.current
    val window = view.context.findActivity()?.window
    LifecycleResumeEffect(window, isGenerating, shouldDimScreen) {
        val previousBrightness = window?.attributes?.screenBrightness
        val wasScreenKeptOn = window?.attributes?.flags?.and(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        ) == WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        if (isGenerating && window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (shouldDimScreen) {
                window.attributes = window.attributes.apply { screenBrightness = 0.01f }
            }
        }
        onPauseOrDispose {
            if (isGenerating && window != null) {
                if (!wasScreenKeptOn) window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                if (shouldDimScreen && previousBrightness != null) {
                    window.attributes =
                        window.attributes.apply { screenBrightness = previousBrightness }
                }
            }
        }
    }
}

/**
 * Resolves the Activity behind Compose context wrappers without retaining it beyond composition.
 * @receiver Context belonging to the application's Compose view.
 * @author Dmitriy Moroz
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
