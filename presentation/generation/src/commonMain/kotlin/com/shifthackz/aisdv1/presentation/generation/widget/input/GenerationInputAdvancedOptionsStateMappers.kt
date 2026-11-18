package com.shifthackz.aisdv1.presentation.generation.widget.input

/**
 * Projects only fields rendered by advanced controls and their sampler, batch and A1111 children.
 * Prompt edits, output changes and screen progress therefore do not change this value.
 *
 * @receiver Current generation form snapshot; its validation results are preserved.
 *
 * @author Dmitriy Moroz
 */
internal fun GenerationInputFormState.toAdvancedOptionsState(): GenerationInputAdvancedOptionsState =
    GenerationInputAdvancedOptionsState(
        mode = mode,
        isVisible = advancedOptionsVisible,
        samplingSteps = samplingSteps,
        cfgScale = cfgScale,
        shouldRestoreFaces = restoreFaces,
        seed = seed,
        subSeed = subSeed,
        seedValidationError = seedValidationError,
        subSeedValidationError = subSeedValidationError,
        subSeedStrength = subSeedStrength,
        selectedSampler = selectedSampler,
        selectedScheduler = selectedScheduler,
        availableForgeModules = availableForgeModules,
        selectedForgeModules = selectedForgeModules,
        availableSamplers = availableSamplers,
        isLoadingSamplers = isLoadingSamplers,
        hasSamplerLoadFailed = hasSamplerLoadFailed,
        selectedStylePreset = selectedStylePreset,
        selectedClipGuidancePreset = selectedClipGuidancePreset,
        falAiModel = falAiModel,
        falAiAcceleration = falAiAcceleration,
        isFalAiSyncEnabled = falAiSyncMode,
        isNsfwEnabled = nsfw,
        batchCount = batchCount,
        hires = hires,
        aDetailer = aDetailer,
        isADetailerAvailable = aDetailerAvailable,
        isADetailerRefreshing = aDetailerRefreshing,
    )
