@file:Suppress("MemberVisibilityCanBePrivate")

package com.shifthackz.aisdv1.work.core

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCase

import android.content.Context
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import kotlinx.coroutines.flow.combine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import androidx.work.WorkerParameters
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.core.localization.formatter.DurationFormatter
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptWorkerGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.shifthackz.aisdv1.core.localization.R as LocalizationR

/**
 * Shared foreground worker base for generation tasks.
 *
 * It owns cancellation, progress subscriptions, foreground notifications, and
 * background observer updates that are common to txt2img and img2img work.
 * Android Bonsai uses the same child lifecycle and owner-scoped cancellation as other native engines.
 *
 * @param context Worker application context.
 * @param workerParameters WorkManager identity and lifecycle parameters.
 * @param pushNotificationManager Publishes foreground and terminal notifications.
 * @param activityIntentProvider Opens the application from notifications.
 * @param preferenceManager Supplies the initial source for legacy work without a captured provider.
 * @param nativeGateway Observes and cancels isolated native attempts without loading engines.
 * @param backgroundWorkObserver Publishes state only for the current worker owner.
 * @param observeHordeProcessStatusUseCase Supplies remote Horde queue progress.
 * @param observeEasyDiffusionProgressUseCase Hot queued/sampling progress for this remote generation owner.
 * @param interruptWorkerGenerationUseCase Cancels the captured source for this exact worker.
 *
 * @author Dmitriy Moroz
 */
