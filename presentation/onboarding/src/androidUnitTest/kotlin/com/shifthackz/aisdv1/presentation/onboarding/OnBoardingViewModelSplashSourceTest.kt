package com.shifthackz.aisdv1.presentation.onboarding

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.backup.SetBackupStartupStageUseCase
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnBoardingViewModelSplashSourceTest {
    private val router = mockk<OnBoardingRouter>(relaxed = true)
    private val preferences = mockk<PreferenceManager>(relaxed = true) {
        every { designDarkThemeToken } returns DarkThemeToken.FRAPPE.toString()
    }
    private val stage = mockk<SetBackupStartupStageUseCase>(relaxed = true)

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given first launch onboarding, user completes onboarding, expected durable invitation and no old loader navigation`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val viewModel = OnBoardingViewModel(
                mockk(relaxed = true),
                LaunchSource.SPLASH,
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher },
                router,
                preferences,
                BuildInfoProvider.stub,
                stage
            )
            // When
            viewModel.processIntent(OnBoardingIntent.Navigate)
            advanceUntilIdle()
            // Then
            verify { stage(BackupStartupStage.INVITATION); router.navigateToBackupWelcome() }
            verify(exactly = 0) { router.navigateBack() }
        }

    @Test
    fun `given settings onboarding, user completes walkthrough, expected return without first launch invitation`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val viewModel = OnBoardingViewModel(
                mockk(relaxed = true),
                LaunchSource.SETTINGS,
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher },
                router,
                preferences,
                BuildInfoProvider.stub,
                stage
            )
            // When
            viewModel.processIntent(OnBoardingIntent.Navigate)
            advanceUntilIdle()
            // Then
            verify { router.navigateBack() }
            verify(exactly = 0) { stage(any()) }
        }
}
