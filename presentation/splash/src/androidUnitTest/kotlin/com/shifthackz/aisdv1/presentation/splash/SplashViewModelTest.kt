package com.shifthackz.aisdv1.presentation.splash

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCase
import com.shifthackz.aisdv1.core.navigation.router.SplashRouter
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchersProvider = object : DispatchersProvider {
        override val io: CoroutineDispatcher = testDispatcher
        override val ui: CoroutineDispatcher = testDispatcher
        override val immediate: CoroutineDispatcher = testDispatcher
    }
    private val stubSplashRouter = mockk<SplashRouter>(relaxed = true)
    private val stubSplashNavigationUseCase = mockk<SplashNavigationUseCase>()

    @Test
    fun `given splash, navigation resolves onboarding on initialization, expected onboarding route opens`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubSplashNavigationUseCase()
            } returns SplashNavigationUseCase.Action.LAUNCH_ONBOARDING

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            verify {
                stubSplashRouter.navigateToOnBoardingFromSplash()
            }

        }

    @Test
    fun `given splash, navigation resolves server setup on initialization, expected server setup route opens`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubSplashNavigationUseCase()
            } returns SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            verify {
                stubSplashRouter.navigateToServerSetupFromSplash()
            }

        }

    @Test
    fun `given splash, navigation resolves home on initialization, expected post splash configuration loader opens`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubSplashNavigationUseCase()
            } returns SplashNavigationUseCase.Action.LAUNCH_HOME

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            verify {
                stubSplashRouter.navigateToPostSplashConfigLoader()
            }

        }

    @Test
    fun `given splash, initial navigation resolution fails, expected server setup route opens`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubSplashNavigationUseCase()
            } throws Throwable("Unable to resolve initial route.")

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            verify {
                stubSplashRouter.navigateToServerSetupFromSplash()
            }

        }

    private fun TestScope.createViewModel() = SplashViewModel(
        diagnostics = mockk(relaxed = true),
        dispatchersProvider = dispatchersProvider,
        splashNavigationUseCase = stubSplashNavigationUseCase,
        splashRouter = stubSplashRouter,
    )
}
