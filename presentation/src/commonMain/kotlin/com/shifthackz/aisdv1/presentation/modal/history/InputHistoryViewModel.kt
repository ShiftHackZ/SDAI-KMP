package com.shifthackz.aisdv1.presentation.modal.history

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCase
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Exposes the `INPUT_HISTORY_FIRST_PAGE` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val INPUT_HISTORY_FIRST_PAGE = 0

/**
 * Exposes the `INPUT_HISTORY_PAGE_SIZE` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val INPUT_HISTORY_PAGE_SIZE = 60

/**
 * Coordinates `InputHistoryViewModel` behavior in the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 * @param diagnostics Injected fixed-capacity opt-in state/composition observer.
 * @param galleryReads Metadata-only history reads.
 * @param getGenerationResultUseCase Original loaded only after explicit selection.
 */
class InputHistoryViewModel(
    diagnostics: UiDiagnostics,
    /**
     * Exposes the `dispatchersProvider` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val dispatchersProvider: DispatchersProvider,
    /**
     * Reads metadata projections for visible input-history pages.
     *
     * @author Dmitriy Moroz
     */
    private val galleryReads: GalleryReadGateway,
    private val getGenerationResultUseCase: GetGenerationResultUseCase,
    /**
     * Exposes the `onError` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
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
