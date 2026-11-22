package com.shifthackz.aisdv1.presentation.civitai.settings

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.entity.CivitaiAuthorizationCancelled
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.usecase.civitai.SignInToCivitaiUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CivitaiViewModelTest {
    private val repository = mockk<CivitaiRepository>(relaxed = true)
    private val router = mockk<CivitaiRouter>(relaxed = true)
    private val signIn = mockk<SignInToCivitaiUseCase>()

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given disabled automatic sharing, enable is requested then cancelled, expected consent stays disabled`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val dispatchers =
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
            coEvery { repository.account() } returns CivitaiAccount(7, "account")
            coEvery { repository.isAutoUploadEnabled() } returns false
            val vm =
                CivitaiViewModel(repository, dispatchers, mockk(relaxed = true), router, signIn)
            advanceUntilIdle()
            // When
            vm.processIntent(CivitaiIntent.EnableAutoUpload)
            assertTrue(vm.state.value.shouldConfirmAutoUpload)
            vm.processIntent(CivitaiIntent.Dismiss)
            // Then
            assertFalse(vm.state.value.shouldConfirmAutoUpload)
            assertFalse(vm.state.value.isAutoUploadEnabled)
            coVerify(exactly = 0) { repository.setAutoUploadEnabled(any()) }
        }

    @Test
    fun `given automatic sharing confirmation, user enables sharing, expected consent persisted and checked state refreshed`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val dispatchers =
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
            coEvery { repository.account() } returns CivitaiAccount(7, "account")
            coEvery { repository.isAutoUploadEnabled() } returns false
            val vm =
                CivitaiViewModel(repository, dispatchers, mockk(relaxed = true), router, signIn)
            advanceUntilIdle()
            coEvery { repository.isAutoUploadEnabled() } returns true
            // When
            vm.processIntent(CivitaiIntent.ConfirmAutoUpload)
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { repository.setAutoUploadEnabled(true) }
            assertTrue(vm.state.value.isAutoUploadEnabled)
            assertFalse(vm.state.value.shouldConfirmAutoUpload)
        }

    @Test
    fun `given signed-out account settings, upload history requested, expected independent history navigation`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val dispatchers =
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
            coEvery { repository.account() } returns null
            val vm =
                CivitaiViewModel(repository, dispatchers, mockk(relaxed = true), router, signIn)
            advanceUntilIdle()
            // When
            vm.processIntent(CivitaiIntent.OpenHistory)
            // Then
            verify(exactly = 1) { router.navigateToCivitaiHistory() }
        }

    @Test
    fun `given cancelled native sign-in, browser returns, expected quiet account page without error`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val dispatchers =
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
            val vm =
                CivitaiViewModel(repository, dispatchers, mockk(relaxed = true), router, signIn)
            advanceUntilIdle()
            coEvery { signIn() } throws CivitaiAuthorizationCancelled()
            // When
            vm.processIntent(CivitaiIntent.SignIn)
            advanceUntilIdle()
            // Then
            assertFalse(vm.state.value.isBusy)
            assertFalse(vm.state.value.hasFailed)
        }
}