internal abstract class CoreGenerationWorker(
    context: Context,
    workerParameters: WorkerParameters,
    pushNotificationManager: PushNotificationManager,
    activityIntentProvider: ActivityIntentProvider,
    preferenceManager: PreferenceManager,
    private val nativeGateway: NativeGenerationGateway,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    private val observeEasyDiffusionProgressUseCase: ObserveEasyDiffusionProgressUseCase,
    private val interruptWorkerGenerationUseCase: InterruptWorkerGenerationUseCase,
) : NotificationWorker(
    context = context,
    workerParameters = workerParameters,
    pushNotificationManager = pushNotificationManager,
    activityIntentProvider = activityIntentProvider,
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    protected var source = preferenceManager.source

    /**
     * Stops observations and cancels the captured source; native cancellation requires this worker's UUID.
     *
     * @author Dmitriy Moroz
     */
    protected suspend fun handleCancellation() {
        clearSubscriptions()
        runCatching {
            if (source in setOf(
                    ServerSource.LOCAL_MNN,
                    ServerSource.LOCAL_MICROSOFT_ONNX,
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                    ServerSource.LOCAL_APPLE_BONSAI,
                    ServerSource.LOCAL_STABLE_DIFFUSION_CPP
                ) ||
                backgroundWorkObserver.publishForOwner(id.toString()) {}
            ) interruptWorkerGenerationUseCase(source, id.toString())
        }
            .onFailure(::errorLog)
        backgroundWorkObserver.publishForOwner(id.toString()) { backgroundWorkObserver.postCancelSignal() }
    }

    /**
     * Cancels worker status subscriptions so late progress cannot update completed work.
     *
     * @author Dmitriy Moroz
     */

    protected fun clearSubscriptions() {
        coroutineScope.coroutineContext.cancelChildren()
    }

    /**
     * Subscribes to the captured source, using child stages for every Android native engine.
     *
     * @author Dmitriy Moroz
     */
    protected fun listenSourceStatus() {
        when (source) {
            ServerSource.EASY_DIFFUSION -> listenEasyDiffusionStatus()
            ServerSource.HORDE -> listenHordeStatus()
            ServerSource.LOCAL_MNN -> listenNativeStatus(NativeGenerationEngine.MNN)
            ServerSource.LOCAL_MICROSOFT_ONNX -> listenNativeStatus(NativeGenerationEngine.ONNX)
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> listenNativeStatus(NativeGenerationEngine.SDCPP)
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> listenNativeStatus(NativeGenerationEngine.MEDIA_PIPE)
            ServerSource.LOCAL_APPLE_BONSAI -> listenNativeStatus(NativeGenerationEngine.BONSAI)
            else -> Unit
        }
    }

    /**
     * Publishes only this worker’s current remote progress to observer and foreground notification.
     *
     * @author Dmitriy Moroz
     */
    private fun listenEasyDiffusionStatus() {
        coroutineScope.launch {
            observeEasyDiffusionProgressUseCase().collect { status ->
                if (!status.isRunning || status.ownerId != id.toString()) return@collect
                val title =
                    applicationContext.getString(LocalizationR.string.notification_running_title)
                val subtitle = if (status.totalSteps > 0) applicationContext.getString(
                    LocalizationR.string.communicating_status_steps,
                    status.step.toString(),
                    status.totalSteps.toString()
                ) else applicationContext.getString(LocalizationR.string.srv_type_easy_diffusion)
                if (backgroundWorkObserver.publishForOwner(id.toString()) {
                        backgroundWorkObserver.postStatusMessage(title, subtitle)
                    }) setForegroundNotification(
                    title, subtitle, silent = true, canCancel = true,
                    progress = (status.step to status.totalSteps).takeIf { status.totalSteps > 0 })
            }
        }
    }

    /**
     * Observes only this worker’s native engine ownership and publishes stages or sampling progress.
     *
     * @param engine Native engine whose worker-owned progress should be observed.
     *
     * @author Dmitriy Moroz
     */

    private fun listenNativeStatus(engine: NativeGenerationEngine) {
        coroutineScope.launch {
            combine(
                nativeGateway.observeStage(engine),
                nativeGateway.observeStatus(engine)
            ) { stage, status -> stage to status }.collect { (stage, status) ->
                val title =
                    applicationContext.getString(LocalizationR.string.notification_running_title)
                val subtitle =
                    stageText(stage) + if (stage == NativeGenerationStage.DENOISING && status.total > 0) "\n" + applicationContext.getString(
                        LocalizationR.string.communicating_status_steps,
                        status.current.toString(),
                        status.total.toString()
                    ) else ""
                backgroundWorkObserver.publishForOwner(id.toString()) {
                    backgroundWorkObserver.postStatusMessage(
                        title,
                        subtitle
                    )
                }
                if (backgroundWorkObserver.publishForOwner(id.toString()) {}) setForegroundNotification(
                    title,
                    subtitle,
                    silent = true,
                    canCancel = true,
                    progress = (status.current to status.total).takeIf { stage == NativeGenerationStage.DENOISING && status.total > 0 })
            }
        }
    }

    /**
     * Maps a native lifecycle stage into the localized notification status.
     *
     * @param stage Native lifecycle stage to localize.
     *
     * @author Dmitriy Moroz
     */

    private fun stageText(stage: NativeGenerationStage): String = applicationContext.getString(
        when (stage) {
            NativeGenerationStage.PREPARING -> LocalizationR.string.native_generation_stage_preparing
            NativeGenerationStage.TOKENIZING -> LocalizationR.string.native_generation_stage_tokenizing
            NativeGenerationStage.DENOISING -> LocalizationR.string.native_generation_stage_denoising
            NativeGenerationStage.DECODING -> LocalizationR.string.native_generation_stage_decoding
            NativeGenerationStage.RUNNING -> LocalizationR.string.native_generation_stage_running
            NativeGenerationStage.WRITING_RESULT -> LocalizationR.string.native_generation_stage_writing_result
        }
    )

    /**
     * Publishes AI Horde queue position and wait estimates to active work status and notification.
     *
     * @author Dmitriy Moroz
     */

    protected fun listenHordeStatus() {
        coroutineScope.launch {
            observeHordeProcessStatusUseCase()
                .catch { t -> errorLog(t) }
                .collect { status ->
                    val title =
                        applicationContext.getString(LocalizationR.string.notification_running_title)
                    val subTitle = buildString {
                        appendLine(
                            applicationContext.getString(
                                LocalizationR.string.communicating_status_queue,
                                "${status.queuePosition}",
                            ),
                        )
                        append(
                            applicationContext.getString(
                                LocalizationR.string.communicating_wait_time,
                                DurationFormatter.formatDurationInSeconds(status.waitTimeSeconds),
                            ),
                        )
                    }
                    backgroundWorkObserver.publishForOwner(id.toString()) {
                        backgroundWorkObserver.postStatusMessage(
                            title,
                            subTitle
                        )
                    }
                    if (backgroundWorkObserver.publishForOwner(id.toString()) {}) setForegroundNotification(
                        title = title,
                        body = subTitle,
                        silent = true,
                        canCancel = true,
                    )
                }
        }
    }

    /**
     * Posts the start notification and initial running status only while this worker owns background state.
     *
     * @author Dmitriy Moroz
     */

    protected fun handleStart() {
        if (!backgroundWorkObserver.publishForOwner(id.toString()) {}) return
        val title = applicationContext.getString(LocalizationR.string.notification_started_title)
        val subTitle =
            applicationContext.getString(LocalizationR.string.notification_running_sub_title)
        showGenericNotification(title, subTitle)
        handleProcess()
    }

    /**
     * Publishes initial progress only while this worker owns background state.
     * Native sources start with PREPARING before child callbacks arrive.
     *
     * @author Dmitriy Moroz
     */
    protected fun handleProcess() {
        if (!backgroundWorkObserver.publishForOwner(id.toString()) {}) return
        val title = applicationContext.getString(LocalizationR.string.notification_running_title)
        val subTitle = if (source in setOf(
                ServerSource.LOCAL_MICROSOFT_ONNX,
                ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                ServerSource.LOCAL_APPLE_BONSAI,
                ServerSource.LOCAL_GOOGLE_MEDIA_PIPE
            )
        ) stageText(NativeGenerationStage.PREPARING) else applicationContext.getString(LocalizationR.string.notification_running_sub_title)
        backgroundWorkObserver.publishForOwner(id.toString()) {
            backgroundWorkObserver.postStatusMessage(
                title,
                subTitle
            )
        }
        setForegroundNotification(
            title = title,
            body = subTitle,
            canCancel = true,
        )
    }

    /**
     * Publishes generated results and replaces the running notification with a completion notification.
     *
     * @param result Successfully generated application results.
     *
     * @author Dmitriy Moroz
     */

    protected fun handleSuccess(result: List<AiGenerationResult>) {
        if (!backgroundWorkObserver.publishForOwner(id.toString()) {}) return
        backgroundWorkObserver.refreshStatus()
        backgroundWorkObserver.publishForOwner(id.toString()) {
            backgroundWorkObserver.postSuccessSignal(
                result
            )
        }
        val title = applicationContext.getString(LocalizationR.string.notification_finish_title)
        val subTitle =
            applicationContext.getString(LocalizationR.string.notification_finish_sub_title)
        showGenericNotification(title, subTitle)
    }

    /**
     * Publishes the generation failure and replaces the running notification with its error state.
     *
     * @param t Underlying generation or download failure.
     *
     * @author Dmitriy Moroz
     */

    protected fun handleError(t: Throwable) {
        if (!backgroundWorkObserver.publishForOwner(id.toString()) {}) return
        backgroundWorkObserver.publishForOwner(id.toString()) {
            backgroundWorkObserver.postFailedSignal(
                t
            )
        }
        val title = applicationContext.getString(LocalizationR.string.notification_fail_title)
        val subTitle =
            applicationContext.getString(LocalizationR.string.notification_fail_sub_title)
        showGenericNotification(title, subTitle)
    }
}
