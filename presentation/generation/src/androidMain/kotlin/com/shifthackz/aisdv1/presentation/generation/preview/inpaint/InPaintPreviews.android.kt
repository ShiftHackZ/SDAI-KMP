package com.shifthackz.aisdv1.presentation.generation.preview.inpaint

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Displays mask drawing, viewport and request parameters in both application palettes.
 *
 * @param scenario Named fixed-data snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "InPaint · Light", group = "InPaint", widthDp = 360, heightDp = 800)
@Preview(
    name = "InPaint · Dark",
    group = "InPaint",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun InPaintStatesPreview(
    @PreviewParameter(InPaintPreviewParameterProvider::class) scenario: InPaintPreviewCase,
) {
    InPaintPreview(scenario)
}

/**
 * Displays the same production content at compact, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "InPaint sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark",
    group = "InPaint sizes",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "InPaint sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "InPaint sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun InPaintSizesPreview() {
    InPaintPreview(InPaintPreviewCase("Mask editor", GenerationPreviewFixtures.mask))
}
