package com.shifthackz.aisdv1.presentation.generation.preview.txt2img

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Displays provider forms, validation and advanced controls in both application palettes.
 *
 * @param scenario Named fixed-data snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "TextGeneration · Light", group = "TextGeneration", widthDp = 360, heightDp = 800)
@Preview(
    name = "TextGeneration · Dark",
    group = "TextGeneration",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TextGenerationStatesPreview(
    @PreviewParameter(TextGenerationPreviewParameterProvider::class) scenario: TextGenerationPreviewCase,
) {
    TextGenerationPreview(scenario)
}

/**
 * Displays the same production content at compact, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "TextGeneration sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark",
    group = "TextGeneration sizes",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "TextGeneration sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "TextGeneration sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun TextGenerationSizesPreview() {
    TextGenerationPreview(TextGenerationPreviewCase("Ready", GenerationPreviewFixtures.text()))
}
