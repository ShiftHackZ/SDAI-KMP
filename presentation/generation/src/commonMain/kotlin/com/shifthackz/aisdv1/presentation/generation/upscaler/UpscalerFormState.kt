package com.shifthackz.aisdv1.presentation.generation.upscaler

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel

/**
 * Independent upscaler availability displayed in standalone and generation forms.
 *
 * @param platform Host selecting local-provider artwork.
 * @param isSupported Whether the provider catalog allows any upscaler on this platform.
 * @param models Dedicated catalog used by the reusable parameters form.
 * @param model Selected dedicated model, including current download completeness.
 * @param isLoading Whether the initial configuration read is still pending.
 * @param error Configuration read failure, displayed without treating it as a valid selection.
 * @author Dmitriy Moroz
 */
data class UpscalerFormState(
    val isSupported: Boolean, val platform: Platform, val model: LocalAiModel? = null,
    val models: List<LocalAiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) : MviState
