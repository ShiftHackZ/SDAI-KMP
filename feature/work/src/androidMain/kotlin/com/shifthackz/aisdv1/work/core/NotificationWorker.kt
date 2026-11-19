package com.shifthackz.aisdv1.work.core

import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.extensions.isAppInForeground
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Shares app navigation and foreground notification appearance across long-running workers.
 *
 * @param context Application context supplied by WorkManager.
 * @param workerParameters Persisted request input and cancellation lifetime.
 * @param pushNotificationManager Creates notifications using the application channel and branding.
 * @param activityIntentProvider Opens the application when the notification is tapped.
 *
 * @author Dmitriy Moroz
 */
internal abstract class NotificationWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val pushNotificationManager: PushNotificationManager,
    private val activityIntentProvider: ActivityIntentProvider,
) : CoroutineWorker(context, workerParameters) {

    /**
     * Stable notification identity used while this worker runs in the foreground.
     *
     * @author Dmitriy Moroz
     */
    abstract val notificationId: Int

    /**
     * Separate notification identity for dismissible completion or failure messages.
     *
     * @author Dmitriy Moroz
     */
    abstract val genericNotificationId: Int

    /**
     * Posts a dismissible result message whose tap opens the application.
     *
     * @param text Localized notification title.
     * @param body Optional detail shown below the title.
     * @author Dmitriy Moroz
     */
    fun showGenericNotification(text: String, body: String?) {
        pushNotificationManager.createNotificationChannel()
        val notification = pushNotificationManager.createNotification(text, body) {
            setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    activityIntentProvider().also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            setVibrate(longArrayOf(10000L, 2000L, 1000L))
            setAutoCancel(true)
            setTicker(text)
            setSilent(applicationContext.isAppInForeground())
        }
        pushNotificationManager.show(genericNotificationId, notification)
    }

    /**
     * Updates the ongoing foreground notification and its optional worker cancellation action.
     *
     * @param title Localized operation title.
     * @param body Current operation detail, such as a model name.
     * @param subText Optional compact status rendered by the system.
     * @param progress Completed units and total units; null displays an indeterminate bar.
     * @param silent Whether progress updates suppress sound and vibration.
     * @param canCancel Whether to expose cancellation for this WorkManager request.
     * @param cancelText Localized label describing what the cancellation action stops.
     *
     * @author Dmitriy Moroz
     */
    fun setForegroundNotification(
        title: String,
        body: String,
        subText: String? = null,
        progress: Pair<Int, Int>? = null,
        silent: Boolean = false,
        canCancel: Boolean = false,
        cancelText: String = Localization.string("cancel"),
    ) {
        pushNotificationManager.createNotificationChannel()
        val notification = pushNotificationManager.createProgressNotification(title, body) {
            setSilent(silent)
            setTicker(title)
            setOngoing(true)

            progress?.let { (step, max) ->
                setProgress(max, step, false)
            } ?: run {
                setProgress(0, 0, true)
            }
            subText?.let { setSubText(it) }

            if (canCancel) {
                val intent = WorkManager
                    .getInstance(applicationContext)
                    .createCancelPendingIntent(id)
                addAction(R.drawable.ic_menu_close_clear_cancel, cancelText, intent)
            }

            setAutoCancel(false)
            setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

            setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    activityIntentProvider().also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }

        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
        setForegroundAsync(info)
    }
}
