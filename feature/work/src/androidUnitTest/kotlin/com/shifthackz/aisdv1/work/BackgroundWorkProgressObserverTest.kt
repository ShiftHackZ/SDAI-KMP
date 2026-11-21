package com.shifthackz.aisdv1.work

import androidx.work.WorkManager
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkStatus
import com.shifthackz.aisdv1.work.di.WorkManagerProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Verifies atomic percentage publication and exact worker ownership in the production background observer. @author Dmitriy Moroz */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundWorkProgressObserverTest {
    private val workManager = mockk<WorkManager> {
        every { getWorkInfosByTag(any()) } returns mockk {
            every { get() } returns emptyList()
        }
    }
    private val provider = WorkManagerProvider { workManager }

    @Test
    fun `given background progress observer, normal measurement is posted, expected bounded status and percentage published together`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            // When
            observer.postProgressMessage("Running", "Denoising", 35)
            val actual = flow.first()
            // Then
            assertEquals(BackgroundWorkStatus(true, "Running", "Denoising", 35), actual)
        }

    @Test
    fun `given background progress observer, negative measurement is posted, expected bounded status and percentage published together`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            // When
            observer.postProgressMessage("Running", "Denoising", -5)
            val actual = flow.first()
            // Then
            assertEquals(BackgroundWorkStatus(true, "Running", "Denoising", 0), actual)
        }

    @Test
    fun `given background progress observer, overshoot measurement is posted, expected bounded status and percentage published together`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            // When
            observer.postProgressMessage("Running", "Denoising", 150)
            val actual = flow.first()
            // Then
            assertEquals(BackgroundWorkStatus(true, "Running", "Denoising", 100), actual)
        }

    @Test
    fun `given background progress observer, missing measurement is posted, expected bounded status and percentage published together`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            // When
            observer.postProgressMessage("Running", "Denoising", null)
            val actual = flow.first()
            // Then
            assertEquals(BackgroundWorkStatus(true, "Running", "Denoising", null), actual)
        }

    @Test
    fun `given background progress observer, indeterminate stage replaces measurement, expected percentage removed`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            observer.postProgressMessage("Running", "Denoising", 40)
            // When
            observer.postStatusMessage("Running", "Loading")
            // Then
            assertNull(flow.first().percentage)
            assertEquals("Loading", flow.first().statusSubTitle)
        }

    @Test
    fun `given background progress observer, replaced worker publishes measurement, expected stale owner rejected`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            observer.claimOwner("old")
            observer.claimOwner("current")
            observer.publishForOwner("current") {
                observer.postProgressMessage(
                    "Running",
                    "Denoising",
                    20
                )
            }
            // When
            val isAccepted = observer.publishForOwner("old") {
                observer.postProgressMessage(
                    "Wrong",
                    "Stale",
                    90
                )
            }
            // Then
            assertFalse(isAccepted)
            assertEquals(BackgroundWorkStatus(true, "Running", "Denoising", 20), flow.first())
        }

    @Test
    fun `given background progress observer, completion occurs, expected terminal status without stale percentage`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            observer.postProgressMessage("Running", "Denoising", 40)
            // When
            observer.postSuccessSignal(emptyList())
            // Then
            assertEquals(BackgroundWorkStatus(false, "", ""), flow.first())
        }

    @Test
    fun `given background progress observer, cancellation occurs, expected terminal status without stale percentage`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            observer.postProgressMessage("Running", "Denoising", 40)
            // When
            observer.postCancelSignal()
            // Then
            assertEquals(BackgroundWorkStatus(false, "", ""), flow.first())
        }

    @Test
    fun `given background progress observer, failure occurs, expected terminal status without stale percentage`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl(provider, backgroundScope)
            val flow = observer.observeStatus()
            runCurrent()
            observer.postProgressMessage("Running", "Denoising", 40)
            // When
            observer.postFailedSignal(IllegalStateException("failed"))
            // Then
            assertEquals(BackgroundWorkStatus(false, "", ""), flow.first())
        }

}
