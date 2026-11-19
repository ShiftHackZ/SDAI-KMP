package com.shifthackz.aisdv1.presentation.onboarding

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.usecase.backup.SetBackupStartupStageUseCase
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import kotlinx.coroutines.withContext

/**
 * Initializes onboarding previews and replaces first-launch completion with the restore invitation.
 * Completion is persisted before navigation; a settings launch returns to the caller, while first launch
 * persists recovery progression off the UI dispatcher before navigating.
 *
 * @param diagnostics Optional bounded observer for MVI state transitions.
 * @param launchSource Distinguishes first-launch completion from revisiting onboarding through settings.
 * @param dispatchersProvider Runs configuration checks on IO and publishes navigation on the UI dispatcher.
 * @param router Receives setup, loader or back navigation after completion.
 * @param setBackupStartupStage Persists the fresh-install restore invitation before onboarding is consumed.
 * @param preferenceManager Supplies saved preview appearance; the checked startup transition owns onboarding completion.
 * @param buildInfoProvider Supplies the version displayed in the appearance preview.
 *
 * @author Dmitriy Moroz
 */
class OnBoardingViewModel(
    diagnostics: UiDiagnostics,
    val launchSource: LaunchSource,
    private val dispatchersProvider: DispatchersProvider,
    private val router: OnBoardingRouter,
    private val preferenceManager: PreferenceManager,
    private val buildInfoProvider: BuildInfoProvider,
    private val setBackupStartupStage: SetBackupStartupStageUseCase,
) : BaseMviViewModel<OnBoardingState, OnBoardingIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.ON_BOARDING.asDiagnosticScreen(),
    initialState = OnBoardingState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        emitState(
            OnBoardingState(
                darkThemeToken = DarkThemeToken.parse(preferenceManager.designDarkThemeToken),
                appVersion = buildInfoProvider.toString(),
                showLocalDiffusionPage = isLocalDiffusionOnBoardingAvailable(),
            )
        )
    }

    override fun processIntent(intent: OnBoardingIntent) {
        when (intent) {
            OnBoardingIntent.Navigate -> navigate()
        }
    }

    private fun navigate() {
        when (launchSource) {
            LaunchSource.SPLASH -> launch(dispatchersProvider.io) {
                setBackupStartupStage(BackupStartupStage.INVITATION)
                withContext(dispatchersProvider.immediate) { router.navigateToBackupWelcome() }
            }

            LaunchSource.SETTINGS -> router.navigateBack()
        }
    }
}
