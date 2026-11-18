package com.shifthackz.aisdv1.presentation.statistics.networkusage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.core.navigation.router.NetworkUsageRouter
import com.shifthackz.aisdv1.presentation.components.widget.usage.UsageScreenContent
import com.shifthackz.aisdv1.presentation.components.model.UsageScreenKind
import org.koin.core.parameter.parametersOf

/**
 * Standalone network usage route without bottom navigation.
 *
 * The screen observes Room-backed traffic counters in real time and delegates rendering to the
 * shared Telegram-style usage dashboard.
 *
 * @param modifier Compose modifier applied to the full-screen route.
 * @param router Optional router override for tests and previews; Koin supplies the app router otherwise.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun NetworkUsageScreen(
    modifier: Modifier = Modifier,
    router: NetworkUsageRouter? = null,
) {
    val koin = remember { getKoin() }
    val resolvedRouter = remember(koin, router) {
        router ?: koin.get<NetworkUsageRouter>()
    }
    val viewModel = remember(koin, resolvedRouter) {
        koin.get<NetworkUsageViewModel> {
            parametersOf(resolvedRouter)
        }
    }

    MviComponent(viewModel = viewModel) { state, intentHandler ->
        NetworkUsageScreenContent(
            modifier = modifier,
            state = state,
            processIntent = intentHandler,
        )
    }
}

/**
 * Stateless network usage content adapter for previews and ViewModel tests.
 *
 * @param modifier Compose modifier applied to the shared usage layout.
 * @param state Current network usage state emitted by [NetworkUsageViewModel].
 * @param processIntent Intent sink used to route UI actions back to the ViewModel.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun NetworkUsageScreenContent(
    modifier: Modifier = Modifier,
    state: NetworkUsageState,
    processIntent: (NetworkUsageIntent) -> Unit = {},
) {
    UsageScreenContent(
        modifier = modifier,
        screen = UsageScreenKind.NETWORK,
        usage = state.usage,
        onBack = { processIntent(NetworkUsageIntent.NavigateBack) },
        onSelectCategory = { processIntent(NetworkUsageIntent.SelectCategory(it)) },
        onPrimaryAction = { processIntent(NetworkUsageIntent.ResetStatistics) },
    )
}
