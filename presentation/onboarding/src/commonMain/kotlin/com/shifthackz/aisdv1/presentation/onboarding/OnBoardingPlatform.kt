package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Provider identity and localization keys for the current platform's local-generation preview.
 *
 * @param serverSource Local provider demonstrated by the preview.
 * @param titleKey Localization key for the explanatory heading.
 * @param progressTitleKey Localization key for the illustrative generation-progress title.
 *
 * @author Dmitriy Moroz
 */
data class LocalDiffusionOnBoardingSpec(
    val serverSource: ServerSource,
    val titleKey: String,
    val progressTitleKey: String,
)

/**
 * Reports whether this platform/build should include local-generation onboarding.
 *
 * @author Dmitriy Moroz
 */
expect fun isLocalDiffusionOnBoardingAvailable(): Boolean

/**
 * Selects the local provider and localized copy demonstrated on this platform.
 *
 * @author Dmitriy Moroz
 */
expect fun localDiffusionOnBoardingSpec(): LocalDiffusionOnBoardingSpec

/**
 * Installs platform back handling while the pager can return to an earlier page.
 *
 * @param enabled Whether a previous page or active scroll should consume platform back.
 * @param onBack Requests the pager's previous-page animation.
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun OnBoardingBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
