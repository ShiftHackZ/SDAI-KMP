package com.shifthackz.aisdv1.presentation.configuration.preview.upscaler

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Covers named configuration states in the light and dark app palettes.
 *
 * @param scenario Fixed visual snapshot from the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Upscaler · Configuration", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark",
    group = "Upscaler · Configuration",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun UpscalerSetupStatesPreview(
    @PreviewParameter(UpscalerSetupPreviewParameterProvider::class) scenario: UpscalerSetupPreviewCase,
) {
    UpscalerSetupPreview(scenario)
}

/**
 * Covers narrow phones, landscape and enlarged text using production content.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Upscaler · Configuration sizes",
    widthDp = 320,
    heightDp = 640
)
@Preview(
    name = "Compact · Dark",
    group = "Upscaler · Configuration sizes",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Landscape",
    group = "Upscaler · Configuration sizes",
    widthDp = 800,
    heightDp = 360
)
@Preview(
    name = "Large text",
    group = "Upscaler · Configuration sizes",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun UpscalerSetupSizesPreview() {
    UpscalerSetupPreview(UpscalerSetupPreviewParameterProvider().values.first())
}
