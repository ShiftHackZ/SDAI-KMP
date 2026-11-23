package com.shifthackz.aisdv1.presentation.generation.preview

import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationBaseFormField
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormState
import com.shifthackz.aisdv1.presentation.generation.widget.input.generationInputAdvancedFields
import com.shifthackz.aisdv1.presentation.generation.widget.input.generationInputBaseFields
import com.shifthackz.aisdv1.presentation.generation.widget.input.toAdvancedOptionsState

/**
 * Resolves a preview anchor against the same provider-dependent lazy-item ordering as the real form.
 *
 * @param state Provider capabilities and advanced visibility selecting the actual field list.
 * @param anchor Base/advanced field key or footer to bring into view; empty starts at the provider card.
 * @param isImageToImage Whether the source-image card precedes the generation controls.
 * @param isExpanded Initial generation-options disclosure state.
 * @author Dmitriy Moroz
 */
internal fun generationPreviewAnchorIndex(
    state: GenerationInputFormState,
    anchor: String,
    isImageToImage: Boolean,
    isExpanded: Boolean,
): Int {
    val baseFields = generationInputBaseFields(state)
    val hasIndependentAdvancedToggle = GenerationBaseFormField.ADVANCED_TOGGLE in baseFields
    val keys = buildList {
        add("provider")
        if (isImageToImage) add("input")
        add("header")
        addAll(baseFields.filter { isExpanded || it == GenerationBaseFormField.ADVANCED_TOGGLE }
            .map { "base-${it.name}" })
        if (isExpanded || hasIndependentAdvancedToggle) {
            addAll(
                generationInputAdvancedFields(
                    state.toAdvancedOptionsState(),
                    isImageToImage
                ).map { "advanced-${it.name}" })
        }
        add("footer")
    }
    return keys.indexOf(anchor).coerceAtLeast(0)
}
