package com.shifthackz.aisdv1.work.core

import android.content.Context
import androidx.work.WorkerParameters
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptWorkerGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Exercises the production worker's captured-source subscriptions and exact owner cancellation.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MnnCoreGenerationWorkerTest {
    private val gateway = mockk<NativeGenerationGateway>()
    private val observer = mockk<BackgroundWorkObserver>()
    private val interrupt = mockk<InterruptWorkerGenerationUseCase>()
    private val preferences =
        mockk<PreferenceManager> { every { source } returns ServerSource.LOCAL_MNN }
    private val id = UUID.randomUUID()

    @Test
    fun `given MNN worker, progress listening starts, expected MNN stage and percentage subscriptions only`() =
        runTest {
            // Given
            val stages = CompletableDeferred<NativeGenerationEngine>()
            val progress = CompletableDeferred<NativeGenerationEngine>()
            every { gateway.observeStage(any()) } answers {
                val engine =
                    firstArg<NativeGenerationEngine>(); flow { stages.complete(engine); awaitCancellation() }
            }
            every { gateway.observeStatus(any()) } answers {
                val engine =
                    firstArg<NativeGenerationEngine>(); flow { progress.complete(engine); awaitCancellation() }
            }
            val worker = worker()

            // When
            worker.startListening()
            val stageEngine = stages.await()
            val progressEngine = progress.await()
            worker.stopListening()

            // Then
            assertEquals(NativeGenerationEngine.MNN, stageEngine)
            assertEquals(NativeGenerationEngine.MNN, progressEngine)
            verify(exactly = 1) {
                gateway.observeStage(NativeGenerationEngine.MNN)
                gateway.observeStatus(NativeGenerationEngine.MNN)
            }
        }

    @Test
    fun `given MNN worker, preferences change and observer owner is stale, expected captured source and exact worker cancellation`() =
        runTest {
            // Given
            every { observer.publishForOwner(any(), any()) } returns false
            coEvery { interrupt(ServerSource.LOCAL_MNN, id.toString()) } returns Unit
            val worker = worker()
            every { preferences.source } returns ServerSource.AUTOMATIC1111

            // When
            worker.cancelCaptured()

            // Then
            coVerify(exactly = 1) { interrupt(ServerSource.LOCAL_MNN, id.toString()) }
            coVerify(exactly = 0) { interrupt(ServerSource.AUTOMATIC1111, any()) }
        }

    /**
     * Supplies actual worker identity and context while external notification collaborators remain isolated.
     *
     * @author Dmitriy Moroz
     */
    private fun worker(): TestWorker = TestWorker(
        RuntimeEnvironment.getApplication(),
        mockk(relaxed = true) { every { this@mockk.id } returns this@MnnCoreGenerationWorkerTest.id },
        mockk(),
        mockk(),
        preferences,
        gateway,
        observer,
        mockk(),
        mockk(),
        interrupt
    )
}

/**
 * Makes protected production lifecycle entries callable without duplicating their implementation.
 *
 * @param context Actual Robolectric application context.
 * @param parameters Captured WorkManager identity.
 * @param notifications Notification boundary unused by non-emitting subscription fixtures.
 * @param activityIntent Notification navigation boundary.
 * @param preferences Supplies the initial immutable source.
 * @param gateway Production worker's native progress boundary.
 * @param observer Owner-scoped application status sink.
 * @param horde Unused remote provider progress.
 * @param easyDiffusion Unused remote provider progress.
 * @param interrupt Captured-source cancellation operation.
 *
 * @author Dmitriy Moroz
 */
private class TestWorker(
    context: Context,
    parameters: WorkerParameters,
    notifications: PushNotificationManager,
    activityIntent: ActivityIntentProvider,
    preferences: PreferenceManager,
    gateway: NativeGenerationGateway,
    observer: BackgroundWorkObserver,
    horde: ObserveHordeProcessStatusUseCase,
    easyDiffusion: ObserveEasyDiffusionProgressUseCase,
    interrupt: InterruptWorkerGenerationUseCase
) : CoreGenerationWorker(
    context,
    parameters,
    notifications,
    activityIntent,
    preferences,
    gateway,
    observer,
    horde,
    easyDiffusion,
    interrupt
) {
    override val notificationId = 1
    override val genericNotificationId = 2
    override suspend fun doWork(): Result = Result.success()
    fun startListening() = listenSourceStatus()
    fun stopListening() = clearSubscriptions()
    suspend fun cancelCaptured() = handleCancellation()
}
