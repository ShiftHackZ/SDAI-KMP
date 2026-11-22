package com.shifthackz.aisdv1.feature.appupdate

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises Play eligibility and asynchronous completion without claiming store installation QA.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayAppUpdateGatewayTest {

    @Test
    fun `given Play update discovery, an immediate update is available, expected a native offer`() =
        runTest {
            // Given
            val fixture = PlayFixture()
            val info = fixture.info(UpdateAvailability.UPDATE_AVAILABLE, true)
            val pending = async { fixture.gateway.check() }
            runCurrent()

            // When
            fixture.success.captured.onSuccess(info)
            val result = pending.await()

            // Then
            assertEquals(AppUpdate("221", null), result)
        }

    @Test
    fun `given Play update discovery, an interrupted update is in progress, expected a native offer`() =
        runTest {
            // Given
            val fixture = PlayFixture()
            val info = fixture.info(UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS, true)
            val pending = async { fixture.gateway.check() }
            runCurrent()

            // When
            fixture.success.captured.onSuccess(info)
            val result = pending.await()

            // Then
            assertEquals(AppUpdate("221", null), result)
        }

    @Test
    fun `given Play update discovery, the app is current, expected no offer`() = runTest {
        // Given
        val fixture = PlayFixture()
        val info = fixture.info(UpdateAvailability.UPDATE_NOT_AVAILABLE, true)
        val pending = async { fixture.gateway.check() }
        runCurrent()

        // When
        fixture.success.captured.onSuccess(info)
        val result = pending.await()

        // Then
        assertNull(result)
    }

    @Test
    fun `given Play update discovery, availability is unknown, expected no offer`() = runTest {
        // Given
        val fixture = PlayFixture()
        val info = fixture.info(UpdateAvailability.UNKNOWN, true)
        val pending = async { fixture.gateway.check() }
        runCurrent()

        // When
        fixture.success.captured.onSuccess(info)
        val result = pending.await()

        // Then
        assertNull(result)
    }

    @Test
    fun `given Play update discovery, immediate updates are disallowed, expected no offer`() =
        runTest {
            // Given
            val fixture = PlayFixture()
            val info = fixture.info(UpdateAvailability.UPDATE_AVAILABLE, false)
            val pending = async { fixture.gateway.check() }
            runCurrent()

            // When
            fixture.success.captured.onSuccess(info)
            val result = pending.await()

            // Then
            assertNull(result)
        }

    @Test
    fun `given Play update discovery, an interrupted update is no longer allowed, expected no offer`() =
        runTest {
            // Given
            val fixture = PlayFixture()
            val info =
                fixture.info(UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS, false)
            val pending = async { fixture.gateway.check() }
            runCurrent()

            // When
            fixture.success.captured.onSuccess(info)
            val result = pending.await()

            // Then
            assertNull(result)
        }

    @Test
    fun `given Play update discovery, only flexible updates are allowed, expected no offer`() =
        runTest {
            // Given
            val fixture = PlayFixture()
            val info = fixture.info(UpdateAvailability.UPDATE_AVAILABLE, false)
            val pending = async { fixture.gateway.check() }
            runCurrent()

            // When
            fixture.success.captured.onSuccess(info)
            val result = pending.await()

            // Then
            assertNull(result)
        }

    @Test
    fun `given Play update discovery, the SDK reports a failure, expected the original failure propagates`() =
        runTest {
            // Given
            val fixture = PlayFixture()
            val failure = IOException("Play unavailable")
            val pending = async { runCatching { fixture.gateway.check() }.exceptionOrNull() }
            runCurrent()

            // When
            fixture.failure.captured.onFailure(failure)
            val result = pending.await()

            // Then
            assertTrue(generateSequence(result) { it.cause }.any { it === failure })
        }

    @Test
    fun `given Play update discovery, success arrives after host cancellation, expected the cancelled job remains cancelled`() =
        runTest {
            // Given
            val fixture = PlayFixture()
            val pending = async { fixture.gateway.check() }
            runCurrent()
            pending.cancel()
            pending.join()

            // When
            fixture.success.captured.onSuccess(
                fixture.info(
                    UpdateAvailability.UPDATE_AVAILABLE,
                    true
                )
            )

            // Then
            assertTrue(pending.isCancelled)
        }

    @Test
    fun `given Play update discovery, failure arrives after host cancellation, expected the cancelled job remains cancelled`() =
        runTest {
            // Given
            val fixture = PlayFixture()
            val pending = async { fixture.gateway.check() }
            runCurrent()
            pending.cancel()
            pending.join()

            // When
            fixture.failure.captured.onFailure(IOException("late failure"))

            // Then
            assertTrue(pending.isCancelled)
        }

    /**
     * Captures SDK callbacks so each scenario controls real gateway continuation completion.
     *
     * @author Dmitriy Moroz
     */
    private class PlayFixture {
        val success: CapturingSlot<OnSuccessListener<AppUpdateInfo>> = slot()
        val failure: CapturingSlot<OnFailureListener> = slot()
        private val task = mockk<Task<AppUpdateInfo>>()
        private val manager = mockk<AppUpdateManager>()
        val gateway = PlayAppUpdateGateway(manager)

        init {
            every { manager.appUpdateInfo } returns task
            every { task.addOnSuccessListener(capture(success)) } returns task
            every { task.addOnFailureListener(capture(failure)) } returns task
        }

        /**
         * Provides only the SDK fields the gateway consumes.
         *
         * @param availability Play-owned eligibility state.
         * @param isImmediateAllowed Whether Play allows this installation to use immediate updates.
         * @author Dmitriy Moroz
         */
        fun info(availability: Int, isImmediateAllowed: Boolean): AppUpdateInfo = mockk {
            every { updateAvailability() } returns availability
            every { isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) } returns isImmediateAllowed
            every { availableVersionCode() } returns 221
        }
    }
}
