package com.shifthackz.aisdv1.presentation.generation.modal.tag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import org.koin.core.parameter.parametersOf

/**
 * Hosts a remembered single-tag editor and returns the confirmed prompt pair before dismissal.
 *
 * @param modifier Layout modifiers for the editor dialog content.
 * @param prompt Positive prompt captured before opening.
 * @param negativePrompt Negative prompt captured before opening.
 * @param tag Exact tag selected for editing.
 * @param isNegative Whether the tag belongs to the negative prompt.
 * @param onDismissRequest Removes the dialog on cancellation or after applying edits.
 * @param onNewPrompts Receives confirmed positive and negative prompts.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun EditTagDialog(
    modifier: Modifier = Modifier,
    prompt: String,
    negativePrompt: String,
    tag: String,
    isNegative: Boolean,
    onDismissRequest: () -> Unit,
    onNewPrompts: (String, String) -> Unit,
) {
    val koin = remember { getKoin() }
    val viewModel = remember(
        koin,
        prompt,
        negativePrompt,
        tag,
        isNegative,
    ) {
        koin.get<EditTagViewModel> {
            parametersOf(prompt, negativePrompt, tag, isNegative)
        }
    }
    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect) {
                EditTagEffect.Close -> onDismissRequest()

                is EditTagEffect.ApplyPrompts -> {
                    onNewPrompts(effect.prompt, effect.negativePrompt)
                    onDismissRequest()
                }
            }
        },
    ) { state, processIntent ->
        EditTagScreenContent(
            modifier = modifier,
            state = state,
            processIntent = processIntent,
        )
    }
}
