package com.shifthackz.aisdv1.presentation.onboarding

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCase
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test


@Ignore("ToDo: Investigate why sometimes tests fail on remote worker due to race-conditions.")
@OptIn(ExperimentalCoroutinesApi::class)
class OnBoardingViewModelSplashSourceTest {

    private val stubRouter = mockk<OnBoardingRouter>(relaxed = true)
    private val stubSplashNavigationUseCase = mockk<SplashNavigationUseCase>()
    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubBuildInfoProvider = mockk<BuildInfoProvider>()

    @Before
    fun initialize() {
        every {
            stubPreferenceManager::designDarkThemeToken.get()
        } returns DarkThemeToken.FRAPPE.toString()

        every {
            stubPreferenceManager::onBoardingComplete.set(any())
        } returns Unit

        every {
            stubBuildInfoProvider.toString()
        } returns ""
    }

    @Test
    fun `given on boarding view model splash source, received Navigate intent from splash, expected navigation processed`() =
        runTest {
            // Given

            coEvery {
                stubSplashNavigationUseCase()
            } returns SplashNavigationUseCase.Action.LAUNCH_HOME
            // When
            val viewModel = createViewModel(LaunchSource.SPLASH)

            viewModel.processIntent(OnBoardingIntent.Navigate)
            advanceUntilIdle()

            // Then
            verify {
                stubRouter.navigateToPostOnBoardingConfigLoader()
            }

        }

    @Test
    fun `given on boarding view model splash source, received Navigate intent from settings, expected navigateBack processed`() =
        runTest {
            // Given

            val viewModel = createViewModel(LaunchSource.SETTINGS)

            // When
            viewModel.processIntent(OnBoardingIntent.Navigate)

            // Then
            verify {
                stubRouter.navigateBack()
            }

        }

    private fun TestScope.createViewModel(
        source: LaunchSource,
    ): OnBoardingViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatchersProvider = object : DispatchersProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val ui: CoroutineDispatcher = dispatcher
            override val immediate: CoroutineDispatcher = dispatcher
        }
        return OnBoardingViewModel(
            diagnostics = mockk(relaxed = true),
            launchSource = source,
            dispatchersProvider = dispatchersProvider,
            router = stubRouter,
            splashNavigationUseCase = stubSplashNavigationUseCase,
            preferenceManager = stubPreferenceManager,
            buildInfoProvider = stubBuildInfoProvider,
        )
    }
}
