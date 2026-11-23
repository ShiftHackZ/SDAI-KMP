package com.shifthackz.aisdv1.presentation.generation.preview.modal

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal

/**
 * Displays progress, benchmark decisions and operation feedback in both application palettes.
 *
 * @param scenario Named fixed-data snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "GenerationModal · Light", group = "GenerationModal", widthDp = 360, heightDp = 800)
@Preview(
    name = "GenerationModal · Dark",
    group = "GenerationModal",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GenerationModalStatesPreview(
    @PreviewParameter(GenerationModalPreviewParameterProvider::class) scenario: GenerationModalPreviewCase,
) {
    GenerationModalPreview(scenario)
}

/**
 * Displays the same production content at compact, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "GenerationModal sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark",
    group = "GenerationModal sizes",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "GenerationModal sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "GenerationModal sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun GenerationModalSizesPreview() {
    GenerationModalPreview(
        GenerationModalPreviewCase(
            "Progress",
            GenerationModal.Generating(canCancel = true),
            elapsedText = "01:05",
            shouldShowPerformance = true
        )
    )
}
