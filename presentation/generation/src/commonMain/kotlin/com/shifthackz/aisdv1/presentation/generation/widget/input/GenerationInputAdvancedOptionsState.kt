package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.FalAiAcceleration
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset

/**
 * Values consumed by advanced generation controls, independent of prompt and result updates.
 *
 * Lists retain the owning screen's snapshots. No immutability promise is made for their types;
 * the form remembers structurally equal projections to preserve their container identity.
 *
 * @param mode Provider whose controls and parameter limits are displayed.
 * @param isVisible Whether advanced controls are expanded.
 * @param samplingSteps Requested sampling steps before provider-specific display clamping.
 * @param cfgScale Requested guidance scale before provider-specific display clamping.
 * @param shouldRestoreFaces Whether AUTOMATIC1111 face restoration is enabled.
 * @param seed Seed text preserved for editing, including incomplete values.
 * @param subSeed Variation seed text for supported providers.
 * @param seedValidationError Owning screen's seed validation feedback.
 * @param subSeedValidationError Owning screen's variation seed validation feedback.
 * @param subSeedStrength Variation seed strength.
 * @param selectedSampler Selected sampler name.
 * @param selectedScheduler Selected AUTOMATIC1111 scheduler.
 * @param availableForgeModules Available AUTOMATIC1111 Forge modules.
 * @param selectedForgeModules Selected AUTOMATIC1111 Forge modules.
 * @param availableSamplers Sampler choices from the current provider.
 * @param isLoadingSamplers Whether the current sampler request is pending.
 * @param hasSamplerLoadFailed Whether the sampler field offers retry feedback.
 * @param selectedStylePreset Selected Stability AI style.
 * @param selectedClipGuidancePreset Selected Stability AI CLIP guidance preset.
 * @param falAiModel Fal model defining guidance, step and acceleration capabilities.
 * @param falAiAcceleration Selected Fal acceleration mode.
 * @param isFalAiSyncEnabled Whether Fal synchronous delivery is enabled.
 * @param isNsfwEnabled Provider-specific safety setting.
 * @param batchCount Requested batch size before provider-specific display clamping.
 * @param hires AUTOMATIC1111 high-resolution settings.
 * @param aDetailer AUTOMATIC1111 ADetailer settings.
 * @param isADetailerAvailable Whether ADetailer is available on the configured server.
 * @param isADetailerRefreshing Whether ADetailer availability is being refreshed.
 *
 * @author Dmitriy Moroz
 */
internal data class GenerationInputAdvancedOptionsState(
    val mode: ServerSource,
    val isVisible: Boolean,
    val samplingSteps: Int,
    val cfgScale: Float,
    val shouldRestoreFaces: Boolean,
    val seed: String,
    val subSeed: String,
    val seedValidationError: UiText?,
    val subSeedValidationError: UiText?,
    val subSeedStrength: Float,
    val selectedSampler: String,
    val selectedScheduler: Scheduler,
    val availableForgeModules: List<ForgeModule>,
    val selectedForgeModules: List<ForgeModule>,
    val availableSamplers: List<String>,
    val isLoadingSamplers: Boolean,
    val hasSamplerLoadFailed: Boolean,
    val selectedStylePreset: StabilityAiStylePreset,
    val selectedClipGuidancePreset: StabilityAiClipGuidance,
    val falAiModel: FalAiModel,
    val falAiAcceleration: FalAiAcceleration,
    val isFalAiSyncEnabled: Boolean,
    val isNsfwEnabled: Boolean,
    val batchCount: Int,
    val hires: HiresConfig,
    val aDetailer: ADetailerConfig,
    val isADetailerAvailable: Boolean,
    val isADetailerRefreshing: Boolean,
)
