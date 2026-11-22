package com.shifthackz.aisdv1.work.core

import android.R as AndroidR
import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.work.task.DownloadModelsTask
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Inspects real Android notification objects built by the helper shared with generation workers.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ModelDownloadNotificationTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val notifications = mockk<PushNotificationManager>(relaxed = true)
    private val foreground = slot<ForegroundInfo>()
    private val workManager = mockk<WorkManager>()
    private val ownerId = UUID.randomUUID()
    private lateinit var worker: DownloadModelsTask

    @Before
    fun initialize() {
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns workManager
        every { notifications.createProgressNotification(any(), any(), any()) } answers {
            buildNotification(firstArg(), secondArg(), thirdArg())
        }
        every { notifications.createNotification(any<String>(), any<String>(), any()) } answers {
            buildNotification(firstArg(), secondArg(), thirdArg())
        }
        val parameters = mockk<WorkerParameters>(relaxed = true) { every { id } returns ownerId }
        worker = spyk(
            DownloadModelsTask(
                context,
                parameters,
                notifications,
                ActivityIntentProvider { Intent(context, Activity::class.java) },
                mockk()
            )
        )
        every { worker.setForegroundAsync(capture(foreground)) } returns mockk()
        every { notifications.show(any(), any()) } just Runs
    }

    @After
    fun releaseWorkManager() {
        unmockkObject(WorkManager.Companion)
    }

    @Test
    fun `given batch progress notification, cancellation is enabled, expected aggregate bar and exact worker cancel action`() {
        // Given
        val cancel = PendingIntent.getBroadcast(
            context,
            1,
            Intent("cancel-download"),
            PendingIntent.FLAG_IMMUTABLE
        )
        every { workManager.createCancelPendingIntent(ownerId) } returns cancel
        // When
        worker.setForegroundNotification(
            "Download all",
            "Second model",
            progress = 37 to 100,
            canCancel = true,
            cancelText = "Cancel all downloads"
        )
        // Then
        val notification = foreground.captured.notification
        assertEquals(37, notification.extras.getInt(Notification.EXTRA_PROGRESS))
        assertEquals(100, notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX))
        assertFalse(notification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE))
        assertEquals("Cancel all downloads", notification.actions.single().title.toString())
        assertSame(cancel, notification.actions.single().actionIntent)
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        verify(exactly = 1) { workManager.createCancelPendingIntent(ownerId) }
    }

    @Test
    fun `given progress notification, cancellation is disabled, expected indeterminate bar without action`() {
        // Given
        val title = "Download"
        // When
        worker.setForegroundNotification(title, "Preparing")
        // Then
        val notification = foreground.captured.notification
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE))
        assertTrue(notification.actions.isNullOrEmpty())
        verify(exactly = 0) { workManager.createCancelPendingIntent(any()) }
    }

    @Test
    fun `given terminal download notification, result is published, expected dismissible app navigation matching generation helper`() {
        // Given
        val result = slot<Notification>()
        val resultId = worker.genericNotificationId
        every { notifications.show(resultId, capture(result)) } just Runs
        // When
        worker.showGenericNotification("Download complete 🤩", "Click here to open the application.")
        // Then
        assertEquals(
            "Download complete 🤩",
            result.captured.extras.getString(Notification.EXTRA_TITLE)
        )
        assertNotNull(result.captured.contentIntent)
        assertTrue(result.captured.flags and Notification.FLAG_AUTO_CANCEL != 0)
        assertFalse(result.captured.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertTrue(result.captured.actions.isNullOrEmpty())
    }

    /**
     * Applies the production customization block to an actual platform-compatible notification builder.
     *
     * @param title Notification title supplied by the worker.
     * @param body Notification detail supplied by the worker.
     * @param block Production helper's appearance, navigation and cancellation customization.
     * @author Dmitriy Moroz
     */
    private fun buildNotification(
        title: String,
        body: String?,
        block: NotificationCompat.Builder.() -> Unit
    ): Notification =
        NotificationCompat.Builder(context, "downloads")
            .setSmallIcon(AndroidR.drawable.ic_menu_save)
            .setContentTitle(title).setContentText(body).apply(block).build()
}
