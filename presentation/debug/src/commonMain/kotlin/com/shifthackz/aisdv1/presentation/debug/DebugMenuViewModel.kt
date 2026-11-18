package com.shifthackz.aisdv1.presentation.debug

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
 * Maintains developer settings and dispatches explicitly requested diagnostics and platform debug actions.
 * Settings stay synchronized with the preference stream; operation outcomes become transient UI messages.
 *
 * @param diagnostics Bounded capture controller and observer for this screen's MVI transitions.
 * @param diagnosticReports Exports sanitized diagnostic snapshots through platform sharing.
 * @param dispatchersProvider Separates filesystem/debug work from immediate UI delivery.
 * @param preferenceManager Observes and updates local-inference debugging preferences.
 * @param debugInsertBadBase64UseCase Inserts deliberately invalid gallery data for decoder diagnostics.
 * @param router Navigates to logs or back to the previous screen.
 * @param platformActions Performs available log and background-work operations for this platform.
 * @param onError Receives failed debug operations before a failure message is emitted.
 *
 * @author Dmitriy Moroz
 */
class DebugMenuViewModel(
    diagnostics: UiDiagnostics,
    private val diagnosticReports: UiDiagnosticReportActions,
    private val dispatchersProvider: DispatchersProvider,
    private val preferenceManager: PreferenceManager,
    private val debugInsertBadBase64UseCase: DebugInsertBadBase64UseCase,
    private val router: DebugMenuRouter,
    private val platformActions: DebugMenuPlatformActions,
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
     * Mirrors capture-controller changes into the debug switch state.
     *
     * @param isCapturing Whether bounded UI event capture is currently active.
     *
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
