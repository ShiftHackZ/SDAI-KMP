package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

/**
 * Width fraction reserved for the onboarding phone illustration and navigation controls.
 *
 * @author Dmitriy Moroz
 */
val onBoardingPhoneWidthFraction = 0.76f

/**
 * Pager transition shared by next, previous and system-back onboarding navigation.
 *
 * @author Dmitriy Moroz
 */
val onBoardingPageAnimation = tween<Float>(1200, easing = FastOutSlowInEasing)
