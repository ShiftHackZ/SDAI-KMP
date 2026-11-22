package com.shifthackz.aisdv1.presentation.components.widget.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin

/**
 * Collects connectivity once for a containing layout so retained tabs can share stateless badges.
 *
 * @param content Layout receiving the latest provider status and indicator visibility.
 * @author Dmitriy Moroz
 */
@Composable
fun ConnectivityStateProvider(content: @Composable (ConnectivityState) -> Unit) {
    val koin = remember { getKoin() }
    val viewModel = remember(koin) { koin.get<ConnectivityViewModel>() }
    MviComponent(viewModel = viewModel, content = content)
}
