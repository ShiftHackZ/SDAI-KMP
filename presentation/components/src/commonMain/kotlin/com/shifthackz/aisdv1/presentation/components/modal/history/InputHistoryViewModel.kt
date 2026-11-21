package com.shifthackz.aisdv1.presentation.components.modal.history

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val INPUT_HISTORY_FIRST_PAGE = 0


private const val INPUT_HISTORY_PAGE_SIZE = 60

/**
 * Pages history metadata and loads original generation content only after explicit selection.
 *
 * @param diagnostics Optional bounded observer for history-selector MVI transitions.
 * @param dispatchersProvider Runs metadata/original reads on IO and delivers selected results on the UI dispatcher.
 * @param galleryReads Supplies paged metadata without retaining full image bytes in list state.
 * @param getGenerationResultUseCase Resolves the selected persistent identity to its full original generation.
 * @param onError Receives page-read and selected-result failures.
 *
 * @author Dmitriy Moroz
 */
class InputHistoryViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val galleryReads: GalleryReadGateway,
    private val getGenerationResultUseCase: GetGenerationResultUseCase,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<InputHistoryState, InputHistoryIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = UiDiagnosticComponent.INPUT_HISTORY,
    initialState = InputHistoryState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        loadPage(reset = true)
    }

    override fun processIntent(intent: InputHistoryIntent) {
        when (intent) {
            InputHistoryIntent.LoadNextPage -> loadPage(reset = false)
            InputHistoryIntent.Retry -> loadPage(reset = true)
        }
    }

    /**
     * Reads only the explicitly selected original instead of retaining every image in list state.
     *
     * @param id Selected persisted identity.
     * @param onSelected Delivery to the owning generation form.
     *
     * @author Dmitriy Moroz
     */
    fun select(id: Long, onSelected: (AiGenerationResult) -> Unit) {
        launch(dispatchersProvider.io) {
            try {
                val original = getGenerationResultUseCase(id)
                withContext(dispatchersProvider.immediate) { onSelected(original) }
            } catch (t: Exception) {
                if (t is CancellationException) throw t
                onError(t)
                updateState { it.copy(error = Localization.string("error_generic")) }
            }
        }
    }

    private fun loadPage(reset: Boolean) {
        val state = currentState
        if (!reset && (!state.canLoadMore || state.loading || state.loadingNextPage)) return

        val page = if (reset) INPUT_HISTORY_FIRST_PAGE else state.nextPage
        updateState {
            it.copy(
                loading = reset,
                loadingNextPage = !reset,
                items = if (reset) emptyList() else it.items,
                nextPage = if (reset) INPUT_HISTORY_FIRST_PAGE else it.nextPage,
                canLoadMore = true,
                error = null,
            )
        }

        launch(dispatchersProvider.io) {
            runCatching {
                galleryReads.observeMetadata(
                    limit = INPUT_HISTORY_PAGE_SIZE,
                    offset = page * INPUT_HISTORY_PAGE_SIZE,
                ).first().map { generation ->
                    InputHistoryItemUi(
                        generationResult = generation,
                    )
                }
            }
                .onSuccess { pageItems ->
                    updateState { current ->
                        current.copy(
                            loading = false,
                            loadingNextPage = false,
                            items = if (reset) pageItems else current.items + pageItems,
                            nextPage = page + 1,
                            canLoadMore = pageItems.isNotEmpty(),
                            error = null,
                        )
                    }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    onError(t)
                    updateState {
                        it.copy(
                            loading = false,
                            loadingNextPage = false,
                            canLoadMore = false,
                            error = Localization.string("error_generic"),
                        )
                    }
                }
        }
    }
}
