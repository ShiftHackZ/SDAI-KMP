package com.shifthackz.aisdv1.presentation.appupdate

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.IOException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Checks native Play launch ownership and late SDK callbacks using the real composable lifecycle.
 * The SDK is isolated here; actual store consent and installation remain device QA.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DistributionAppUpdateHostTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val manager = mockk<AppUpdateManager>()
    private val task = mockk<Task<AppUpdateInfo>>()
    private val successes = mutableListOf<OnSuccessListener<AppUpdateInfo>>()
    private val failures = mutableListOf<OnFailureListener>()

    @Before
    fun installPlayBoundary() {
        mockkStatic(AppUpdateManagerFactory::class)
        every { AppUpdateManagerFactory.create(any()) } returns manager
        every { manager.appUpdateInfo } returns task
        every { task.addOnSuccessListener(capture(successes)) } returns task
        every { task.addOnFailureListener(capture(failures)) } returns task
        every {
            manager.startUpdateFlowForResult(
                any<AppUpdateInfo>(),
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                any<AppUpdateOptions>()
            )
        } returns true
    }

    @After
    fun releasePlayBoundary() {
        unmockkStatic(AppUpdateManagerFactory::class)
    }

    @Test
    fun `given native Play host, an eligible update is returned, expected one immediate SDK launch`() {
        // Given
        var finished = 0
        compose.setContent { DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()

        // When
        val info = info(UpdateAvailability.UPDATE_AVAILABLE)
        compose.runOnIdle { successes.toList().forEach { it.onSuccess(info) } }

        // Then
        verify(exactly = 1) {
            manager.startUpdateFlowForResult(
                info,
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                match { it.appUpdateType() == AppUpdateType.IMMEDIATE })
        }
        assertEquals(0, finished)
    }

    @Test
    fun `given native Play host, an interrupted update is returned, expected one immediate SDK launch`() {
        // Given
        var finished = 0
        compose.setContent { DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()

        // When
        val info = info(UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
        compose.runOnIdle { successes.toList().forEach { it.onSuccess(info) } }

        // Then
        verify(exactly = 1) {
            manager.startUpdateFlowForResult(
                info,
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                match { it.appUpdateType() == AppUpdateType.IMMEDIATE })
        }
        assertEquals(0, finished)
    }

    @Test
    fun `given native Play host, the app is current, expected notification consumption without SDK launch`() {
        // Given
        var finished = 0
        compose.setContent { DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()

        // When
        compose.runOnIdle {
            successes.toList()
                .forEach { it.onSuccess(info(UpdateAvailability.UPDATE_NOT_AVAILABLE, true)) }
        }

        // Then
        verify(exactly = 0) {
            manager.startUpdateFlowForResult(
                any<AppUpdateInfo>(),
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                any<AppUpdateOptions>()
            )
        }
        assertEquals(1, finished)
    }

    @Test
    fun `given native Play host, availability is unknown, expected notification consumption without SDK launch`() {
        // Given
        var finished = 0
        compose.setContent { DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()

        // When
        compose.runOnIdle {
            successes.toList().forEach { it.onSuccess(info(UpdateAvailability.UNKNOWN, true)) }
        }

        // Then
        verify(exactly = 0) {
            manager.startUpdateFlowForResult(
                any<AppUpdateInfo>(),
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                any<AppUpdateOptions>()
            )
        }
        assertEquals(1, finished)
    }

    @Test
    fun `given native Play host, immediate updates are disallowed, expected notification consumption without SDK launch`() {
        // Given
        var finished = 0
        compose.setContent { DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()

        // When
        compose.runOnIdle {
            successes.toList()
                .forEach { it.onSuccess(info(UpdateAvailability.UPDATE_AVAILABLE, false)) }
        }

        // Then
        verify(exactly = 0) {
            manager.startUpdateFlowForResult(
                any<AppUpdateInfo>(),
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                any<AppUpdateOptions>()
            )
        }
        assertEquals(1, finished)
    }

    @Test
    fun `given native Play host, the SDK declines to start, expected the notification is consumed`() {
        // Given
        every {
            manager.startUpdateFlowForResult(
                any<AppUpdateInfo>(),
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                any<AppUpdateOptions>()
            )
        } returns false
        var finished = 0
        compose.setContent { DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()

        // When
        compose.runOnIdle {
            successes.toList().forEach { it.onSuccess(info(UpdateAvailability.UPDATE_AVAILABLE)) }
        }

        // Then
        assertEquals(1, finished)
    }

    @Test
    fun `given native Play host, the SDK throws while starting, expected the notification is consumed`() {
        // Given
        every {
            manager.startUpdateFlowForResult(
                any<AppUpdateInfo>(),
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                any<AppUpdateOptions>()
            )
        } throws IllegalStateException("SDK unavailable")
        var finished = 0
        compose.setContent { DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()

        // When
        compose.runOnIdle {
            successes.toList().forEach { it.onSuccess(info(UpdateAvailability.UPDATE_AVAILABLE)) }
        }

        // Then
        assertEquals(1, finished)
    }

    @Test
    fun `given native Play host, availability fails, expected the notification is consumed`() {
        // Given
        var finished = 0
        compose.setContent { DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()

        // When
        compose.runOnIdle { failures.first().onFailure(IOException("offline")) }

        // Then
        assertEquals(1, finished)
    }

    @Test
    fun `given native Play host, success arrives after disposal, expected no launch or notification mutation`() {
        // Given
        var finished = 0
        val isVisible = mutableStateOf(true)
        compose.setContent { if (isVisible.value) DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()
        compose.runOnIdle { isVisible.value = false }
        compose.waitForIdle()

        // When
        compose.runOnIdle {
            successes.toList().forEach { it.onSuccess(info(UpdateAvailability.UPDATE_AVAILABLE)) }
        }

        // Then
        verify(exactly = 0) {
            manager.startUpdateFlowForResult(
                any<AppUpdateInfo>(),
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                any<AppUpdateOptions>()
            )
        }
        assertEquals(0, finished)
    }

    @Test
    fun `given native Play host, failure arrives after disposal, expected no launch or notification mutation`() {
        // Given
        var finished = 0
        val isVisible = mutableStateOf(true)
        compose.setContent { if (isVisible.value) DistributionAppUpdateHost { finished++ } }
        compose.waitForIdle()
        compose.runOnIdle { isVisible.value = false }
        compose.waitForIdle()

        // When
        compose.runOnIdle {
            failures.toList().forEach { it.onFailure(IOException("late failure")) }
        }

        // Then
        verify(exactly = 0) {
            manager.startUpdateFlowForResult(
                any<AppUpdateInfo>(),
                any<ActivityResultLauncher<IntentSenderRequest>>(),
                any<AppUpdateOptions>()
            )
        }
        assertEquals(0, finished)
    }

    /**
     * Supplies Play eligibility without invoking a store service.
     *
     * @param availability SDK-owned availability classification.
     * @param isAllowed Whether this installation may start an immediate update.
     * @author Dmitriy Moroz
     */
    private fun info(availability: Int, isAllowed: Boolean = true): AppUpdateInfo = mockk {
        every { updateAvailability() } returns availability
        every { isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) } returns isAllowed
    }
}
