package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.Composable

/**
 * Leaves back navigation to the iOS host; no system-back callback is installed.
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
) = Unit
