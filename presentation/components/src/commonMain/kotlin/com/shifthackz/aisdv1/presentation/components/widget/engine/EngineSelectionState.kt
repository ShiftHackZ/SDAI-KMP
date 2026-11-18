package com.shifthackz.aisdv1.presentation.components.widget.engine

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Provider-specific checkpoint choices, selected identifiers and loading state used by the shared model picker.
 *
 * @param loading Whether the selected provider catalog is being loaded.
 * @param mode Provider whose model choices are displayed.
 * @param sdModels Available AUTOMATIC1111 checkpoint titles and selection state.
 * @param selectedSdModel Selected AUTOMATIC1111 checkpoint title.
 * @param swarmModels Available SwarmUI checkpoint choices.
 * @param easyDiffusionModels Latest successfully fetched checkpoint catalog for the selected Easy Diffusion endpoint.
 * @param easyDiffusionUrl Endpoint that supplied the displayed checkpoint choices.
 * @param easyDiffusionSelectionRevision Provider revision that owns the displayed checkpoint choices.
 * @param selectedEasyDiffusionModel Exact selected Easy Diffusion checkpoint identifier.
 * @param hasEasyDiffusionModelLoadFailed Whether the latest strict checkpoint refresh failed; the UI offers an explicit retry.
 * @param selectedSwarmModel Selected SwarmUI checkpoint name.
 * @param hfModels Supported Hugging Face model choices.
 * @param selectedHfModel Selected supported Hugging Face model alias.
 * @param stEngines Available Stability AI engines.
 * @param selectedStEngine Selected Stability AI engine identifier.
 * @param localAiModels Downloaded local model choices for the active engine.
 * @param selectedLocalAiModelId Selected downloaded local model identifier.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class EngineSelectionState(
    val loading: Boolean = true,
    val mode: ServerSource = ServerSource.AUTOMATIC1111,
    val sdModels: List<String> = emptyList(),
    val selectedSdModel: String = "",
    val swarmModels: List<String> = emptyList(),
    val easyDiffusionUrl: String = "",
    val easyDiffusionSelectionRevision: Long = -1L,
    val easyDiffusionModels: List<EasyDiffusionModel> = emptyList(),
    val selectedEasyDiffusionModel: String = "",
    val hasEasyDiffusionModelLoadFailed: Boolean = false,
    val selectedSwarmModel: String = "",
    val hfModels: List<String> = emptyList(),
    val selectedHfModel: String = "",
    val stEngines: List<String> = emptyList(),
    val selectedStEngine: String = "",
    val localAiModels: List<LocalAiModel> = emptyList(),
    val selectedLocalAiModelId: String = "",
) : MviState
