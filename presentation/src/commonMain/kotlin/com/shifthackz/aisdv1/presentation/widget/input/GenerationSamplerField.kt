package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSection
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSubtree
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiSampler

/**
 * Displays sampler selection and an inline retry when AUTOMATIC1111 has no usable sampler list.
 * Loading feedback replaces the empty dropdown while the current attempt is running.
 *
 * @param mode Provider controlling sampler loading and retry feedback.
 * @param selectedSampler Current sampler name.
 * @param availableSamplers Choices supplied by the provider.
 * @param isLoadingSamplers Whether the current sampler request is pending.
 * @param hasSamplerLoadFailed Whether retry feedback should be displayed.
 * @param onEvent Receives sampler selection and retry actions for the owning generation screen.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationSamplerField(
    mode: ServerSource,
    selectedSampler: String,
    availableSamplers: List<String>,
    isLoadingSamplers: Boolean,
    hasSamplerLoadFailed: Boolean,
    onEvent: (GenerationInputFormEvent) -> Unit,
) {
    UiDiagnosticSection(UiDiagnosticSubtree.SAMPLER_FIELD) {
        val isAutomatic1111 = mode == ServerSource.AUTOMATIC1111
        Column(modifier = Modifier.fillMaxWidth()) {
            DropdownTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                loading = isAutomatic1111 && isLoadingSamplers,
                label = Localization.string("hint_sampler").asUiText(),
                value = selectedSampler,
                items = availableSamplers,
                onItemSelected = { onEvent(GenerationInputFormEvent.UpdateSampler(it)) },
                displayDelegate = { value ->
                    if (value == StabilityAiSampler.NONE.toString()) {
                        Localization.string("hint_autodetect").asUiText()
                    } else {
                        value.asUiText()
                    }
                },
            )
            if (isAutomatic1111 && hasSamplerLoadFailed) {
                Text(
                    text = Localization.string("error_samplers_unavailable"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(
                    enabled = !isLoadingSamplers,
                    onClick = { onEvent(GenerationInputFormEvent.RetrySamplers) },
                ) {
                    Text(Localization.string("retry"))
                }
            }
        }
    }
}
