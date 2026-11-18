package com.shifthackz.aisdv1.presentation.report

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.report.SendReportUseCase
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.core.navigation.router.ReportRouter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

/**
 * Loads the reported generation image, maintains editable report fields and submits with a bounded timeout.
 * Submission feedback preserves the configured distribution-specific error presentation policy.
 *
 * @param diagnostics Optional bounded MVI transition observer.
 * @param itemId Positive gallery identity, or nonpositive to report the last cached generation.
 * @param dispatchersProvider Runs result reads and submission work on IO and dispatches UI effects.
 * @param sendReportUseCase Submits the selected category, user description and generation image.
 * @param getGenerationResultUseCase Reads a persisted gallery generation by its positive identity.
 * @param getLastResultFromCacheUseCase Reads the transient result when no persistent identity was supplied.
 * @param router Handles back navigation from the report screen.
 * @param buildInfoProvider Selects the distribution-specific submission-failure presentation policy.
 * @param onError Receives source-image and report-submission failures.
 *
 * @author Dmitriy Moroz
 */
class ReportViewModel(
    diagnostics: UiDiagnostics,
    private val itemId: Long,
    private val dispatchersProvider: DispatchersProvider,
    private val sendReportUseCase: SendReportUseCase,
    private val getGenerationResultUseCase: GetGenerationResultUseCase,
    private val getLastResultFromCacheUseCase: GetLastResultFromCacheUseCase,
    private val router: ReportRouter,
    private val buildInfoProvider: BuildInfoProvider,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<ReportState, ReportIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.REPORT.asDiagnosticScreen(),
    initialState = ReportState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.io) {
            runCatching { getGenerationResult(itemId) }
                .onFailure(onError)
                .onSuccess { ai ->
                    updateState {
                        it.copy(
                            imageBase64 = ai.image,
                            loading = false,
                        )
                    }
                }
        }
    }

    override fun processIntent(intent: ReportIntent) {
        when (intent) {
            ReportIntent.Submit -> submitReport()
            is ReportIntent.UpdateReason -> updateState { it.copy(reason = intent.reason) }
            is ReportIntent.UpdateText -> updateState { it.copy(text = intent.text) }
            ReportIntent.NavigateBack -> router.navigateBack()
            ReportIntent.DismissError -> updateState { it.copy(error = ErrorState.None) }
        }
    }

    private suspend fun getGenerationResult(id: Long): AiGenerationResult {
        if (id <= 0) return getLastResultFromCacheUseCase()
        return getGenerationResultUseCase(id)
    }

    private fun submitReport() {
        updateState { it.copy(loading = true) }
        launch(dispatchersProvider.io) {
            try {
                withTimeout(REPORT_TIMEOUT_MS.milliseconds) {
                    sendReportUseCase(
                        text = currentState.text,
                        reason = currentState.reason,
                        image = currentState.imageBase64,
                    )
                }
                updateState { it.copy(reportSent = true) }
            } catch (t: Throwable) {
                if (t is CancellationException && t !is TimeoutCancellationException) throw t
                onError(t)
                updateState { state ->
                    when (buildInfoProvider.type) {
                        BuildType.PLAY -> state.copy(reportSent = true)
                        else -> state.copy(
                            reportSent = false,
                            error = ErrorState.WithMessage(
                                (t.message ?: "Something went wrong").asUiText(),
                            ),
                        )
                    }
                }
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }

    companion object {
        private const val REPORT_TIMEOUT_MS = 10_000L
    }
}
