package com.shifthackz.aisdv1.presentation.logger

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.navigation.router.LoggerRouter
import kotlinx.coroutines.withContext

/**
 * Loads application log snapshots and delegates copy/share requests for the currently displayed text.
 *
 * @param diagnostics Optional bounded MVI transition observer.
 * @param dispatchersProvider Separates file reads from immediate UI/platform operations.
 * @param logReader Reads the current platform log cache.
 * @param platformActions Copies or shares a nonblank displayed log snapshot.
 * @param router Handles back navigation from the log viewer.
 * @param onError Receives log-read and native-action failures.
 *
 * @author Dmitriy Moroz
 */
class LoggerViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val logReader: LogReader,
    private val platformActions: LoggerPlatformActions,
    private val router: LoggerRouter,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<LoggerState, LoggerIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.LOGGER.asDiagnosticScreen(),
    initialState = LoggerState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        readLogs()
    }

    override fun processIntent(intent: LoggerIntent) {
        when (intent) {
            LoggerIntent.ReadLogs -> readLogs()
            LoggerIntent.CopyLogs -> runLogAction(platformActions::copyLogs)
            LoggerIntent.ShareLogs -> runLogAction(platformActions::shareLogs)
            LoggerIntent.NavigateBack -> router.navigateBack()
        }
    }

    private fun readLogs() {
        updateState { it.copy(loading = true, text = "") }
        launch(dispatchersProvider.io) {
            runCatching { logReader.read() }
                .onSuccess { content ->
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                loading = false,
                                text = content,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(loading = false, text = "") }
                    }
                    onError(t)
                }
        }
    }

    private fun runLogAction(action: suspend (String) -> Unit) {
        val text = currentState.text.takeIf(String::isNotBlank) ?: return
        launch(dispatchersProvider.immediate) {
            runCatching { action(text) }
                .onFailure(onError)
        }
    }
}
