package com.shifthackz.aisdv1.domain.feature.work

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Shares owner-scoped generation status and retained outcomes between workers and UI.
 *
 * @author Dmitriy Moroz
 */
interface BackgroundWorkObserver {
    /**
     * Selects the newest scheduled work owner before REPLACE can cancel its predecessor.
     *
     * @param ownerId Work UUID.
     * @author Dmitriy Moroz
     */
    fun claimOwner(ownerId: String) = Unit

    /**
     * Publishes signals atomically only for the current work owner.
     *
     * @param ownerId Work UUID.
     * @param publish Synchronous observer updates.
     * @author Dmitriy Moroz
     */
    fun publishForOwner(ownerId: String, publish: () -> Unit): Boolean {
        publish(); return true
    }

    /**
     * Observes active work headings and measured progress.
     *
     * @return Result produced by `observeStatus`.
     * @author Dmitriy Moroz
     */
    fun observeStatus(): Flow<BackgroundWorkStatus>

    /**
     * Observes the retained generation outcome until acknowledgment.
     *
     * @return Result produced by `observeResult`.
     * @author Dmitriy Moroz
     */
    fun observeResult(): Flow<BackgroundWorkResult>

    /**
     * Clears the acknowledged generation outcome.
     *
     * @author Dmitriy Moroz
     */
    fun dismissResult()

    /**
     * Refreshes running state from scheduled generation work.
     *
     * @author Dmitriy Moroz
     */
    fun refreshStatus()

    /**
     * Publishes an indeterminate active stage and clears the previous outcome.
     *
     * @param title Localized operation heading.
     * @param subTitle Localized stage description.
     * @author Dmitriy Moroz
     */
    fun postStatusMessage(title: String, subTitle: String)

    /**
     * Publishes measured generation progress, retaining null for queue/loading stages.
     * @param title Localized operation heading.
     * @param subTitle Stage description or sampling counters.
     * @param percentage Bounded completion, or null when unavailable.
     * @author Dmitriy Moroz
     */
    fun postProgressMessage(title: String, subTitle: String, percentage: Int?) {
        postStatusMessage(title, subTitle)
    }

    /**
     * Ends active work and retains successful results for display.
     *
     * @param result Completed images retained for the result UI.
     * @author Dmitriy Moroz
     */
    fun postSuccessSignal(result: List<AiGenerationResult>)

    /**
     * Ends active work without publishing a successful result.
     *
     * @author Dmitriy Moroz
     */
    fun postCancelSignal()

    /**
     * Ends active work and retains its failure for display.
     *
     * @param t Failure retained for the error UI.
     * @author Dmitriy Moroz
     */
    fun postFailedSignal(t: Throwable)

    /**
     * Reports whether generation is scheduled or running.
     *
     * @return Result produced by `hasActiveTasks`.
     * @author Dmitriy Moroz
     */
    fun hasActiveTasks(): Boolean
}

/**
 * Provides the `NoOpBackgroundWorkObserver` singleton used by the SDAI domain layer.
 *
 * @author Dmitriy Moroz
 */
object NoOpBackgroundWorkObserver : BackgroundWorkObserver {
    /**
     * Observes active work headings and measured progress.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus(): Flow<BackgroundWorkStatus> =
        flowOf(BackgroundWorkStatus(false, "", ""))

    /**
     * Observes the retained generation outcome until acknowledgment.
     *
     * @author Dmitriy Moroz
     */
    override fun observeResult(): Flow<BackgroundWorkResult> = flowOf(BackgroundWorkResult.None)

    /**
     * Clears the acknowledged generation outcome.
     *
     * @author Dmitriy Moroz
     */
    override fun dismissResult() = Unit

    /**
     * Refreshes running state from scheduled generation work.
     *
     * @author Dmitriy Moroz
     */
    override fun refreshStatus() = Unit

    /**
     * Publishes an indeterminate active stage and clears the previous outcome.
     *
     * @param title Localized operation heading.
     * @param subTitle Localized stage description.
     * @author Dmitriy Moroz
     */
    override fun postStatusMessage(title: String, subTitle: String) = Unit

    /**
     * Ends active work and retains successful results for display.
     *
     * @param result Completed images retained for the result UI.
     * @author Dmitriy Moroz
     */
    override fun postSuccessSignal(result: List<AiGenerationResult>) = Unit

    /**
     * Ends active work without publishing a successful result.
     *
     * @author Dmitriy Moroz
     */
    override fun postCancelSignal() = Unit

    /**
     * Ends active work and retains its failure for display.
     *
     * @param t Failure retained for the error UI.
     * @author Dmitriy Moroz
     */
    override fun postFailedSignal(t: Throwable) = Unit

    /**
     * Reports whether generation is scheduled or running.
     *
     * @return Result produced by `hasActiveTasks`.
     * @author Dmitriy Moroz
     */
    override fun hasActiveTasks(): Boolean = false
}
