package com.shifthackz.aisdv1.presentation.generation.modal.embedding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.presentation.generation.modal.extras.ExtrasEffect
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Loads textual-inversion choices for a captured prompt pair and returns confirmed edits to the form.
 *
 * @param prompt Positive prompt before editing.
 * @param negativePrompt Negative prompt before editing.
 * @param onNewPrompts Receives confirmed positive and negative prompts before dismissal.
 * @param onClose Removes the editor on cancellation or after confirmed edits.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun EmbeddingScreen(
    prompt: String,
    negativePrompt: String,
    onNewPrompts: (String, String) -> Unit,
    onClose: () -> Unit,
) {
    val koin = remember { getKoin() }
    val viewModel = remember(
        koin,
        prompt,
        negativePrompt,
    ) {
        koin.get<EmbeddingViewModel> {
            parametersOf(prompt, negativePrompt)
        }
    }
    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect) {
                is ExtrasEffect.ApplyPrompts -> {
                    onNewPrompts(effect.prompt, effect.negativePrompt)
                    onClose()
                }

                ExtrasEffect.Close -> onClose()
            }
        }
    ) { state, intentHandler ->
        EmbeddingScreenContent(
            state = state,
            processIntent = intentHandler,
        )
    }
}
