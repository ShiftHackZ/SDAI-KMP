package com.shifthackz.aisdv1.presentation.onboarding.preview

import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingPage
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingState

/**
 * Describes one walkthrough page together with its navigation origin and illustration preferences.
 *
 * @param name Label displayed above the generated preview.
 * @param page Page initially selected in the walkthrough pager.
 * @param launchSource Whether the walkthrough is entered at first launch or from Settings.
 * @param state Appearance preferences, version label and local-page availability.
 * @author Dmitriy Moroz
 */
internal data class OnBoardingPreviewCase(
    val name: String,
    val page: OnBoardingPage,
    val launchSource: LaunchSource = LaunchSource.SPLASH,
    val state: OnBoardingState = OnBoardingState(
        appVersion = "1.3.0-dev.local (240)",
        showLocalDiffusionPage = true,
    ),
)
