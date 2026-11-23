@file:OptIn(ExperimentalComposeUiApi::class)

package com.shifthackz.aisdv1.presentation.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController

/**
 * Creates the Compose controller embedded by the Swift host after shared graph/runtime registration.
 * Disables automatic focus-driven viewport panning so text fields use the application's inset handling.
 * Presents Compose cutouts and UIKit interop moves in the same Core Animation transaction,
 * preventing native glass backgrounds from lagging behind their controls during scroll deceleration.
 *
 * @return A new controller owning its Compose application composition.
 *
 * @author Dmitriy Moroz
 */
fun MainViewController() = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
        parallelRendering = false
    },
) {
    AiSdApp()
}
