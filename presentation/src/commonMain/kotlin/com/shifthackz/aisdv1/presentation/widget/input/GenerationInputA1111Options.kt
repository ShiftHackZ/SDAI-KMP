package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.Scheduler

/**
 * Renders one AUTOMATIC1111-specific advanced field. Nested Hires and ADetailer settings are
 * separate lazy items, so their offscreen text fields are not mounted when a switch is pressed.
 *
 * @param field Stable logical field identity selected by the owning lazy list.
 * @param state Current AUTOMATIC1111 values and available modules.
 * @param isImg2Img Whether text-only Forge and Hires options are excluded.
 * @param onEvent Receives field edits and extension actions for the generation screen.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationInputA1111Option(
    field: GenerationFormField,
    state: GenerationInputAdvancedOptionsState,
    isImg2Img: Boolean,
    onEvent: (GenerationInputFormEvent) -> Unit,
) {
    when (field) {
        GenerationFormField.SCHEDULER -> SchedulerSection(
            config = state.selectedScheduler,
            onConfigChange = { onEvent(GenerationInputFormEvent.UpdateScheduler(it)) },
        )

        GenerationFormField.FORGE_MODULES -> if (!isImg2Img) {
            MultiSelectDropdownField(
                modifier = Modifier.padding(top = 8.dp),
                label = Localization.string("hint_forge_modules").asUiText(),
                selectedItems = state.selectedForgeModules,
                availableItems = state.availableForgeModules,
                onSelectionChanged = { onEvent(GenerationInputFormEvent.UpdateForgeModules(it)) },
                displayDelegate = { module -> module.name.ifBlank { module.path }.asUiText() },
            )
        }

        GenerationFormField.HIRES_ENABLED,
        GenerationFormField.HIRES_UPSCALER -> HiresOption(
            field = field,
            config = state.hires,
            onConfigChange = { onEvent(GenerationInputFormEvent.UpdateHiresConfig(it)) },
        )

        GenerationFormField.ADETAILER_ENABLED,
        GenerationFormField.ADETAILER_MODEL -> ADetailerOption(
            field = field,
            config = state.aDetailer,
            onConfigChange = { onEvent(GenerationInputFormEvent.UpdateADetailerConfig(it)) },
        )

        GenerationFormField.ADETAILER_MISSING -> ADetailerMissingContent(
            refreshing = state.isADetailerRefreshing,
            onInstallClick = { onEvent(GenerationInputFormEvent.OpenADetailerInstallInstructions) },
            onRefreshClick = { onEvent(GenerationInputFormEvent.RefreshADetailerAvailability) },
        )

        else -> Unit
    }
}

/**
 * Renders scheduler controls in the SDAI presentation layer.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @param config config rendered or processed by the component.
 * @param onConfigChange callback invoked by the component.
 * @author Dmitriy Moroz
 */
@Composable
private fun SchedulerSection(
    modifier: Modifier = Modifier,
    config: Scheduler,
    onConfigChange: (Scheduler) -> Unit,
) {
    DropdownTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        label = Localization.string("hint_scheduler").asUiText(),
        value = config,
        items = Scheduler.entries,
        onItemSelected = onConfigChange,
        displayDelegate = { it.displayName.asUiText() },
    )
}

/**
 * Renders one high-resolution control without constructing its offscreen siblings.
 *
 * @param field Logical field to display from the expanded option list.
 * @param config Current persisted-in-form provider configuration.
 * @param onConfigChange Receives a copy with only the edited configuration value changed.
 * @author Dmitriy Moroz
 */
@Composable
private fun HiresOption(
    field: GenerationFormField,
    config: HiresConfig,
    onConfigChange: (HiresConfig) -> Unit,
) {
    when (field) {
        GenerationFormField.HIRES_ENABLED -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { onConfigChange(config.copy(enabled = it)) },
                )
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = Localization.string("hint_hires_enabled"),
                )
            }
        }

        GenerationFormField.HIRES_UPSCALER -> {
            DropdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = Localization.string("hint_hires_upscaler").asUiText(),
                value = config.upscaler,
                items = HiresConfig.AVAILABLE_UPSCALERS,
                onItemSelected = { onConfigChange(config.copy(upscaler = it)) },
                displayDelegate = { it.asUiText() },
            )
        }

        else -> Unit
    }
}

/**
 * Renders one ADetailer control without constructing its offscreen siblings.
 *
 * @param field Logical field to display from the expanded option list.
 * @param config Current persisted-in-form provider configuration.
 * @param onConfigChange Receives a copy with only the edited configuration value changed.
 * @author Dmitriy Moroz
 */
@Composable
private fun ADetailerOption(
    field: GenerationFormField,
    config: ADetailerConfig,
    onConfigChange: (ADetailerConfig) -> Unit,
) {
    when (field) {
        GenerationFormField.ADETAILER_ENABLED -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { onConfigChange(config.copy(enabled = it)) },
                )
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = Localization.string("hint_adetailer_enabled"),
                )
            }
        }

        GenerationFormField.ADETAILER_MODEL -> {
            DropdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = Localization.string("hint_adetailer_model").asUiText(),
                value = config.model,
                items = ADetailerConfig.AVAILABLE_MODELS,
                onItemSelected = { onConfigChange(config.copy(model = it)) },
                displayDelegate = { it.asUiText() },
            )
        }

        else -> Unit
    }
}

/**
 * Renders ADetailer missing state in the SDAI presentation layer.
 *
 * @param refreshing refreshing value consumed by the API.
 * @param onInstallClick callback invoked by the component.
 * @param onRefreshClick callback invoked by the component.
 * @author Dmitriy Moroz
 */
@Composable
private fun ADetailerMissingContent(
    refreshing: Boolean,
    onInstallClick: () -> Unit,
    onRefreshClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = Localization.string("hint_adetailer_not_installed"),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                onClick = onInstallClick,
            ) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = Localization.string("action_install_instructions"),
                )
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                enabled = !refreshing,
                onClick = onRefreshClick,
            ) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                }
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = Localization.string("action_refresh"),
                )
            }
        }
    }
}
