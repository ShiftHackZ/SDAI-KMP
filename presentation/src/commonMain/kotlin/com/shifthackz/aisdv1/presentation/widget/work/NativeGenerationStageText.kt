package com.shifthackz.aisdv1.presentation.widget.work

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage

/**
 * Maps a sanitized native stage to the current application language.
 *
 * @author Dmitriy Moroz
 */
internal fun NativeGenerationStage.localizedText(): String = Localization.string(
    when (this) {
        NativeGenerationStage.PREPARING -> "native_generation_stage_preparing"
        NativeGenerationStage.TOKENIZING -> "native_generation_stage_tokenizing"
        NativeGenerationStage.DENOISING -> "native_generation_stage_denoising"
        NativeGenerationStage.DECODING -> "native_generation_stage_decoding"
        NativeGenerationStage.RUNNING -> "native_generation_stage_running"
        NativeGenerationStage.WRITING_RESULT -> "native_generation_stage_writing_result"
    }
)
