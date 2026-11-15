package com.shifthackz.aisdv1.presentation.screen.debug

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.debug.DebugInsertBadBase64UseCase
import com.shifthackz.aisdv1.core.navigation.router.DebugMenuRouter
import kotlinx.coroutines.flow.catch

/**
 * Coordinates `DebugMenuViewModel` behavior in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 * @param diagnosticReports Platform share boundary invoked only by the explicit export action.
 * @param diagnostics Injected fixed-capacity opt-in state/composition observer.
 */
class DebugMenuViewModel(
    diagnostics: UiDiagnostics,
    private val diagnosticReports: UiDiagnosticReportActions,
    /**
     * Exposes the `dispatchersProvider` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val dispatchersProvider: DispatchersProvider,
    /**
     * Exposes the `preferenceManager` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager,
    /**
     * Exposes the `debugInsertBadBase64UseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val debugInsertBadBase64UseCase: DebugInsertBadBase64UseCase,
    /**
     * Exposes the `router` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val router: DebugMenuRouter,
    /**
     * Exposes the `platformActions` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val platformActions: DebugMenuPlatformActions,
    /**
     * Exposes the `onError` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<DebugMenuState, DebugMenuIntent, DebugMenuEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.DEBUG_MENU.asDiagnosticScreen(),
    initialState = DebugMenuState(
        isUiDiagnosticsSupported = diagnostics.isSupported,
        showWorkManagerSection = platformActions.showWorkManagerSection,
        showLocalDiffusionSection = platformActions.showLocalDiffusionSection,
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.immediate) {
            preferenceManager
                .observe()
                .catch { onError(it) }
                .collect { settings ->
                    updateState {
                        it.copy(
                            localDiffusionAllowCancel = settings.localDiffusionAllowCancel,
                            localDiffusionSchedulerThread = settings.localDiffusionSchedulerThread,
                        )
                    }
                }
        }
    }

    /**
     * Updates capture controls through the composition-bound observer owned by the MVI base.
     *
     * @param isCapturing Explicit capture selection, never a per-event count notification.
     * @author Dmitriy Moroz
     */
    override fun onDiagnosticCaptureChanged(isCapturing: Boolean) {
        updateState { it.copy(isUiDiagnosticsCapturing = isCapturing) }
    }

    override fun processIntent(intent: DebugMenuIntent) {
        when (intent) {
            DebugMenuIntent.StartUiCapture -> diagnostics.startCapture()
            DebugMenuIntent.StopUiCapture -> diagnostics.stopCapture()
            DebugMenuIntent.ResetUiCapture -> diagnostics.reset()
            DebugMenuIntent.ShareUiCapture -> if (diagnostics.isSupported) launch(
                dispatchersProvider.io
            ) {
                diagnosticReports.share(diagnostics.snapshot())
            }

            DebugMenuIntent.NavigateBack -> router.navigateBack()
            DebugMenuIntent.ViewLogs -> router.navigateToLogger()
            DebugMenuIntent.InsertBadBase64 -> launch(dispatchersProvider.io) {
                runCatching { debugInsertBadBase64UseCase() }.handleState()
            }

            DebugMenuIntent.ClearLogs -> launch(dispatchersProvider.io) {
                platformActions.clearLogs().handleState()
            }

            DebugMenuIntent.AllowLocalDiffusionCancel -> {
                if (platformActions.showLocalDiffusionSection) {
                    preferenceManager.localOnnxAllowCancel = !currentState.localDiffusionAllowCancel
                }
            }

            DebugMenuIntent.LocalDiffusionScheduler.Request -> updateState {
                if (platformActions.showLocalDiffusionSection) {
                    it.copy(screenModal = DebugMenuModal.LDScheduler(it.localDiffusionSchedulerThread))
                } else {
                    it
                }
            }

            is DebugMenuIntent.LocalDiffusionScheduler.Confirm -> {
                if (platformActions.showLocalDiffusionSection) {
                    preferenceManager.localOnnxSchedulerThread = intent.token
                }
                updateState { it.copy(screenModal = DebugMenuModal.None) }
            }

            DebugMenuIntent.DismissModal -> updateState {
                it.copy(screenModal = DebugMenuModal.None)
            }

            DebugMenuIntent.WorkManager.CancelAll -> launch(dispatchersProvider.io) {
                platformActions.cancelAllWork().handleState()
            }

            DebugMenuIntent.WorkManager.RestartTxt2Img -> launch(dispatchersProvider.io) {
                platformActions.retryLastTextToImageTask().handleState()
            }

            DebugMenuIntent.WorkManager.RestartImg2Img -> launch(dispatchersProvider.io) {
                platformActions.retryLastImageToImageTask().handleState()
            }
        }
    }

    private fun Result<Unit>.handleState() = fold(
        onSuccess = { onSuccess() },
        onFailure = ::onErrorMessage,
    )

    private fun onSuccess() {
        emitEffect(DebugMenuEffect.Message(Localization.string("success")))
    }

    private fun onErrorMessage(t: Throwable) {
        onError(t)
        emitEffect(DebugMenuEffect.Message(Localization.string("failure")))
    }
}
