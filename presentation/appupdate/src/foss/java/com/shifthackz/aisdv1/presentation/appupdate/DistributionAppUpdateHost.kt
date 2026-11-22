package com.shifthackz.aisdv1.presentation.appupdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Unsupported native updater; web release prompts are rendered by the shared host.
 *
 * @param onFinished Consumes an unsupported native notification without launching anything.
 * @author Dmitriy Moroz
 */
@Composable
internal fun DistributionAppUpdateHost(onFinished: () -> Unit) {
    LaunchedEffect(Unit) { onFinished() }
}
