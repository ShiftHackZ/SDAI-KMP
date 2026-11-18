package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Selects local-generation onboarding availability for this platform/build.
 *
 * @author Dmitriy Moroz
 */
actual fun isLocalDiffusionOnBoardingAvailable(): Boolean = true

/**
 * Provides the platform-specific local provider and onboarding localization keys.
 *
 * @author Dmitriy Moroz
 */
actual fun localDiffusionOnBoardingSpec() = LocalDiffusionOnBoardingSpec(
    serverSource = ServerSource.LOCAL_APPLE_CORE_ML,
    titleKey = "on_boarding_page_core_ml_title",
    progressTitleKey = "communicating_core_ml_title",
)

/**
 * Leaves system-back handling to iOS; pager navigation remains available through its controls.
 *
 * @param enabled Whether pager navigation would consume a platform back event.
 * @param onBack Requests navigation to the preceding onboarding page.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun OnBoardingBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) = Unit
