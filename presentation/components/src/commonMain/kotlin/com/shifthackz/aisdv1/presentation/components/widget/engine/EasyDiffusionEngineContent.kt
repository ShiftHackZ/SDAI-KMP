package com.shifthackz.aisdv1.presentation.components.widget.engine

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Shows the server's full checkpoint identifiers, fixed sampler and an explicit refresh path.
 *
 * @param state Latest successful catalog or visible fetch failure.
 * @param processIntent Sends selection and refresh requests through the injected view model.
 * @param modifier Placement supplied by the generation form.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun EasyDiffusionEngineContent(
    state: EngineSelectionState,
    processIntent: (EngineSelectionIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        DropdownTextField(
            label = Localization.string("hint_sd_model"), loading = state.loading,
            value = state.easyDiffusionModels.firstOrNull { it.id == state.selectedEasyDiffusionModel },
            items = state.easyDiffusionModels,
            onItemSelected = { processIntent(EngineSelectionIntent(it.id)) },
            displayDelegate = { if (it.name == it.id) it.id else "${it.name} (${it.id})" },
        )
        Text(
            Localization.string("easy_diffusion_sampler"),
            style = MaterialTheme.typography.bodySmall
        )
        if (!state.loading && (state.hasEasyDiffusionModelLoadFailed || state.easyDiffusionModels.isEmpty() ||
                    state.easyDiffusionModels.none { it.id == state.selectedEasyDiffusionModel })
        ) {
            Text(Localization.string(if (state.hasEasyDiffusionModelLoadFailed) "easy_diffusion_models_failed" else "easy_diffusion_models_empty"))
        }
        TextButton(
            enabled = !state.loading,
            onClick = { processIntent(EngineSelectionIntent(shouldRefresh = true)) }) {
            Text(Localization.string("easy_diffusion_refresh_models"))
        }
    }
}
