package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.Composable

/**
 * Installs the platform back callback for the root router.
 *
 * @param enabled Whether the root currently has a drawer or route to dismiss.
 * @param onBack Closes the drawer or pops the root route when the platform dispatches back.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun RootBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
