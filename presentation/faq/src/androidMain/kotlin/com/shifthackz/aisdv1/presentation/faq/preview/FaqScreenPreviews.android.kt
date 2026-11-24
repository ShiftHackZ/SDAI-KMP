package com.shifthackz.aisdv1.presentation.faq.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.faq.content.FaqScreenContent

/**
 * Shows load states and populated, filtered, and empty search results in both palettes.
 *
 * @param case Named screen state and search controls to render.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "FAQ · Screen", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "FAQ · Screen", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FaqScreenStatesPreview(
    @PreviewParameter(FaqPreviewParameterProvider::class) case: FaqPreviewCase,
) {
    FaqPreview(case)
}

/**
 * Shows cached content and the Retry action on a narrow phone with oversized text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Large text",
    group = "FAQ · Screen",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun FaqCompactPreview() {
    FaqPreview(FaqPreviewFixtures.cached)
}

/**
 * Shows centered FAQ content and the active filter indicator in landscape.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Landscape", group = "FAQ · Screen", widthDp = 820, heightDp = 420)
@Composable
private fun FaqLandscapePreview() {
    FaqPreview(FaqPreviewFixtures.category)
}

@Composable
private fun FaqPreview(case: FaqPreviewCase) {
    FaqPreviewTheme {
        FaqScreenContent(
            state = case.state,
            query = case.query,
            categoryId = case.categoryId,
            onQueryChange = {},
            onCategoryChange = {},
            onRetry = {},
            onBack = {},
        )
    }
}
