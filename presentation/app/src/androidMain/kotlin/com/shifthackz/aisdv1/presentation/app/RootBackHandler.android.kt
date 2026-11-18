package com.shifthackz.aisdv1.presentation.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Forwards Android system back events to the root router while interception is enabled.
 *
 * @param enabled Whether the root currently has a drawer or route to dismiss.
 * @param onBack Closes the drawer or pops the root route when the platform dispatches back.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun RootBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
