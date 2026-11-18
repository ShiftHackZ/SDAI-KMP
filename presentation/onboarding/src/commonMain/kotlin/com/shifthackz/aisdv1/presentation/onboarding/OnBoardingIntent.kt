package com.shifthackz.aisdv1.presentation.onboarding

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Completion and exit actions produced by the onboarding pager.
 *
 * @author Dmitriy Moroz
 */
sealed interface OnBoardingIntent : MviIntent {
    /**
     * Completes first-launch onboarding or returns from a settings-launched walkthrough.
     *
     * @author Dmitriy Moroz
     */
    data object Navigate : OnBoardingIntent
}
