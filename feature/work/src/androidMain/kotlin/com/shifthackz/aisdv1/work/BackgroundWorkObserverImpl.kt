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
     * Exposes the `stateFlow` value used by the SDAI background work feature layer.
     *
     * @author Dmitriy Moroz
     */
    private val stateFlow = MutableStateFlow(false)

    /**
     * Exposes the `messageFlow` value used by the SDAI background work feature layer.
     *
     * @author Dmitriy Moroz
     */
    private val messageFlow = MutableStateFlow("" to "")

    /**
     * Exposes the `resultFlow` value used by the SDAI background work feature layer.
     *
     * @author Dmitriy Moroz
     */
    private val resultFlow = MutableStateFlow<BackgroundWorkResult>(BackgroundWorkResult.None)

    /**
     * Loads SDAI data through `observeStatus`.
     *
     * @return Result produced by `observeStatus`.
     * @author Dmitriy Moroz
     */
    override fun observeStatus(): Flow<BackgroundWorkStatus> {
        refreshStatus()
        return combine(stateFlow, messageFlow) { running, (title, subTitle) ->
            BackgroundWorkStatus(running, title, subTitle)
        }
    }

    /**
     * Loads SDAI data through `observeResult`.
     *
     * @return Result produced by `observeResult`.
     * @author Dmitriy Moroz
     */
    override fun observeResult(): Flow<BackgroundWorkResult> = resultFlow

    /**
     * Executes the `dismissResult` step in the SDAI background work feature layer.
     *
     * @author Dmitriy Moroz
     */
    override fun dismissResult() {
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Executes the `refreshStatus` step in the SDAI background work feature layer.
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
     * Executes the `postStatusMessage` step in the SDAI background work feature layer.
     *
     * @param title title value consumed by the API.
     * @param subTitle sub title value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun postStatusMessage(title: String, subTitle: String) {
        stateFlow.value = true
        messageFlow.value = title to subTitle
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Executes the `postSuccessSignal` step in the SDAI background work feature layer.
     *
     * @param result result value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun postSuccessSignal(result: List<AiGenerationResult>) {
        hasOwnerTerminated = true
        stateFlow.value = false
        messageFlow.value = "" to ""
        resultFlow.value = BackgroundWorkResult.Success(result)
    }

    /**
     * Executes the `postCancelSignal` step in the SDAI background work feature layer.
     *
     * @author Dmitriy Moroz
     */
    override fun postCancelSignal() {
        hasOwnerTerminated = true
        stateFlow.value = false
        messageFlow.value = "" to ""
        resultFlow.value = BackgroundWorkResult.None
    }

    /**
     * Executes the `postFailedSignal` step in the SDAI background work feature layer.
     *
     * @param t t value consumed by the API.
     * @author Dmitriy Moroz
     */
    override fun postFailedSignal(t: Throwable) {
        hasOwnerTerminated = true
        stateFlow.value = false
        messageFlow.value = "" to ""
        resultFlow.value = BackgroundWorkResult.Error(t)
    }

    /**
     * Executes the `hasActiveTasks` step in the SDAI background work feature layer.
     *
     * @return Result produced by `hasActiveTasks`.
     * @author Dmitriy Moroz
     */
    override fun hasActiveTasks(): Boolean {
        return stateFlow.value
    }
}
