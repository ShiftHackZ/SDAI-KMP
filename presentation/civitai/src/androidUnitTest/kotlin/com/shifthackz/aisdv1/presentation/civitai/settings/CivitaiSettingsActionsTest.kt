package com.shifthackz.aisdv1.presentation.civitai.settings

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.usecase.civitai.SignInToCivitaiUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Protects observable settings recovery, native-login cancellation and busy-operation action guards.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CivitaiSettingsActionsTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<CivitaiRepository>(relaxed = true)
    private val router = mockk<CivitaiRouter>(relaxed = true)
    private val signIn = mockk<SignInToCivitaiUseCase>()
    private val dispatchers = mockk<DispatchersProvider> {
        every { io } returns dispatcher
        every { immediate } returns dispatcher
    }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.account() } returns CivitaiAccount(7, "creator")
        coEvery { repository.isAutoUploadEnabled() } returns true
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given pasted API key, connection requested, expected trimmed key and refreshed account view`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            // When
            vm.processIntent(CivitaiIntent.Connect("  key-value\n"))
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { repository.connect("key-value") }
            assertEquals("creator", vm.state.value.account?.username)
            assertFalse(vm.state.value.isBusy)
            assertFalse(vm.state.value.hasFailed)
        }

    @Test
    fun `given account operation failure, error is dismissed, expected usable settings and cleared feedback`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            coEvery { repository.connect(any()) } throws IllegalStateException("unavailable")
            // When
            vm.processIntent(CivitaiIntent.Connect("key"))
            advanceUntilIdle()
            // Then
            assertTrue(vm.state.value.hasFailed)
            assertFalse(vm.state.value.isBusy)
            vm.processIntent(CivitaiIntent.Dismiss)
            assertFalse(vm.state.value.hasFailed)
        }

    @Test
    fun `given enabled automatic uploads, disable is requested, expected saved disabled state without consent dialog`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            coEvery { repository.isAutoUploadEnabled() } returns false
            // When
            vm.processIntent(CivitaiIntent.DisableAutoUpload)
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { repository.setAutoUploadEnabled(false) }
            assertFalse(vm.state.value.isAutoUploadEnabled)
            assertFalse(vm.state.value.shouldConfirmAutoUpload)
        }

    @Test
    fun `given pending sign-in, repeated settings actions and Back arrive, expected one login with Back still available`() =
        runTest(dispatcher) {
            // Given
            val completion = CompletableDeferred<Unit>()
            coEvery { signIn() } coAnswers { completion.await() }
            val vm = viewModel()
            advanceUntilIdle()
            vm.processIntent(CivitaiIntent.SignIn)
            runCurrent()
            // When
            vm.processIntent(CivitaiIntent.SignIn)
            vm.processIntent(CivitaiIntent.Disconnect)
            vm.processIntent(CivitaiIntent.EnableAutoUpload)
            vm.processIntent(CivitaiIntent.Back)
            // Then
            assertTrue(vm.state.value.isBusy)
            assertFalse(vm.state.value.shouldConfirmAutoUpload)
            verify(exactly = 1) { router.navigateBack() }
            coVerify(exactly = 1) { signIn() }
            coVerify(exactly = 0) { repository.disconnect() }
            completion.complete(Unit)
            advanceUntilIdle()
        }

    @Test
    fun `given native login succeeds, account page reloads, expected new identity and cleared busy state`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            coEvery { signIn() } coAnswers {
                coEvery { repository.account() } returns CivitaiAccount(
                    8,
                    "other"
                )
            }
            // When
            vm.processIntent(CivitaiIntent.SignIn)
            advanceUntilIdle()
            // Then
            assertEquals("other", vm.state.value.account?.username)
            assertFalse(vm.state.value.isBusy)
            assertFalse(vm.state.value.hasFailed)
        }

    @Test
    fun `given connected account, disconnect completes, expected signed-out page with disabled automatic uploads`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            coEvery { repository.disconnect() } coAnswers {
                coEvery { repository.account() } returns null
                coEvery { repository.isAutoUploadEnabled() } returns false
            }
            // When
            vm.processIntent(CivitaiIntent.Disconnect)
            advanceUntilIdle()
            // Then
            assertNull(vm.state.value.account)
            assertFalse(vm.state.value.isAutoUploadEnabled)
            assertFalse(vm.state.value.isBusy)
        }

    @Test
    fun `given cancelled account operation, request stops, expected no generic error or retained busy state`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            coEvery { signIn() } throws CancellationException("route cancelled")
            // When
            vm.processIntent(CivitaiIntent.SignIn)
            advanceUntilIdle()
            // Then
            assertFalse(vm.state.value.isBusy)
            assertFalse(vm.state.value.hasFailed)
        }

    /**
     * Creates the real settings state owner using deterministic dispatchers and inert diagnostics.
     *
     * @author Dmitriy Moroz
     */
    private fun viewModel() =
        CivitaiViewModel(repository, dispatchers, mockk(relaxed = true), router, signIn)
}
