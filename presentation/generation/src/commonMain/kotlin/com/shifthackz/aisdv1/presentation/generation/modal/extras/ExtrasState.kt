package com.shifthackz.aisdv1.presentation.generation.modal.extras

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.presentation.components.model.ExtraType

/**
 * Provider extra catalog and proposed prompts before confirmation by the generation form.
 *
 * @param loading Whether the selected extra catalog is loading.
 * @param source Provider whose catalog/capabilities are shown.
 * @param error Catalog-load error shown by the selector.
 * @param prompt Proposed positive prompt with toggled extras.
 * @param negativePrompt Negative prompt retained for confirmation.
 * @param type LoRA/hypernetwork category currently selected.
 * @param loras Extra entries and their current positive-prompt selection/weights.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class ExtrasState(
    val loading: Boolean = true,
    val source: ServerSource = ServerSource.AUTOMATIC1111,
    val error: ErrorState = ErrorState.None,
    val prompt: String = "",
    val negativePrompt: String = "",
    val type: ExtraType = ExtraType.Lora,
    val loras: List<ExtraItemUi> = emptyList(),
) : MviState

/**
 * One selectable LoRA or hypernetwork with its prompt alias and current weight.
 *
 * @param type Extra category controlling prompt syntax.
 * @param key Stable editor-list identity for this catalog entry.
 * @param name Provider display/catalog name.
 * @param alias Provider prompt alias, or null when the name is the alias.
 * @param isApplied Whether the extra is present in the proposed positive prompt.
 * @param value Existing textual weight parsed from the positive prompt, if present.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class ExtraItemUi(
    val type: ExtraType,
    val key: String,
    val name: String,
    val alias: String?,
    val isApplied: Boolean,
    val value: String? = null,
)
