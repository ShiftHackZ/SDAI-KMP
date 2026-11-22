package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkStatus
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Retains atomic generation status and the latest outcome for shared iOS screens.
 *
 * @author Dmitriy Moroz
 */
internal class BackgroundWorkObserverImpl : BackgroundWorkObserver {

    private val statusFlow = MutableStateFlow(BackgroundWorkStatus(false, "", ""))
    private val resultFlow = MutableStateFlow<BackgroundWorkResult>(BackgroundWorkResult.None)

    /**
     * Observes running state, stage and measured percentage as a single snapshot.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus(): Flow<BackgroundWorkStatus> =
        statusFlow

    /**
     * Observes the retained outcome until it is acknowledged.
     *
     * @author Dmitriy Moroz
     */
    override fun observeResult(): Flow<BackgroundWorkResult> = resultFlow

    /**
     * Clears the outcome acknowledged by the result UI.
     *
     * @author Dmitriy Moroz
     */
    override fun dismissResult() {
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Keeps in-process state; iOS has no persisted worker queue to query.
     *
     * @author Dmitriy Moroz
     */
    override fun refreshStatus() = Unit

    /**
     * Publishes an indeterminate stage and clears a previous outcome.
     *
     * @param title Localized generation heading.
     * @param subTitle Localized current stage.
     *
     * @author Dmitriy Moroz
     */
    override fun postStatusMessage(title: String, subTitle: String) =
        postProgressMessage(title, subTitle, null)

    /**
     * Publishes measured completion together with its stage, preserving unknown progress as null.
     *
     * @param title Localized generation heading.
     * @param subTitle Localized stage or sampling counters.
     * @param percentage Measured percentage, or null while no measurement is available.
     * @author Dmitriy Moroz
     */
    override fun postProgressMessage(title: String, subTitle: String, percentage: Int?) {
        statusFlow.value = BackgroundWorkStatus(true, title, subTitle, percentage?.coerceIn(0, 100))
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Ends running status and retains generated images for the result UI.
     *
     * @param result Persisted generation results.
     *
     * @author Dmitriy Moroz
     */
    override fun postSuccessSignal(result: List<AiGenerationResult>) {
        statusFlow.value = BackgroundWorkStatus(false, "", "")
        resultFlow.value = BackgroundWorkResult.Success(result)
    }

    /**
     * Clears running status without a success or error outcome.
     *
     * @author Dmitriy Moroz
     */
    override fun postCancelSignal() {
        statusFlow.value = BackgroundWorkStatus(false, "", "")
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Ends running status and retains the generation failure.
     *
     * @param t Failure surfaced by generation or persistence.
     *
     * @author Dmitriy Moroz
     */
    override fun postFailedSignal(t: Throwable) {
        statusFlow.value = BackgroundWorkStatus(false, "", "")
        resultFlow.value = BackgroundWorkResult.Error(t)
    }

    /**
     * Reports whether in-process generation is active.
     *
     * @author Dmitriy Moroz
     */
    override fun hasActiveTasks(): Boolean = statusFlow.value.running
}
