package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset

/**
 * Composes one visible logical advanced field. The owning lazy list determines visibility and
 * disposes offscreen controls; no hidden provider form is retained or eagerly constructed.
 *
 * @param field Stable logical identity selected by the provider-specific item list.
 * @param state Current generation values and validation feedback.
 * @param isImg2Img Whether text-only provider controls are unavailable.
 * @param onEvent Receives edits for the owning generation screen.
 * @param afterSlidersSection Additional slider content supplied by image-to-image generation.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationInputAdvancedOption(
    field: GenerationFormField,
    state: GenerationInputAdvancedOptionsState,
    isImg2Img: Boolean,
    onEvent: (GenerationInputFormEvent) -> Unit,
    afterSlidersSection: @Composable () -> Unit,
) {
    when (field.contentType()) {
        GenerationFormContentType.SEED_EDITOR -> {
            val isVariationSeed = field == GenerationFormField.VARIATION_SEED
            GenerationSeedEditor(
                value = if (isVariationSeed) state.subSeed else state.seed,
                labelKey = if (isVariationSeed) "hint_sub_seed" else "hint_seed",
                validationError = if (isVariationSeed) state.subSeedValidationError else state.seedValidationError,
                onValueChange = { value ->
                    onEvent(
                        if (isVariationSeed) GenerationInputFormEvent.UpdateSubSeed(value)
                        else GenerationInputFormEvent.UpdateSeed(value),
                    )
                },
            )
        }

        GenerationFormContentType.SLIDER_EDITOR -> GenerationNumericEditor(
            parameters = field.numericParameters(state),
            onValueChange = { value -> onEvent(field.numericChangeEvent(value, state)) },
        )

        else -> GenerationInputOtherAdvancedOption(
            field,
            state,
            isImg2Img,
            onEvent,
            afterSlidersSection
        )
    }
}

/**
 * Renders nonnumeric provider controls after reusable seed and numeric editors have been selected.
 *
 * @param field Logical provider field to display.
 * @param state Current provider values and validation feedback.
 * @param isImg2Img Whether text-only provider controls are excluded.
 * @param onEvent Receives edits for the owning generation screen.
 * @param afterSlidersSection Additional image-to-image denoising controls.
 * @author Dmitriy Moroz
 */
@Composable
private fun GenerationInputOtherAdvancedOption(
    field: GenerationFormField,
    state: GenerationInputAdvancedOptionsState,
    isImg2Img: Boolean,
    onEvent: (GenerationInputFormEvent) -> Unit,
    afterSlidersSection: @Composable () -> Unit,
) {
    when (field) {
        GenerationFormField.SAMPLER -> {
            // Sampler selection only supported for A1111, STABILITY AI
            when (state.mode) {
                ServerSource.STABILITY_AI,
                ServerSource.ARLI_AI,
                ServerSource.AUTOMATIC1111 -> GenerationSamplerField(
                    mode = state.mode,
                    selectedSampler = state.selectedSampler,
                    availableSamplers = state.availableSamplers,
                    isLoadingSamplers = state.isLoadingSamplers,
                    hasSamplerLoadFailed = state.hasSamplerLoadFailed,
                    onEvent = onEvent,
                )

                else -> Unit
            }
        }

        GenerationFormField.STYLE -> {
            DropdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = Localization.string("hint_style_preset").asUiText(),
                value = state.selectedStylePreset,
                items = StabilityAiStylePreset.entries,
                onItemSelected = {
                    onEvent(GenerationInputFormEvent.UpdateStabilityAiStyle(it))
                },
                displayDelegate = { value ->
                    if (value == StabilityAiStylePreset.NONE) {
                        Localization.string("hint_autodetect").asUiText()
                    } else {
                        value.key.asUiText()
                    }
                },
            )
        }

        GenerationFormField.CLIP_GUIDANCE -> {
            DropdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = Localization.string("hint_clip_guidance_preset").asUiText(),
                value = state.selectedClipGuidancePreset,
                items = StabilityAiClipGuidance.entries,
                onItemSelected = {
                    onEvent(GenerationInputFormEvent.UpdateStabilityAiClipGuidance(it))
                },
            )
        }

        GenerationFormField.FAL_ACCELERATION -> {
            DropdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = Localization.string("hint_fal_ai_acceleration").asUiText(),
                value = state.falAiAcceleration,
                items = state.falAiModel.supportedAccelerations.toList(),
                onItemSelected = {
                    onEvent(GenerationInputFormEvent.UpdateFalAiAcceleration(it))
                },
                displayDelegate = { it.displayName.asUiText() },
            )
        }

        GenerationFormField.FAL_SYNC -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = state.isFalAiSyncEnabled,
                    onCheckedChange = {
                        onEvent(GenerationInputFormEvent.UpdateFalAiSyncMode(it))
                    },
                )
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = Localization.string("hint_fal_ai_sync_mode"),
                )
            }
        }

        GenerationFormField.NSFW -> {
            // NSFW flag maps to provider-specific safety settings.
            if (
                state.mode == ServerSource.HORDE ||
                state.mode == ServerSource.FAL_AI ||
                state.mode == ServerSource.LOCAL_APPLE_CORE_ML
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = state.isNsfwEnabled,
                        onCheckedChange = { onEvent(GenerationInputFormEvent.UpdateNsfw(it)) },
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = Localization.string("hint_nsfw"),
                    )
                }
            }
        }

        GenerationFormField.AFTER_SLIDERS -> {
            afterSlidersSection()
        }

        GenerationFormField.RESTORE_FACES -> {
            //Restore faces available only for A1111
            if (state.mode == ServerSource.AUTOMATIC1111) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = state.shouldRestoreFaces,
                        onCheckedChange = {
                            onEvent(GenerationInputFormEvent.UpdateRestoreFaces(it))
                        },
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = Localization.string("hint_restore_faces"),
                    )
                }
            }
        }

        else -> GenerationInputA1111Option(field, state, isImg2Img, onEvent)
    }
}
