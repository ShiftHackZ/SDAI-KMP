package com.shifthackz.aisdv1.presentation.logger

import androidx.compose.ui.Modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.core.navigation.router.LoggerRouter
import org.koin.core.parameter.parametersOf

/**
 * Binds the log viewer to a remembered ViewModel from the initialized graph.
 *
 * @param modifier Layout modifiers for the log screen.
 * @param router Optional back-navigation owner; defaults to the application graph binding.
 * @author Dmitriy Moroz
 */
@Composable
fun LoggerScreen(
    modifier: Modifier = Modifier,
    router: LoggerRouter? = null,
) {
    val koin = remember { getKoin() }
    val resolvedRouter = remember(koin, router) {
        router ?: koin.get<LoggerRouter>()
    }
    val viewModel = remember(koin, resolvedRouter) {
        koin.get<LoggerViewModel> {
            parametersOf(resolvedRouter)
        }
    }
    MviComponent(
        viewModel = viewModel,
    ) { state, intentHandler ->
        LoggerScreenContent(
            strings = LoggerScreenStrings(),
            state = state.toContentState(),
            processAction = { action -> intentHandler(action.toIntent()) },
            modifier = modifier,
        )
    }
}
