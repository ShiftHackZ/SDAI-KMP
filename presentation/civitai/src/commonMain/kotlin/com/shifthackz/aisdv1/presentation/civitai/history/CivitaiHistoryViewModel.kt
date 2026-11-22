package com.shifthackz.aisdv1.presentation.civitai.history

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
import com.shifthackz.aisdv1.domain.repository.CivitaiUploadHistoryRepository
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryMemoryBudget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Uses the gallery's viewport-window loading pattern for account-independent publication history.
 *
 * @param repository Strict receipt identities and bounded storage projections.
 * @param dispatchers IO reads and immediate MVI delivery dispatchers.
 * @param diagnostics Credential-free screen observer.
 * @param router Invoking Civitai settings navigation owner.
 * @param memoryBudget Shared gallery metadata-window and prefetch limits.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CivitaiHistoryViewModel(
    private val repository: CivitaiUploadHistoryRepository,
    private val dispatchers: DispatchersProvider,
    diagnostics: UiDiagnostics,
    private val router: CivitaiRouter,
    private val memoryBudget: GalleryMemoryBudget,
) : BaseMviViewModel<CivitaiHistoryState, CivitaiHistoryIntent, EmptyEffect>(
    diagnostics,
    NavigationDestination.CIVITAI_HISTORY.asDiagnosticScreen(),
    CivitaiHistoryState(),
    dispatchers.immediate,
) {
    private val windowOffset = MutableStateFlow(0)
    private var reads: List<Job> = emptyList()

    init {
        observeHistory()
    }

    /**
     * Moves the bounded metadata window or handles history retry and Back actions.
     *
     * @param intent Current lazy-list viewport or explicit user action.
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: CivitaiHistoryIntent) {
        when (intent) {
            is CivitaiHistoryIntent.Viewport -> windowOffset.value =
                (intent.first - memoryBudget.metadataPrefetch).coerceAtLeast(0)

            CivitaiHistoryIntent.Retry -> observeHistory()
            CivitaiHistoryIntent.Back -> router.navigateBack()
        }
    }

    /**
     * Stops storage observation when the standalone route leaves composition.
     *
     * @author Dmitriy Moroz
     */
    fun onRouteDisposed() {
        reads.forEach { it.cancel() }
        reads = emptyList()
    }

    /**
     * Observes lightweight identities separately from the viewport-sized receipt window.
     * Failures remain visible until the user retries; cancellation never becomes an error row.
     *
     * @author Dmitriy Moroz
     */
    private fun observeHistory() {
        onRouteDisposed()
        updateState { it.copy(isLoading = true, hasFailed = false) }
        reads = listOf(
            launch(dispatchers.io) {
                repository.observeIds().catch { error ->
                    if (error is CancellationException) throw error
                    updateState { it.copy(isLoading = false, hasFailed = true) }
                }.collect { ids -> updateState { it.copy(itemIds = ids, isLoading = false) } }
            },
            launch(dispatchers.io) {
                windowOffset.flatMapLatest { offset ->
                    repository.observePage(
                        memoryBudget.metadataWindow,
                        offset
                    )
                }
                    .catch { error ->
                        if (error is CancellationException) throw error
                        updateState { it.copy(isLoading = false, hasFailed = true) }
                    }.collect { rows -> updateState { it.copy(uploads = rows) } }
            },
        )
    }
}
