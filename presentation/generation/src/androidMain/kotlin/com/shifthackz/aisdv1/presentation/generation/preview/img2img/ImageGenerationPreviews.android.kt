package com.shifthackz.aisdv1.presentation.generation.preview.img2img

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Displays provider forms, source acquisition and mask previews in both application palettes.
 *
 * @param scenario Named fixed-data snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "ImageGeneration · Light", group = "ImageGeneration", widthDp = 360, heightDp = 800)
@Preview(
    name = "ImageGeneration · Dark",
    group = "ImageGeneration",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ImageGenerationStatesPreview(
    @PreviewParameter(ImageGenerationPreviewParameterProvider::class) scenario: ImageGenerationPreviewCase,
) {
    ImageGenerationPreview(scenario)
}

/**
 * Displays the same production content at compact, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "ImageGeneration sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark",
    group = "ImageGeneration sizes",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "ImageGeneration sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "ImageGeneration sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun ImageGenerationSizesPreview() {
    ImageGenerationPreview(
        ImageGenerationPreviewCase(
            "Selected source",
            GenerationPreviewFixtures.image()
                .copy(imageBase64 = GenerationPreviewFixtures.ImageBase64)
        )
    )
}
