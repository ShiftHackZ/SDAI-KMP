package com.shifthackz.aisdv1.presentation.onboarding

/**
 * Stable page identities used by onboarding pager ordering and preview selection.
 *
 * @author Dmitriy Moroz
 */
enum class OnBoardingPage {
    Universal,
    Providers,
    Form,
    LocalDiffusion,
    LookAndFeel,
}

/**
 * Returns onboarding pages in their declared order, optionally omitting local generation.
 *
 * @param showLocalDiffusionPage Whether the active build supports the local-generation explanation.
 * @return Ordered page identities used by pager indexing and navigation controls.
 *
 * @author Dmitriy Moroz
 */
fun visibleOnBoardingPages(
    showLocalDiffusionPage: Boolean,
): List<OnBoardingPage> = OnBoardingPage.entries.filter { page ->
    showLocalDiffusionPage || page != OnBoardingPage.LocalDiffusion
}
