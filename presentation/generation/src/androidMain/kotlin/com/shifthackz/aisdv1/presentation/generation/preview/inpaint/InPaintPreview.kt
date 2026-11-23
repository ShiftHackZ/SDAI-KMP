package com.shifthackz.aisdv1.presentation.generation.preview.inpaint

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewTheme
import com.shifthackz.aisdv1.presentation.generation.preview.rememberGenerationPreviewBitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInPaintScreenContent

/**
 * Displays the real mask editor with a fixed source image and caller-owned immutable mask state.
 * No original-image loader, encoder, shared generation ViewModel or system-bar mutation is started.
 *
 * @param scenario Fixed mask and initial editor tab, brush/transform mode and zoom.
 * @author Dmitriy Moroz
 */
@Composable
internal fun InPaintPreview(scenario: InPaintPreviewCase) {
    val image = rememberGenerationPreviewBitmap()
    GenerationPreviewTheme {
        ImageInPaintScreenContent(
            modifier = Modifier.fillMaxSize(), image = image, state = scenario.state,
            processIntent = {}, onClose = {},
            shouldStartOnAdjustments = scenario.shouldShowAdjustments,
            isInitiallyDrawing = scenario.isDrawing,
            initialZoom = scenario.zoom,
            adjustmentScrollState = rememberScrollState(scenario.adjustmentOffset),
        )
    }
}
