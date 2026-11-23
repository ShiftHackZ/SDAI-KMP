package com.shifthackz.aisdv1.presentation.generation.modal.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.presentation.components.model.ExtraType
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Loads a LoRA/hypernetwork catalog for the selected provider and returns confirmed prompt edits.
 *
 * @param prompt Positive prompt captured before selecting extras.
 * @param negativePrompt Negative prompt retained in the confirmed prompt pair.
 * @param type Catalog category to load and render.
 * @param onNewPrompts Receives confirmed positive and negative prompts before dismissal.
 * @param onClose Removes the catalog on cancellation or after confirmed selection.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ExtrasScreen(
    prompt: String,
    negativePrompt: String,
    type: ExtraType,
    onNewPrompts: (String, String) -> Unit,
    onClose: () -> Unit,
) {
    val koin = remember { getKoin() }
    val viewModel = remember(
        koin,
        prompt,
        negativePrompt,
        type,
    ) {
        koin.get<ExtrasViewModel> {
            parametersOf(prompt, negativePrompt, type)
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
        },
    ) { state, intentHandler ->
        ExtrasScreenContent(
            state = state,
            processIntent = intentHandler,
        )
    }
}
