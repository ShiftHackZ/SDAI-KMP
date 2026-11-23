package com.shifthackz.aisdv1.presentation.components.preview.engine

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.engine.EasyDiffusionEngineContent
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionContent
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionState
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.domain.entity.ServerSource

import com.shifthackz.aisdv1.presentation.components.widget.engine.DropdownTextField as EngineDropdown

/**
 * All model-picker provider branches with loading, empty, selected and Easy Diffusion retry feedback.
 *
 * @author Dmitriy Moroz
 */
internal class EnginePreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("EngineSelectionContent · AUTOMATIC1111 · Selected") {
            EngineSelectionContent(
                state = engineState(ServerSource.AUTOMATIC1111),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · AUTOMATIC1111 · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.AUTOMATIC1111).copy(loading = true),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · AUTOMATIC1111 · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.AUTOMATIC1111
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · EASY_DIFFUSION · Selected") {
            EngineSelectionContent(
                state = engineState(ServerSource.EASY_DIFFUSION),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · EASY_DIFFUSION · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.EASY_DIFFUSION).copy(loading = true),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · EASY_DIFFUSION · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.EASY_DIFFUSION
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · SWARM_UI · Selected") {
            EngineSelectionContent(state = engineState(ServerSource.SWARM_UI), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · SWARM_UI · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.SWARM_UI).copy(loading = true),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · SWARM_UI · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.SWARM_UI
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · HUGGING_FACE · Selected") {
            EngineSelectionContent(
                state = engineState(ServerSource.HUGGING_FACE),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · HUGGING_FACE · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.HUGGING_FACE).copy(loading = true),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · HUGGING_FACE · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.HUGGING_FACE
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · STABILITY_AI · Selected") {
            EngineSelectionContent(
                state = engineState(ServerSource.STABILITY_AI),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · STABILITY_AI · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.STABILITY_AI).copy(loading = true),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · STABILITY_AI · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.STABILITY_AI
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_MICROSOFT_ONNX · Selected") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_MICROSOFT_ONNX),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_MICROSOFT_ONNX · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_MICROSOFT_ONNX).copy(
                    loading = true
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_MICROSOFT_ONNX · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.LOCAL_MICROSOFT_ONNX
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_STABLE_DIFFUSION_CPP · Selected") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_STABLE_DIFFUSION_CPP),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_STABLE_DIFFUSION_CPP · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_STABLE_DIFFUSION_CPP).copy(
                    loading = true
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_STABLE_DIFFUSION_CPP · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.LOCAL_STABLE_DIFFUSION_CPP
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_APPLE_CORE_ML · Selected") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_APPLE_CORE_ML),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_APPLE_CORE_ML · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_APPLE_CORE_ML).copy(
                    loading = true
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_APPLE_CORE_ML · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.LOCAL_APPLE_CORE_ML
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_MNN · Selected") {
            EngineSelectionContent(state = engineState(ServerSource.LOCAL_MNN), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_MNN · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_MNN).copy(loading = true),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_MNN · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.LOCAL_MNN
                ), processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_APPLE_BONSAI · Selected") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_APPLE_BONSAI),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_APPLE_BONSAI · Loading") {
            EngineSelectionContent(
                state = engineState(ServerSource.LOCAL_APPLE_BONSAI).copy(loading = true),
                processIntent = {})
        },
        ComponentPreviewCase("EngineSelectionContent · LOCAL_APPLE_BONSAI · Empty") {
            EngineSelectionContent(
                state = EngineSelectionState(
                    loading = false,
                    mode = ServerSource.LOCAL_APPLE_BONSAI
                ), processIntent = {})
        },
        ComponentPreviewCase("EasyDiffusion · Failure") {
            EasyDiffusionEngineContent(
                state = engineState(ServerSource.EASY_DIFFUSION).copy(
                    hasEasyDiffusionModelLoadFailed = true,
                    easyDiffusionModels = emptyList()
                ), processIntent = {})
        },
        ComponentPreviewCase("EasyDiffusion · Unavailable selection") {
            EasyDiffusionEngineContent(
                state = engineState(ServerSource.EASY_DIFFUSION).copy(
                    selectedEasyDiffusionModel = "missing"
                ), processIntent = {})
        },
        ComponentPreviewCase("EasyDiffusion · Long identifier") {
            EasyDiffusionEngineContent(
                state = engineState(ServerSource.EASY_DIFFUSION).copy(
                    selectedEasyDiffusionModel = "checkpoints/a-very-long-provider-specific-model-identifier.safetensors",
                    easyDiffusionModels = listOf(
                        EasyDiffusionModel(
                            "checkpoints/a-very-long-provider-specific-model-identifier.safetensors",
                            "Display name"
                        )
                    )
                ), processIntent = {})
        },
        ComponentPreviewCase("Engine dropdown · Expanded") {
            EngineDropdown(
                label = "Model",
                value = "Sample model",
                items = listOf("Sample model", "Another model"),
                isInitiallyExpanded = true
            )
        },
        ComponentPreviewCase("Engine dropdown · Loading") {
            EngineDropdown(label = "Model", value = "", loading = true)
        },
    ),
)

/**
 * Renders named engine states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 520)
@Preview(name = "Dark", widthDp = 400, heightDp = 520, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 520, fontScale = 1.5f)
@Composable
private fun EnginePreview(@PreviewParameter(EnginePreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}

/**
 * Creates checkpoint choices for the selected provider without model discovery.
 *
 * @param source Provider whose picker branch is exercised.
 * @author Dmitriy Moroz
 */
private fun engineState(source: ServerSource): EngineSelectionState {
    val modelType = when (source) {
        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> LocalAiModel.Type.Sdxl
        ServerSource.LOCAL_APPLE_CORE_ML -> LocalAiModel.Type.CoreMl
        ServerSource.LOCAL_MNN -> LocalAiModel.Type.Mnn
        ServerSource.LOCAL_APPLE_BONSAI -> LocalAiModel.Type.Bonsai
        else -> LocalAiModel.Type.ONNX
    }
    val models = listOf(
        LocalAiModel(
            "sample",
            modelType,
            "Sample model",
            "1 GB",
            emptyList(),
            downloaded = true
        )
    )
    return EngineSelectionState(
        loading = false,
        mode = source,
        sdModels = listOf("Sample checkpoint", "Another checkpoint"),
        selectedSdModel = "Sample checkpoint",
        swarmModels = listOf("Sample checkpoint"),
        selectedSwarmModel = "Sample checkpoint",
        hfModels = listOf("Sample model"),
        selectedHfModel = "Sample model",
        stEngines = listOf("Sample engine"),
        selectedStEngine = "Sample engine",
        easyDiffusionModels = listOf(EasyDiffusionModel("model-id", "Sample checkpoint")),
        selectedEasyDiffusionModel = "model-id",
        localAiModels = models,
        selectedLocalAiModelId = "sample",
    )
}
