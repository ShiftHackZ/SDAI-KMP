package com.shifthackz.aisdv1.presentation.generation.txt2img

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.source.CurrentProviderCard
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormEvent
import com.shifthackz.aisdv1.presentation.generation.widget.input.generationInputFormItems

/**
 * Displays the text-to-image card as independently composed list items. Advanced fields below
 * the viewport are not constructed when the user expands options.
 *
 * @param state Current generation inputs, provider and status.
 * @param strings Localized screen labels retained by the screen contract.
 * @param promptChipTextFieldState Caller-owned prompt selection and chip editing state.
 * @param negativePromptChipTextFieldState Caller-owned negative-prompt chip editing state.
 * @param processIntent Receives generation edits and extension actions.
 * @param modifier Layout modifiers for the independently composed lazy generation form.
 * @author Dmitriy Moroz
 */
@Composable
internal fun TextToImageBody(
    state: TextToImageState,
    strings: TextToImageStrings,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    processIntent: (TextToImageIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFormExpanded by rememberSaveable { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val urlLauncher = rememberExternalUrlLauncher()
    val onEvent: (GenerationInputFormEvent) -> Unit = remember(processIntent, urlLauncher) {
        { event ->
            if (event == GenerationInputFormEvent.OpenADetailerInstallInstructions) {
                urlLauncher.openUrl(ADETAILER_INSTALL_URL)
            } else event.toTextToImageIntent()?.let(processIntent)
        }
    }
    ToolbarScrollObserver(listState.canScrollBackward)
    LazyColumn(
        modifier = modifier.fillMaxSize()
            .verticalScrollbar(listState, topInset = LocalToolbarContentInset.current),
        state = listState,
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
    }
}

private const val ADETAILER_INSTALL_URL = "https://github.com/Bing-su/adetailer#install"
