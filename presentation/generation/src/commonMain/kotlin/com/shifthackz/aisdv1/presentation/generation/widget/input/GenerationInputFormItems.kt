package com.shifthackz.aisdv1.presentation.generation.widget.input

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.section.ExpandableSectionHeader
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionComponent

/**
 * Emits each basic and advanced control as its own lazy item, followed by the status footer.
 * Stable provider-qualified keys preserve each field's saveable state while scrolling.
 * Expansion uses lazy-item fade and placement transitions rather than measuring an entire
 * offscreen Column before the first expansion frame.
 * The advanced toggle and its fields remain independent when the provider offers that toggle.
 * Otherwise the generation-options disclosure owns the complete form. Status feedback stays visible in either state.
 *
 * @receiver The screen's existing vertical lazy list; no nested scrolling list is created.
 *
 * @param state Current provider inputs and advanced visibility.
 * @param isImg2Img Whether to use image-to-image fields and surface styling.
 * @param promptChipTextFieldState Caller-owned prompt selection and chip editing state.
 * @param negativePromptChipTextFieldState Caller-owned negative-prompt selection and chip state.
 * @param onEvent Receives generation field edits and provider extension actions.
 * @param isExpanded Whether generation options are visible.
 * @param onToggleExpanded Toggles generation options without changing the independent advanced state.
 * @param afterSlidersSection Image-to-image denoising controls inserted at their existing position.
 * @param hasFooter Whether validation, status or progress content needs its own segment when fields are collapsed.
 * @param footer Validation, status and progress content beneath all form fields.
 * @param engineContent Checkpoint picker supplied by the owning form.
 *
 * @author Dmitriy Moroz
 */
internal fun LazyListScope.generationInputFormItems(
    state: GenerationInputFormState,
    isImg2Img: Boolean,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    onEvent: (GenerationInputFormEvent) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    afterSlidersSection: @Composable () -> Unit = {},
    hasFooter: Boolean = false,
    footer: @Composable () -> Unit = {},
    engineContent: @Composable (Modifier) -> Unit = { EngineSelectionComponent(modifier = it) },
) {
    val baseFields = generationInputBaseFields(state)
    val hasIndependentAdvancedToggle = GenerationBaseFormField.ADVANCED_TOGGLE in baseFields
    val hasVisibleBody = isExpanded || hasIndependentAdvancedToggle
    item(
        key = "generation-${state.mode.name}-header",
        contentType = GenerationFormContentType.HEADER
    ) {
        GenerationFormCardSegment(
            isImg2Img = isImg2Img,
            isFirst = true,
            isHeader = true,
            isLast = !hasVisibleBody && !hasFooter,
        ) {
            ExpandableSectionHeader(
                title = Localization.string("title_generation_options"),
                isExpanded = isExpanded,
                onToggleExpanded = onToggleExpanded,
            )
        }
    }
    baseFields.filter { field ->
        isExpanded || field == GenerationBaseFormField.ADVANCED_TOGGLE
    }.forEach { field ->
        item(
            key = "generation-${state.mode.name}-base-${field.name}",
            contentType = field.contentType()
        ) {
            GenerationFormCardSegment(isImg2Img = isImg2Img, modifier = Modifier.animateItem()) {
                GenerationInputBaseOption(
                    field = field,
                    state = state,
                    isImg2Img = isImg2Img,
                    textFieldContainerColor = if (isImg2Img) {
                        MaterialTheme.colorScheme.surface
                    } else MaterialTheme.colorScheme.surfaceVariant,
                    promptChipTextFieldState = promptChipTextFieldState,
                    negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                    onEvent = onEvent,
                    engineContent = engineContent,
                )
            }
        }
    }
    if (isExpanded || hasIndependentAdvancedToggle) generationInputAdvancedItems(
        state = state.toAdvancedOptionsState(),
        isImg2Img = isImg2Img,
        onEvent = onEvent,
        afterSlidersSection = afterSlidersSection,
    )
    if (hasVisibleBody || hasFooter) item(
        key = "generation-footer",
        contentType = GenerationFormContentType.FOOTER
    ) {
        GenerationFormCardSegment(
            isImg2Img = isImg2Img,
            isLast = true,
            modifier = Modifier.animateItem(),
        ) { footer() }
    }
}

/**
 * Emits advanced fields directly into an existing list. All provider visibility decisions are
 * made before composition; each item mounts only its own logical field.
 *
 * @receiver The generation screen's vertical lazy list.
 *
 * @param state Current advanced values and provider capabilities.
 * @param isImg2Img Whether text-only Forge and Hires controls are excluded.
 * @param onEvent Receives generation edits from the visible field.
 * @param afterSlidersSection Additional image-to-image denoising content.
 *
 * @author Dmitriy Moroz
 */
internal fun LazyListScope.generationInputAdvancedItems(
    state: GenerationInputAdvancedOptionsState,
    isImg2Img: Boolean,
    onEvent: (GenerationInputFormEvent) -> Unit,
    afterSlidersSection: @Composable () -> Unit,
) {
    generationInputAdvancedFields(state, isImg2Img).forEach { field ->
        item(
            key = "generation-${state.mode.name}-${field.name}",
            contentType = field.contentType()
        ) {
            val rememberedState = remember(state) { state }
            GenerationFormCardSegment(
                isImg2Img = isImg2Img,
                modifier = Modifier.animateItem(),
            ) {
                GenerationInputAdvancedOption(
                    field,
                    rememberedState,
                    isImg2Img,
                    onEvent,
                    afterSlidersSection
                )
            }
        }
    }
}
