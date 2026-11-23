package com.shifthackz.aisdv1.presentation.onboarding.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingContent
import com.shifthackz.aisdv1.presentation.onboarding.visibleOnBoardingPages

/**
 * Composes the real walkthrough at the requested page without a ViewModel or completion side effects.
 * The page illustrations use fixed sample state and omit live background-work services.
 *
 * @param scenario Page, navigation origin and appearance state to illustrate.
 * @author Dmitriy Moroz
 */
@Composable
internal fun OnBoardingPreview(scenario: OnBoardingPreviewCase) {
    val pages = visibleOnBoardingPages(scenario.state.showLocalDiffusionPage)
    val pagerState = rememberPagerState(
        initialPage = pages.indexOf(scenario.page).coerceAtLeast(0),
        pageCount = { pages.size },
    )
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
            darkThemeToken = scenario.state.darkThemeToken,
        ),
        applySystemBars = false,
    ) {
        OnBoardingContent(
            launchSource = scenario.launchSource,
            state = scenario.state,
            processIntent = {},
            pagerState = pagerState,
        )
    }
}
