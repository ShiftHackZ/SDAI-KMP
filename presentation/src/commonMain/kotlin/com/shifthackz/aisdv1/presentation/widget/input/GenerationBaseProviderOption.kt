package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.widget.engine.EngineSelectionComponent


/**
 * Displays one provider or local-backend selection without mounting prompt editors.
 * Provider visibility is selected by generationInputBaseFields before composition.
 *
 * @param field Provider selection or backend control to render.
 * @param state Current model, available models and backend selections.
 * @param isImg2Img Selects image-to-image Fal endpoints.
 * @param onEvent Receives provider model and backend changes.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationBaseProviderOption(
    field: GenerationBaseFormField,
    state: GenerationInputFormState,
    isImg2Img: Boolean,
    onEvent: (GenerationInputFormEvent) -> Unit,
) {
    when (field) {
        GenerationBaseFormField.PROVIDER -> when (state.mode) {

            ServerSource.AUTOMATIC1111,
            ServerSource.SWARM_UI,
            ServerSource.EASY_DIFFUSION,
            ServerSource.STABILITY_AI,
            ServerSource.HUGGING_FACE,
            ServerSource.LOCAL_MNN,
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_APPLE_CORE_ML,
            ServerSource.LOCAL_APPLE_BONSAI -> EngineSelectionComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            ServerSource.OPEN_AI -> DropdownTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = Localization.string("hint_model_open_ai").asUiText(),
                value = state.openAiModel,
                items = OpenAiModel.entries,
                onItemSelected = { onEvent(GenerationInputFormEvent.UpdateOpenAiModel(it)) },
                displayDelegate = { it.alias.asUiText() },
            )

            ServerSource.FAL_AI -> DropdownTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = Localization.string("hint_fal_ai_endpoint").asUiText(),
                value = state.falAiModel,
                items = if (isImg2Img) {
                    FalAiModel.imageToImage
                } else {
                    FalAiModel.textToImage
                },
                onItemSelected = { onEvent(GenerationInputFormEvent.UpdateFalAiModel(it)) },
                displayDelegate = { it.displayName.asUiText() },
            )

            ServerSource.ARLI_AI -> DropdownTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = Localization.string("hint_arli_ai_model").asUiText(),
                value = state.arliAiModel.takeIf(String::isNotBlank),
                items = state.arliAiModels.ifEmpty {
                    listOfNotNull(state.arliAiModel.takeIf(String::isNotBlank))
                },
                onItemSelected = { onEvent(GenerationInputFormEvent.UpdateArliAiModel(it)) },
            )

            else -> Unit

        }

        GenerationBaseFormField.MNN_BACKEND -> DropdownTextField(
            modifier = Modifier.padding(top = 8.dp),
            label = Localization.string("hint_mnn_backend").asUiText(),
            value = state.mnnBackend, items = state.supportedMnnBackends,
            onItemSelected = { onEvent(GenerationInputFormEvent.UpdateMnnBackend(it)) },
            displayDelegate = {
                (when (it) {
                    MnnBackend.CPU -> "CPU"; MnnBackend.OPENCL -> "OpenCL"; MnnBackend.METAL -> "Metal"
                }).asUiText()
            })

        GenerationBaseFormField.SDXL_BACKEND -> {

            DropdownTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = Localization.string("hint_sdxl_backend").asUiText(),
                value = state.sdxlBackend,
                items = SdxlBackend.entries,
                onItemSelected = { onEvent(GenerationInputFormEvent.UpdateSdxlBackend(it)) },
                displayDelegate = { it.displayName.asUiText() },
            )

        }

        GenerationBaseFormField.BONSAI_BACKEND -> {

            DropdownTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = Localization.string("hint_bonsai_backend").asUiText(),
                value = state.bonsaiBackend,
                items = BonsaiBackend.entries,
                onItemSelected = { onEvent(GenerationInputFormEvent.UpdateBonsaiBackend(it)) },
                displayDelegate = { it.displayName.asUiText() },
            )

        }

        else -> Unit
    }
}
