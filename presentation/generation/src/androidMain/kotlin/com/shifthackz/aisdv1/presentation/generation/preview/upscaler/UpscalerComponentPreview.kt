package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewTheme
import com.shifthackz.aisdv1.presentation.generation.preview.upscaler.UpscalerComponentPreviewCase.Kind
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerEnableCard
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerForm
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerOutputSizeFields
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerParametersContent
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerProviderCard

/**
 * Uses real upscaler components inside the same app theme as generation previews.
 *
 * @param scenario Component, catalog, disclosure and captured scale to display.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerComponentPreview(scenario: UpscalerComponentPreviewCase) {
    GenerationPreviewTheme {
        Box(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            when (scenario.kind) {
                Kind.PARAMETERS -> UpscalerParametersContent(
                    configuration = scenario.configuration,
                    models = scenario.formState.models,
                    onConfigurationChange = {},
                    isExpanded = scenario.isExpanded,
                    onToggleExpanded = {},
                    isEnabled = scenario.isEnabled,
                    isStandalone = scenario.isStandalone,
                    outputSize = scenario.outputSize,
                )

                Kind.FORM -> UpscalerForm(
                    state = scenario.formState, configuration = scenario.configuration,
                    onConfigurationChange = {}, onConfigure = {},
                    isEnabled = scenario.isEnabled, isStandalone = scenario.isStandalone,
                    outputSize = scenario.outputSize,
                )

                Kind.PROVIDER -> UpscalerProviderCard(
                    platform = scenario.formState.platform,
                    source = scenario.configuration?.source, onConfigure = {},
                )

                Kind.ENABLE -> UpscalerEnableCard(
                    isChecked = scenario.configuration != null, isEnabled = scenario.isEnabled,
                    onCheckedChange = {},
                )

                Kind.OUTPUT -> UpscalerOutputSizeFields(size = scenario.outputSize)
            }
        }
    }
}
