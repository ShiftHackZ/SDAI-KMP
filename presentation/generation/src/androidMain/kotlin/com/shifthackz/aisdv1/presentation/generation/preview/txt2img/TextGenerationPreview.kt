package com.shifthackz.aisdv1.presentation.generation.preview.txt2img

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewTheme
import com.shifthackz.aisdv1.presentation.generation.preview.generationPreviewAnchorIndex
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionContent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageContent

/**
 * Displays the real text-generation screen with a fixed model picker and inert intents.
 * Background work, performance collection, provider loading and generation are never started.
 *
 * @param scenario Fixed provider inputs, discovery state and initial visible control.
 * @author Dmitriy Moroz
 */
@Composable
internal fun TextGenerationPreview(scenario: TextGenerationPreviewCase) {
    GenerationPreviewTheme {
        TextToImageContent(
            state = scenario.state,
            processIntent = {},
            modifier = Modifier.fillMaxSize(),
            useDrawerNavigation = true,
            backgroundWorkContent = {},
            listState = rememberLazyListState(
                initialFirstVisibleItemIndex = generationPreviewAnchorIndex(
                    scenario.state, scenario.anchor, false, scenario.isFormExpanded,
                )
            ),
            isFormInitiallyExpanded = scenario.isFormExpanded,
            engineContent = { modifier ->
                EngineSelectionContent(
                    state = scenario.engineState
                        ?: GenerationPreviewFixtures.engine(scenario.state.mode),
                    modifier = modifier,
                    processIntent = {},
                )
            },
        )
    }
}
