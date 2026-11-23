package com.shifthackz.aisdv1.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.shifthackz.aisdv1.core.common.platform.Platform
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
    serverSource = ServerSource.LOCAL_MICROSOFT_ONNX,
    titleKey = "on_boarding_page_local_title",
    progressTitleKey = "communicating_local_title",
    platform = Platform.ANDROID,
)

/**
 * Forwards Android back presses to pager navigation while interception is enabled.
 * Design previews do not register a system-back dispatcher.
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
) {
    if (LocalInspectionMode.current) return
    BackHandler(enabled = enabled, onBack = onBack)
}
