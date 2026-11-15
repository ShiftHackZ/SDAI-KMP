package com.shifthackz.aisdv1.presentation.widget.work

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
 * Coordinates `BackgroundWorkViewModel` behavior in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 * @param diagnostics Injected fixed-capacity opt-in state/composition observer.
 */
class BackgroundWorkViewModel(
    diagnostics: UiDiagnostics,
    /**
     * Exposes the `dispatchersProvider` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val dispatchersProvider: DispatchersProvider,
    /**
     * Exposes the `backgroundWorkObserver` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val backgroundWorkObserver: BackgroundWorkObserver,
    /**
     * Exposes the `imageLoader` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val imageLoader: BackgroundWorkImageLoader,
    /**
     * Exposes the `onError` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
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
