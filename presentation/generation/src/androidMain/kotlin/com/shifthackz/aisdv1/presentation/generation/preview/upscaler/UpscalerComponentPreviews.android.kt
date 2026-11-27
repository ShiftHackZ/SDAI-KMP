package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Covers named components states in the light and dark app palettes.
 *
 * @param scenario Fixed visual snapshot from the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Upscaler · Components", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark",
    group = "Upscaler · Components",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun UpscalerComponentStatesPreview(
    @PreviewParameter(UpscalerComponentPreviewParameterProvider::class) scenario: UpscalerComponentPreviewCase,
) {
    UpscalerComponentPreview(scenario)
}

/**
 * Covers narrow phones, landscape and enlarged text using production content.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Upscaler · Components sizes",
    widthDp = 320,
    heightDp = 640
)
@Preview(
    name = "Compact · Dark",
    group = "Upscaler · Components sizes",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Upscaler · Components sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Upscaler · Components sizes",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun UpscalerComponentSizesPreview() {
    UpscalerComponentPreview(UpscalerComponentPreviewCase("Parameters"))
}
