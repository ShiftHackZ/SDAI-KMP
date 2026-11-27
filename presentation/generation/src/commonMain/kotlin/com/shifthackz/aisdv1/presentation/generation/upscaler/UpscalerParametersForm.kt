package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize

/**
 * Collapsible upscaler parameters shared by the Home tab and both generation pipelines.
 * Model changes affect this submission without changing the saved generation provider.
 * The content and its bottom padding expand and shrink together below the persistent header.
 *
 * @param configuration Model captured by the owning form.
 * @param models Dedicated models with current download completeness.
 * @param onConfigurationChange Receives the selected downloaded model.
 * @param modifier Placement of the complete parameters card.
 * @param isEnabled Whether parameters can be edited during this screen's current operation.
 * @param isStandalone Whether oversized input is downscaled instead of rejected at generation submission.
 * @param outputSize Predicted standalone output dimensions; null means input or configuration is incomplete.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerParametersForm(
    configuration: UpscalerConfiguration?, models: List<LocalAiModel>,
    onConfigurationChange: (UpscalerConfiguration?) -> Unit,
    modifier: Modifier = Modifier, isEnabled: Boolean = true, isStandalone: Boolean = false,
    outputSize: UpscalerImageSize? = null,
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    UpscalerParametersContent(
        configuration = configuration, models = models,
        onConfigurationChange = onConfigurationChange,
        isExpanded = isExpanded, onToggleExpanded = { isExpanded = !isExpanded },
        modifier = modifier, isEnabled = isEnabled, isStandalone = isStandalone,
        outputSize = outputSize,
    )
}
