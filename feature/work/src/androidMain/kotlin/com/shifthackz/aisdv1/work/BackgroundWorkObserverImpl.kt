package com.shifthackz.aisdv1.work

import androidx.work.WorkInfo
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkStatus
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.work.di.WorkManagerProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Serializes worker-owned UI signals and queries WorkManager away from main.
 *
 * @param workManager Injected WorkManager bridge.
 * @param scope Application-owned IO scope for status refreshes.
 *
 * @author Dmitriy Moroz
 */
internal class BackgroundWorkObserverImpl(
    private val workManager: WorkManagerProvider,
    private val scope: CoroutineScope,
) : BackgroundWorkObserver {
    private val ownerLock = Any()
    private var ownerId: String? = null

    @Volatile
    private var hasOwnerTerminated = false

    /**
     * Selects the newly scheduled worker as the owner of subsequent UI signals.
     *
     * @param ownerId New scheduled work UUID.
     * @author Dmitriy Moroz
     */
    override fun claimOwner(ownerId: String) =
        synchronized(ownerLock) { this.ownerId = ownerId; hasOwnerTerminated = false }

    /**
     * Publishes synchronously only for the current owner, claiming an empty owner slot if needed.
     * Returns false when a different worker already owns the observer.
     *
     * @param ownerId Publishing work UUID.
     * @param publish Synchronous observer transition.
     * @author Dmitriy Moroz
     */
    override fun publishForOwner(ownerId: String, publish: () -> Unit): Boolean =
        synchronized(ownerLock) {
            if (this.ownerId == null) this.ownerId = ownerId
            if (this.ownerId != ownerId) return@synchronized false
            publish()
            true
        }


    /**
     * Whether scheduled or running generation should remain visible.
     *
     * @author Dmitriy Moroz
     */
    private val stateFlow = MutableStateFlow(false)

    /**
     * Latest localized stage and measured completion, published together.
     *
     * @author Dmitriy Moroz
     */
    private val messageFlow = MutableStateFlow(BackgroundWorkStatus(false, "", ""))

    /**
     * Latest generation outcome retained until acknowledged.
     *
     * @author Dmitriy Moroz
     */
    private val resultFlow = MutableStateFlow<BackgroundWorkResult>(BackgroundWorkResult.None)

    /**
     * Observes active work headings and measured progress.
     *
     * @return Result produced by `observeStatus`.
     * @author Dmitriy Moroz
     */
    override fun observeStatus(): Flow<BackgroundWorkStatus> {
        refreshStatus()
        return combine(stateFlow, messageFlow) { running, message ->
            message.copy(running = running)
        }
    }

    /**
     * Observes the retained generation outcome until acknowledgment.
     *
     * @return Result produced by `observeResult`.
     * @author Dmitriy Moroz
     */
    override fun observeResult(): Flow<BackgroundWorkResult> = resultFlow

    /**
     * Clears the acknowledged generation outcome.
     *
     * @author Dmitriy Moroz
     */
    override fun dismissResult() {
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Refreshes running state from scheduled generation work.
     *
     * @author Dmitriy Moroz
     */
    override fun refreshStatus() {
        val owner = synchronized(ownerLock) { ownerId }
        scope.launch {
            val workInfos = workManager().getWorkInfosByTag(Constants.TAG_GENERATION).get()
            synchronized(ownerLock) {
                if (owner == ownerId && !hasOwnerTerminated) stateFlow.value =
                    workInfos.any { !it.state.isFinished }
            }
        }
    }

    /**
     * Publishes stage and completion together so subscribers never mix different measurements.
     *
     * @param title Localized operation heading.
     * @param subTitle Stage description or sampling counters.
     * @param percentage Completion fraction expressed as a percentage, or null when unknown.
     * @author Dmitriy Moroz
     */
    override fun postProgressMessage(title: String, subTitle: String, percentage: Int?) {
        stateFlow.value = true
        messageFlow.value =
            BackgroundWorkStatus(true, title, subTitle, percentage?.coerceIn(0, 100))
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Publishes an indeterminate active stage and clears the previous outcome.
     *
     * @param title Localized operation heading.
     * @param subTitle Localized stage description.
     * @author Dmitriy Moroz
     */
    override fun postStatusMessage(title: String, subTitle: String) {
        stateFlow.value = true
        messageFlow.value = BackgroundWorkStatus(true, title, subTitle)
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Ends active work and retains successful results for display.
     *
     * @param result Completed images retained for the result UI.
     * @author Dmitriy Moroz
     */
    override fun postSuccessSignal(result: List<AiGenerationResult>) {
        hasOwnerTerminated = true
        stateFlow.value = false
        messageFlow.value = BackgroundWorkStatus(false, "", "")
        resultFlow.value = BackgroundWorkResult.Success(result)
    }

    /**
     * Ends active work without publishing a successful result.
     *
     * @author Dmitriy Moroz
     */
    override fun postCancelSignal() {
        hasOwnerTerminated = true
        stateFlow.value = false
        messageFlow.value = BackgroundWorkStatus(false, "", "")
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Ends active work and retains its failure for display.
     *
     * @param t Failure retained for the error UI.
     * @author Dmitriy Moroz
     */
    override fun postFailedSignal(t: Throwable) {
        hasOwnerTerminated = true
        stateFlow.value = false
        messageFlow.value = BackgroundWorkStatus(false, "", "")
        resultFlow.value = BackgroundWorkResult.Error(t)
    }

    /**
     * Reports whether generation is scheduled or running.
     *
     * @return Result produced by `hasActiveTasks`.
     * @author Dmitriy Moroz
     */
    override fun hasActiveTasks(): Boolean {
        return stateFlow.value
    }
}
