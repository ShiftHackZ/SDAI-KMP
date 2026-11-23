package com.shifthackz.aisdv1.presentation.onboarding.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingPage

/**
 * Shows every walkthrough page and its real pager controls in light and dark appearance.
 *
 * @param scenario Named page and launch-origin state supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Onboarding", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Onboarding", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun OnBoardingStatesPreview(
    @PreviewParameter(OnBoardingPreviewParameterProvider::class) scenario: OnBoardingPreviewCase,
) {
    OnBoardingPreview(scenario)
}

/**
 * Checks the illustrated prompt form and pager footer on a compact phone in both palettes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Form · Compact light", group = "Onboarding", widthDp = 320, heightDp = 640)
@Preview(
    name = "Form · Compact dark", group = "Onboarding", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun OnBoardingCompactPreview() {
    OnBoardingPreview(OnBoardingPreviewCase(name = "Compact form", page = OnBoardingPage.Form))
}

/**
 * Checks the final appearance illustration and completion control with enlarged heading text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Appearance · Large text",
    group = "Onboarding",
    widthDp = 320,
    heightDp = 740,
    fontScale = 1.5f
)
@Composable
private fun OnBoardingLargeTextPreview() {
    OnBoardingPreview(OnBoardingPreviewCase(name = "Large text", page = OnBoardingPage.LookAndFeel))
}
