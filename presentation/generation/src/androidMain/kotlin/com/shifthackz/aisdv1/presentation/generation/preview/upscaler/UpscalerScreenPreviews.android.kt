package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Covers named standalone states in the light and dark app palettes.
 *
 * @param scenario Fixed visual snapshot from the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Upscaler · Standalone", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark",
    group = "Upscaler · Standalone",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun UpscalerScreenStatesPreview(
    @PreviewParameter(UpscalerScreenPreviewParameterProvider::class) scenario: UpscalerScreenPreviewCase,
) {
    UpscalerScreenPreview(scenario)
}

/**
 * Covers narrow phones, landscape and enlarged text using production content.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Upscaler · Standalone sizes",
    widthDp = 320,
    heightDp = 640
)
@Preview(
    name = "Compact · Dark",
    group = "Upscaler · Standalone sizes",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Upscaler · Standalone sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Upscaler · Standalone sizes",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun UpscalerScreenSizesPreview() {
    UpscalerScreenPreview(UpscalerScreenPreviewCase("Parameters", firstVisibleItemIndex = 2))
}
