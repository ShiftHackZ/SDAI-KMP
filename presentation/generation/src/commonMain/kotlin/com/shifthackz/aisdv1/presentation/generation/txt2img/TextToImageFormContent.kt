package com.shifthackz.aisdv1.presentation.generation.txt2img

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.withoutVisualEffect
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.presentation.components.platform.ExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionComponent
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.source.CurrentProviderCard
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformLazySurface
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerForm
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerFormState
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormEvent
import com.shifthackz.aisdv1.presentation.generation.widget.input.generationInputFormItems

/**
 * Displays the text-to-image card as independently composed list items. Advanced fields below
 * the viewport are not constructed when the user expands options.
 *
 * @param state Current generation inputs, provider and status.
 * @param promptChipTextFieldState Caller-owned prompt selection and chip editing state.
 * @param negativePromptChipTextFieldState Caller-owned negative-prompt chip editing state.
 * @param processIntent Receives generation edits and extension actions.
 * @param modifier Layout modifiers for the independently composed lazy generation form.
 * @param listState Retained vertical position within the generation form.
 * @param upscalerFormState Independent catalog snapshot shared with the other Home forms.
 * @param isInitiallyExpanded Initial disclosure state of generation options.
 * @param engineContent Checkpoint picker supplied by the owning form.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun TextToImageBody(
    state: TextToImageState,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    processIntent: (TextToImageIntent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    isInitiallyExpanded: Boolean = true,
    upscalerFormState: UpscalerFormState? = null,
    engineContent: @Composable (Modifier) -> Unit = { EngineSelectionComponent(modifier = it) },
) {
    var isFormExpanded by rememberSaveable { mutableStateOf(isInitiallyExpanded) }
    val overscrollEffect = rememberOverscrollEffect()
    val lastGenerationItemIndex by remember(listState) {
        derivedStateOf { (listState.layoutInfo.totalItemsCount - 2).coerceAtLeast(1) }
    }
    val urlLauncher = if (LocalInspectionMode.current) remember { ExternalUrlLauncher {} }
    else rememberExternalUrlLauncher()
    val onEvent: (GenerationInputFormEvent) -> Unit = remember(processIntent, urlLauncher) {
        { event ->
            if (event == GenerationInputFormEvent.OpenADetailerInstallInstructions) {
                urlLauncher.openUrl(ADETAILER_INSTALL_URL)
            } else event.toTextToImageIntent()?.let(processIntent)
        }
    }
    ToolbarScrollObserver(listState.canScrollBackward)
    PlatformLazySurface(
        state = listState,
        firstItemIndex = 1,
        lastItemIndex = lastGenerationItemIndex,
        modifier = modifier.fillMaxSize(),
        horizontalPadding = 16.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        overscrollEffect = overscrollEffect,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .verticalScrollbar(listState, topInset = LocalToolbarContentInset.current),
            state = listState,
            overscrollEffect = overscrollEffect?.withoutVisualEffect(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp + LocalToolbarContentInset.current,
                bottom = 16.dp
            ),
        ) {
            item(key = "generation-provider") {
                CurrentProviderCard(
                    source = state.mode,
                    platform = state.platform,
                    onClick = { processIntent(TextToImageIntent.ConfigureProvider) },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            generationInputFormItems(
                state = state,
                isImg2Img = false,
                isExpanded = isFormExpanded,
                onToggleExpanded = { isFormExpanded = !isFormExpanded },
                promptChipTextFieldState = promptChipTextFieldState,
                negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                onEvent = onEvent,
                engineContent = engineContent,
                hasFooter = state.error != null || state.message != null || state.generating,
                footer = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        state.error?.let { message ->
                            Text(
                                modifier = Modifier.padding(top = 14.dp),
                                text = message.asString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        state.message?.let { message ->
                            Text(
                                modifier = Modifier.padding(top = if (state.error == null) 14.dp else 0.dp),
                                text = message.asString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (state.generating) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().padding(
                                    top = if (state.error == null && state.message == null) 14.dp else 0.dp,
                                ),
                            )
                        }
                    }
                },
            )
            item(key = "upscaler-form") {
                UpscalerForm(
                    state = upscalerFormState,
                    configuration = state.upscaler,
                    onConfigurationChange = { processIntent(TextToImageIntent.UpdateUpscaler(it)) },
                    onConfigure = { processIntent(TextToImageIntent.ConfigureUpscaler) },
                    modifier = Modifier.padding(top = 16.dp),
                    isEnabled = !state.generating
                )
            }
        }
    }
}

private const val ADETAILER_INSTALL_URL = "https://github.com/Bing-su/adetailer#install"
