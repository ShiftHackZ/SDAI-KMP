package com.shifthackz.aisdv1.presentation.components.widget.work

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

/**
 * Combines active background-work status and the latest outcome into a shared dismissible notice.
 *
 * @param diagnostics Optional bounded observer for work-widget transitions.
 * @param dispatchersProvider Delivers status/result changes on the immediate UI dispatcher.
 * @param backgroundWorkObserver Supplies active work and retained results, and dismisses acknowledged outcomes.
 * @param onError Receives background-work observation failures.
 *
 * @author Dmitriy Moroz
 */
class BackgroundWorkViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<BackgroundWorkState, BackgroundWorkIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = UiDiagnosticComponent.BACKGROUND_WORK,
    initialState = BackgroundWorkState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.immediate) {
            backgroundWorkObserver
                .observeStatus()
                .combine(backgroundWorkObserver.observeResult(), ::Pair)
                .catch { onError(it) }
                .collect { (work, result) ->
                    if (work.running) {
                        updateState {
                            it.copy(
                                visible = true,
                                running = true,
                                dismissible = false,
                                title = work.statusTitle,
                                subTitle = work.statusSubTitle,
                                percentage = work.percentage,
                                isError = false,
                                image = null,
                                imageBase64 = null,
                            )
                        }
                        return@collect
                    }
                    val resultTitle = when (result) {
                        is BackgroundWorkResult.Error -> Localization.string("notification_fail_title")
                        is BackgroundWorkResult.Success -> Localization.string("notification_finish_title")
                        else -> ""
                    }
                    val failure = (result as? BackgroundWorkResult.Error)?.t
                    val resultSubTitle = if (failure is NativeGenerationFailure) {
                        failure.toUserMessage()
                    } else {
                        failure?.message.orEmpty()
                    }
                    updateState {
                        it.copy(
                            visible = result !is BackgroundWorkResult.None,
                            running = false,
                            percentage = null,
                            dismissible = result !is BackgroundWorkResult.None,
                            title = resultTitle,
                            subTitle = resultSubTitle,
                            isError = result is BackgroundWorkResult.Error,
                            image = null,
                            imageBase64 = (result as? BackgroundWorkResult.Success)?.ai?.firstOrNull()?.image,
                        )
                    }
                }
        }
    }

    override fun processIntent(intent: BackgroundWorkIntent) {
        when (intent) {
            BackgroundWorkIntent.Dismiss -> {
                updateState {
                    it.copy(
                        visible = false,
                        running = false,
                        percentage = null,
                        dismissible = false,
                        title = "",
                        subTitle = "",
                        isError = false,
                        image = null,
                        imageBase64 = null,
                    )
                }
                backgroundWorkObserver.dismissResult()
            }
        }
    }

}
