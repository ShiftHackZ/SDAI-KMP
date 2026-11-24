package com.shifthackz.aisdv1.presentation.faq

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.domain.usecase.faq.FetchOrGetFaqUseCase
import kotlinx.coroutines.CancellationException

/**
 * Resolves FAQ data on entry and serializes manual retries within this screen visit.
 *
 * @param diagnostics Observer for screen state transitions.
 * @param dispatchersProvider Main dispatcher for publishing screen state.
 * @param fetchOrGetFaq Online-first FAQ loader with offline recovery.
 * @author Dmitriy Moroz
 */
internal class FaqViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val fetchOrGetFaq: FetchOrGetFaqUseCase,
) : BaseMviViewModel<FaqState, FaqIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.FAQ.asDiagnosticScreen(),
    initialState = FaqState.Loading,
    effectDispatcher = dispatchersProvider.immediate,
) {
    init {
        load()
    }

    override fun processIntent(intent: FaqIntent) {
        when (intent) {
            FaqIntent.Retry -> if (currentState != FaqState.Loading) load()
        }
    }

    private fun load() {
        emitState(FaqState.Loading)
        launch(dispatchersProvider.immediate) {
            val result = try {
                FaqState.Ready(fetchOrGetFaq())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                FaqState.Error
            }
            emitState(result)
        }
    }
}
