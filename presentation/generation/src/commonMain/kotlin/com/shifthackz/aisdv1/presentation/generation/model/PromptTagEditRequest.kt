package com.shifthackz.aisdv1.presentation.generation.model

import androidx.compose.runtime.Immutable

/**
 * Captured prompt context for a modal editor replacing one tag without changing the parent form yet.
 *
 * @param prompt Positive prompt captured when editing starts.
 * @param negativePrompt Negative prompt captured when editing starts.
 * @param tag Exact source tag text to replace or delete.
 * @param isNegative Whether the source tag belongs to the negative prompt.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class PromptTagEditRequest(

    val prompt: String,

    val negativePrompt: String,

    val tag: String,

    val isNegative: Boolean,
)
