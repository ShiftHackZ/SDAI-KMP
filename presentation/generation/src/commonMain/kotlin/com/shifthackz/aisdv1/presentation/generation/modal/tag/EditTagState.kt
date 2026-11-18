package com.shifthackz.aisdv1.presentation.generation.modal.tag

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.common.math.roundTo
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.presentation.components.model.ExtraType

/**
 * Captured prompt pair and editable tag used to preview and confirm a single-tag replacement.
 *
 * @param prompt Positive prompt captured before editing.
 * @param negativePrompt Negative prompt captured before editing.
 * @param originalTag Exact source tag text replaced or removed on confirmation.
 * @param currentTag Current edited tag text.
 * @param extraType Recognized LoRA/hypernetwork category, or null for a plain tag.
 * @param isNegative Whether replacement applies to the negative prompt.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class EditTagState(
    val prompt: String = "",
    val negativePrompt: String = "",
    val originalTag: String = "",
    val currentTag: String = "",
    val extraType: ExtraType? = null,
    val isNegative: Boolean = false,
) : MviState {

    val currentValue: Double?
        get() {
            if (extraType == null) return null
            return currentTag.getExtraDoubleValue()
        }
}


private fun String?.getExtraDoubleValue(): Double? = this
    ?.replace("<", "")
    ?.replace(">", "")
    ?.split(":")
    ?.lastOrNull()
    ?.toDoubleOrNull()

/**
 * Replaces the weight of a three-part angle-bracket extra tag, rounded to two decimals.
 * Unrecognized shapes are returned unchanged.
 *
 * @param value New numeric weight for the extra tag.
 * @receiver Extra tag formatted as <type:name:weight>.
 * @return Updated extra tag or the unchanged input when its structure is unsupported.
 *
 * @author Dmitriy Moroz
 */
fun String.replaceExtraValue(value: Double): String {
    val parts = this.replace("<", "")
        .replace(">", "")
        .split(":")
    if (parts.size != 3) return this
    val result = buildString {
        append("<")
        append(parts.take(2).joinToString(":"))
        append(":${value.roundTo(2)}")
        append(">")
    }
    return result
        .replace(".0", "")
        .replace(",0", "")
}
