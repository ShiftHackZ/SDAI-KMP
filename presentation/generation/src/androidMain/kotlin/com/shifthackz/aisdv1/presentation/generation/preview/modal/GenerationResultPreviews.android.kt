package com.shifthackz.aisdv1.presentation.generation.preview.modal

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal

/**
 * Displays single-image and batch-result actions in both application palettes.
 *
 * @param scenario Named fixed-data snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(
    name = "GenerationResult · Light",
    group = "GenerationResult",
    widthDp = 360,
    heightDp = 800
)
@Preview(
    name = "GenerationResult · Dark",
    group = "GenerationResult",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GenerationResultStatesPreview(
    @PreviewParameter(GenerationResultPreviewParameterProvider::class) scenario: GenerationModalPreviewCase,
) {
    GenerationModalPreview(scenario)
}

/**
 * Displays the same production content at compact, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "GenerationResult sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark",
    group = "GenerationResult sizes",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "GenerationResult sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "GenerationResult sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun GenerationResultSizesPreview() {
    GenerationModalPreview(
        GenerationModalPreviewCase(
            "Single result",
            GenerationModal.Image.Single(GenerationPreviewFixtures.result, false, true)
        )
    )
}
