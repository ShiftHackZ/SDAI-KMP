package com.shifthackz.aisdv1.presentation.generation.preview.img2img

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewTheme
import com.shifthackz.aisdv1.presentation.generation.preview.rememberGenerationPreviewBitmap
import com.shifthackz.aisdv1.presentation.generation.preview.generationPreviewAnchorIndex
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionContent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInputSection
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageContent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageStrings

/**
 * Displays the real image-generation screen and source card using fixed pixels and inert intents.
 * No native picker, camera, random image request, provider load or mask encoder is started.
 *
 * @param scenario Fixed source, provider inputs and initial visible control.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImageGenerationPreview(scenario: ImageGenerationPreviewCase) {
    val image = rememberGenerationPreviewBitmap()
    GenerationPreviewTheme {
        val strings = ImageToImageStrings()
        ImageToImageContent(
            state = scenario.state,
            processIntent = {},
            modifier = Modifier.fillMaxSize(),
            strings = strings,
            useDrawerNavigation = true,
            backgroundWorkContent = {},
            listState = rememberLazyListState(
                initialFirstVisibleItemIndex = generationPreviewAnchorIndex(
                    scenario.state, scenario.anchor, true, scenario.isFormExpanded,
                )
            ),
            isFormInitiallyExpanded = scenario.isFormExpanded,
            engineContent = { modifier ->
                EngineSelectionContent(
                    state = GenerationPreviewFixtures.engine(scenario.state.mode),
                    modifier = modifier,
                    processIntent = {},
                )
            },
            inputImageContent = { modifier ->
                ImageInputSection(
                    state = scenario.state,
                    strings = strings,
                    modifier = modifier,
                    image = image.takeIf { scenario.state.hasInputImage && !scenario.isSourceUnavailable },
                    processIntent = {},
                    onInPaintClick = {},
                )
            },
        )
    }
}
