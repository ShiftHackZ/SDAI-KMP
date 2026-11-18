package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.presentation.components.widget.input.DropdownTextField

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.ServerSource


/**
 * Displays the existing provider-specific dimension controls as one horizontal lazy item.
 * Width and height remain paired so their proportions, validation and ratio actions are unchanged.
 *
 * @param state Current dimensions, validation and model-dependent size options.
 * @param isImg2Img Excludes unsupported Stability image-to-image size controls.
 * @param onEvent Receives dimension or provider size-preset changes.
 * @param modifier Layout modifiers for the paired dimension-control row.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationBaseDimensions(
    state: GenerationInputFormState,
    isImg2Img: Boolean,
    onEvent: (GenerationInputFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        val localModifier = Modifier.weight(1f)

        when (state.mode) {
            ServerSource.HORDE,
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_APPLE_CORE_ML,
            ServerSource.LOCAL_APPLE_BONSAI -> {
                DropdownTextField(
                    modifier = localModifier.padding(end = 4.dp),
                    label = Localization.string("width").asUiText(),
                    value = state.width,
                    items = GenerationInputFormConstants.sizes,
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateWidth(it)) },
                )
                DropdownTextField(
                    modifier = localModifier.padding(start = 4.dp),
                    label = Localization.string("height").asUiText(),
                    value = state.height,
                    items = GenerationInputFormConstants.sizes,
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateHeight(it)) },
                )
            }

            ServerSource.AUTOMATIC1111,
            ServerSource.SWARM_UI,
            ServerSource.EASY_DIFFUSION,
            ServerSource.HUGGING_FACE,
            ServerSource.ARLI_AI,
                -> {
                GenerationSizeTextFieldsComponent(
                    modifier = localModifier,
                    width = state.width,
                    height = state.height,
                    widthValidationError = state.widthValidationError,
                    heightValidationError = state.heightValidationError,
                    onEvent = onEvent,
                )
            }

            ServerSource.FAL_AI -> {
                DropdownTextField(
                    label = Localization.string("hint_image_size").asUiText(),
                    value = state.falAiImageSize,
                    items = FalAiImageSize.entries,
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateFalAiImageSize(it)) },
                    displayDelegate = { it.displayName.asUiText() },
                )
            }

            ServerSource.STABILITY_AI -> {
                if (isImg2Img) Unit
                else GenerationSizeTextFieldsComponent(
                    modifier = localModifier,
                    width = state.width,
                    height = state.height,
                    widthValidationError = state.widthValidationError,
                    heightValidationError = state.heightValidationError,
                    onEvent = onEvent,
                )
            }

            ServerSource.OPEN_AI -> {
                DropdownTextField(
                    label = Localization.string("hint_image_size").asUiText(),
                    value = state.openAiSize,
                    items = OpenAiSize.entries.filter {
                        it.supportedModels.contains(state.openAiModel)
                    },
                    onItemSelected = { onEvent(GenerationInputFormEvent.UpdateOpenAiSize(it)) },
                    displayDelegate = { it.key.asUiText() },
                )
            }

            else -> Unit
        }

    }
}
