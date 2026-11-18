package com.shifthackz.aisdv1.presentation.app

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController

/**
 * Creates the Compose controller embedded by the Swift host after shared graph/runtime registration.
 * Disables automatic focus-driven viewport panning so text fields use the application's inset handling.
 *
 * @return A new controller owning its Compose application composition.
 *
 * @author Dmitriy Moroz
 */
fun MainViewController() = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    },
) {
    AiSdApp()
}
