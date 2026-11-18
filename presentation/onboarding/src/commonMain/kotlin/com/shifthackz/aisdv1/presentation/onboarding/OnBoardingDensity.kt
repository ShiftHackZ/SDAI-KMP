package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Density

/**
 * Fixed density used to keep scaled phone previews consistent across host screens.
 *
 * @author Dmitriy Moroz
 */
val onBoardingDensity = Density(2f, 1f)

/**
 * Width fraction reserved for the onboarding phone illustration and navigation controls.
 *
 * @author Dmitriy Moroz
 */
val onBoardingPhoneWidthFraction = 0.76f

/**
 * Portrait width-to-height ratio used by onboarding phone frames.
 *
 * @author Dmitriy Moroz
 */
val onBoardingPhoneAspectRatio = 9.5f / 16f

/**
 * Pager transition shared by next, previous and system-back onboarding navigation.
 *
 * @author Dmitriy Moroz
 */
val onBoardingPageAnimation = tween<Float>(1200, easing = FastOutSlowInEasing)
