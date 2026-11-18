package com.shifthackz.aisdv1.work.task

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelsUseCase
import com.shifthackz.aisdv1.work.core.NotificationWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Runs catalog downloads serially as long-running foreground work with durable UI progress.
 * @param context Application context owned by WorkManager.
 * @param parameters Persisted provider identity and worker lifetime.
 * @param notifications Existing app notification channel and appearance.
 * @param activityIntentProvider Opens the app from the notification.
 * @param downloadModels Sequential domain download policy.
 * @author Dmitriy Moroz
 */
internal class DownloadModelsTask(
    context: Context,
    parameters: WorkerParameters,
    notifications: PushNotificationManager,
    activityIntentProvider: ActivityIntentProvider,
    private val downloadModels: DownloadModelsUseCase,
) : NotificationWorker(context, parameters, notifications, activityIntentProvider) {
    override val notificationId = 0x10000000 or (id.hashCode() and 0x0fffffff)
    override val genericNotificationId = 0x20000000 or (id.hashCode() and 0x0fffffff)

    /**
     * Downloads the captured selection and posts the generation-style result notification.
     * Cancellation propagates without publishing a success or failure notification.
     * @author Dmitriy Moroz
     */
    override suspend fun doWork(): Result {
        val type = LocalAiModel.Type.parse(inputData.getString("type"))
        val request =
            ModelDownloadRequest(type, inputData.getString("model"), inputData.getString("url"))
        var latest = ModelDownloadBatch(type, modelId = request.modelId, isBatch = request.isBatch)
        try {
            downloadModels(request).collect { snapshot ->
                latest = snapshot
                setProgress(snapshot.toData())
                setForegroundNotification(
                    title = Localization.string(if (request.isBatch) "download_all" else "download"),
                    body = snapshot.modelName.orEmpty(),
                    progress = snapshot.totalProgress to 100,
                    silent = true,
                    canCancel = true,
                    cancelText = Localization.string(if (request.isBatch) "download_cancel_all" else "cancel"),
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            latest = latest.copy(progress = DownloadState.Error(failure), isRunning = false)
        }
        currentCoroutineContext().ensureActive()
        if (isStopped) throw CancellationException("Model download was stopped")
        val hasFailed = latest.progress is DownloadState.Error
        showGenericNotification(
            Localization.string(if (hasFailed) "notification_download_fail_title" else "notification_download_finish_title"),
            Localization.string("notification_fail_sub_title"),
        )
        return if (hasFailed) Result.failure(latest.toData()) else Result.success(latest.toData())
    }

    private fun ModelDownloadBatch.toData() = workDataOf(
        "type" to type.key,
        "model" to modelId,
        "name" to modelName,
        "total_count" to totalModelCount,
        "percent" to ((progress as? DownloadState.Downloading)?.percent ?: 0),
        "path" to (progress as? DownloadState.Complete)?.filePath,
        "error" to (progress as? DownloadState.Error)?.error?.let { it.message ?: it.toString() }
            ?.take(1000),
        "completed" to completedIds.toTypedArray(),
    )

    companion object {
        const val WORK_NAME = "download_all_models"
    }
}
