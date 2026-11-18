package com.shifthackz.aisdv1.presentation.configuration.modal.download

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalModelUseCase

/**
 * Loads a catalog model's download mirrors, selects the first and emits the confirmed mirror URL.
 *
 * @param diagnostics Optional bounded observer for download-selector MVI transitions.
 * @param modelId Catalog model identity whose source URLs should be offered.
 * @param getLocalModelUseCase Reads model metadata containing available download mirrors.
 * @param dispatchersProvider Runs metadata lookup on IO and dispatches confirmation effects.
 * @param onError Receives model-catalog lookup failures.
 *
 * @author Dmitriy Moroz
 */
class DownloadDialogViewModel(
    diagnostics: UiDiagnostics,
    modelId: String,
    private val getLocalModelUseCase: GetLocalModelUseCase,
    private val dispatchersProvider: DispatchersProvider,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<DownloadDialogState, DownloadDialogIntent, DownloadDialogEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = UiDiagnosticComponent.DOWNLOAD_DIALOG,
    initialState = DownloadDialogState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        loadModelData(modelId)
    }

    override fun processIntent(intent: DownloadDialogIntent) {
        when (intent) {
            is DownloadDialogIntent.SelectSource -> updateState {
                it.copy(sources = it.sources.map { (url, _) -> url to (url == intent.url) })
            }

            DownloadDialogIntent.StartDownload -> emitEffect(
                DownloadDialogEffect.StartDownload(currentState.selectedUrl),
            )

            DownloadDialogIntent.Close -> emitEffect(DownloadDialogEffect.Close)
        }
    }

    private fun loadModelData(id: String) {
        launch(dispatchersProvider.io) {
            runCatching { getLocalModelUseCase(id) }
                .onFailure(onError)
                .onSuccess { model ->
                    updateState {
                        it.copy(sources = model.sources.mapIndexed { index, url -> url to (index == 0) })
                    }
                }
        }
    }
}
