package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.runtime.Immutable

/**
 * Display values for one numeric generation parameter, independent of the rest of the form.
 * This model contains no callbacks; the owning field maps edits back to its existing screen event.
 *
 * @param labelKey Localization key for the parameter heading.
 * @param labelValue Formatted current value substituted into the heading.
 * @param value Applied value rendered by the editor and slider.
 * @param minimum Inclusive supported lower bound.
 * @param maximum Inclusive supported upper bound.
 * @param valueDiff Increment or decrement applied by the numeric step buttons.
 * @param fractionDigits Decimal precision used when displaying and applying edits.
 * @param steps Number of intermediate slider stops; zero allows continuous movement.
 * @author Dmitriy Moroz
 */
@Immutable
internal data class GenerationNumericFieldParameters(
    val labelKey: String,
    val labelValue: String,
    val value: Float,
    val minimum: Float,
    val maximum: Float,
    val valueDiff: Float = 1f,
    val fractionDigits: Int = 2,
    val steps: Int = 0,
)
