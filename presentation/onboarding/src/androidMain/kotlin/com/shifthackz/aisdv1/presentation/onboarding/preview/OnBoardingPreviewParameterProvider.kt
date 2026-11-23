package com.shifthackz.aisdv1.presentation.onboarding.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingPage
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingState
import com.shifthackz.aisdv1.presentation.onboarding.visibleOnBoardingPages

/**
 * Supplies every page from both launch origins, the shorter pager and alternate dark illustrations.
 * No onboarding completion or restore-invitation state is persisted by these scenarios.
 *
 * @author Dmitriy Moroz
 */
internal class OnBoardingPreviewParameterProvider :
    PreviewParameterProvider<OnBoardingPreviewCase> {
    private val cases = buildList {
        LaunchSource.entries.forEach { source ->
            visibleOnBoardingPages(showLocalDiffusionPage = true).forEach { page ->
                add(
                    OnBoardingPreviewCase(
                        name = "$source · $page",
                        page = page,
                        launchSource = source
                    )
                )
            }
        }
        visibleOnBoardingPages(showLocalDiffusionPage = false).forEach { page ->
            add(
                OnBoardingPreviewCase(
                    name = "Without local generation · $page",
                    page = page,
                    state = OnBoardingState(
                        appVersion = "1.3.0 (240)",
                        showLocalDiffusionPage = false
                    ),
                )
            )
        }
        DarkThemeToken.entries.filter { it != DarkThemeToken.FRAPPE }.forEach { token ->
            add(
                OnBoardingPreviewCase(
                    name = "Appearance · $token",
                    page = OnBoardingPage.LookAndFeel,
                    state = OnBoardingState(
                        darkThemeToken = token,
                        appVersion = "1.3.0-dev.local (240)",
                        showLocalDiffusionPage = true,
                    ),
                )
            )
        }
    }

    /**
     * Ordered walkthrough snapshots rendered by the light and dark preview annotations.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<OnBoardingPreviewCase> = cases.asSequence()

    /**
     * Identifies the page, launch origin or availability variant shown in the preview.
     *
     * @param index Position in the ordered scenario sequence.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
