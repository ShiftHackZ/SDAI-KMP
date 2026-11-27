package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.presentation.components.widget.input.DropdownTextField
import com.shifthackz.aisdv1.presentation.components.widget.section.ExpandableSectionHeader
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Parameter-card content shared by the Home tab and both generation pipelines.
 * Model changes affect this submission without changing the saved generation provider.
 * The content and its bottom padding expand and shrink together below the persistent header.
 *
 * @param configuration Model captured by the owning form.
 * @param models Dedicated models with current download completeness.
 * @param onConfigurationChange Receives the selected downloaded model.
 * @param isExpanded Whether parameter fields are visible below the persistent header.
 * @param onToggleExpanded Requests the opposite disclosure state.
 * @param modifier Placement of the complete parameters card.
 * @param isEnabled Whether parameters can be edited during this screen's current operation.
 * @param isStandalone Whether oversized input is downscaled instead of rejected at generation submission.
 * @param outputSize Predicted standalone output dimensions; null means input or configuration is incomplete.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerParametersContent(
    configuration: UpscalerConfiguration?, models: List<LocalAiModel>,
    onConfigurationChange: (UpscalerConfiguration?) -> Unit,
    isExpanded: Boolean, onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier, isEnabled: Boolean = true, isStandalone: Boolean = false,
    outputSize: UpscalerImageSize? = null,
) {
    val downloaded = models.filter { it.downloaded }
    PlatformSurface(
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            ExpandableSectionHeader(
                title = Localization.string("upscaler_options"), isExpanded = isExpanded,
                onToggleExpanded = onToggleExpanded
            )
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            ) {
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = Localization.string("upscaler_model").asUiText(),
                        value = downloaded.firstOrNull { it.id == configuration?.modelId },
                        items = downloaded, isEnabled = isEnabled && downloaded.isNotEmpty(),
                        onItemSelected = {
                            onConfigurationChange(
                                UpscalerConfiguration(
                                    it.id,
                                    scaleFactor = configuration?.scaleFactor
                                        ?: UpscalerSource.forModel(it.id).nativeScaleFactor
                                )
                            )
                        },
                        displayDelegate = { it.name.asUiText() })
                    DropdownTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = Localization.string("upscaler_scale_factor").asUiText(),
                        value = configuration?.scaleFactor,
                        items = configuration?.source?.supportedScaleFactors?.sorted().orEmpty(),
                        isEnabled = isEnabled && configuration != null,
                        onItemSelected = { factor ->
                            onConfigurationChange(
                                configuration?.copy(
                                    scaleFactor = factor
                                )
                            )
                        },
                        displayDelegate = { factor -> "×$factor".asUiText() },
                    )
                    if (isStandalone) UpscalerOutputSizeFields(size = outputSize)
                    if (isStandalone) Text(
                        Localization.string("upscaler_limits"),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (downloaded.isEmpty()) Text(
                        Localization.string("upscaler_setup_required"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
