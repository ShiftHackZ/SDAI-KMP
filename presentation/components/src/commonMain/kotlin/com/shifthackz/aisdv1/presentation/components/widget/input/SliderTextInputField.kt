package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.math.roundTo

/**
 * Displays a bounded numeric editor and slider. The float editor saves its draft by the owning
 * lazy item key, while actual parameter values remain owned by the generation screen.
 * Losing focus discards unapplied edits; external values synchronize when the editor is unfocused.
 * Only typed edits expose Apply; slider and step-button changes apply immediately.
 *
 * @param value Current applied numeric parameter supplied by the owning screen.
 * @param onValueChange Receives a clamped, rounded value from the slider, step buttons or Apply action.
 * @param modifier Layout and styling applied to the editor and slider container.
 * @param valueRange Inclusive bounds used to clamp edited and slider values.
 * @param valueDiff Amount added or subtracted by the step buttons.
 * @param fractionDigits Decimal precision used for displayed and applied parameter values.
 * @param steps Number of intermediate slider stops; zero allows continuous slider movement.
 * @param sliderColors Theme colors used for the slider thumb, track and tick marks.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SliderTextInputField(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Double> = 0.0..1.0,
    valueDiff: Double = 1.0,
    fractionDigits: Int = 2,
    steps: Int = 0,
    sliderColors: SliderColors = SliderDefaults.colors(),
) = SliderTextInputField(
    value = value.toFloat(),
    onValueChange = { onValueChange(it.toDouble()) },
    modifier = modifier,
    valueRange = (valueRange.start.toFloat())..(valueRange.endInclusive.toFloat()),
    valueDiff = valueDiff.toFloat(),
    fractionDigits = fractionDigits,
    steps = steps,
    sliderColors = sliderColors,
)

/**
 * Displays a bounded numeric editor and slider. The float editor saves its draft by the owning
 * lazy item key, while actual parameter values remain owned by the generation screen.
 * Losing focus discards unapplied edits; external values synchronize when the editor is unfocused.
 * Only typed edits expose Apply; slider and step-button changes apply immediately.
 *
 * @param value Current applied numeric parameter supplied by the owning screen.
 * @param onValueChange Receives a clamped, rounded value from the slider, step buttons or Apply action.
 * @param modifier Layout and styling applied to the editor and slider container.
 * @param valueRange Inclusive bounds used to clamp edited and slider values.
 * @param valueDiff Amount added or subtracted by the step buttons.
 * @param fractionDigits Decimal precision used for displayed and applied parameter values.
 * @param steps Number of intermediate slider stops; zero allows continuous slider movement.
 * @param sliderColors Theme colors used for the slider thumb, track and tick marks.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SliderTextInputField(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueDiff: Float = 1f,
    fractionDigits: Int = 2,
    steps: Int = 0,
    sliderColors: SliderColors = SliderDefaults.colors(),
) {
    val stateText = value.roundTo(fractionDigits).toString()
    var text by rememberSaveable { mutableStateOf(stateText) }
    var lastStateText by rememberSaveable { mutableStateOf(stateText) }
    var hasTypedEdit by rememberSaveable { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val callback: (Float) -> Unit = { newValue ->
        hasTypedEdit = false
        newValue
            .coerceIn(valueRange)
            .roundTo(fractionDigits)
            .also { text = "$it" }
            .let(onValueChange::invoke)
    }
    val isNotApplied =
        hasTypedEdit && text.toFloatOrNull()?.roundTo(fractionDigits) != value.roundTo(
            fractionDigits
        )
    val isValid = text.isNotBlank() && (text.toFloatOrNull()?.let { it in valueRange } ?: false)
    LaunchedEffect(isFocused, stateText) {
        if (!isFocused && lastStateText != stateText) {
            text = stateText
            lastStateText = stateText
            hasTypedEdit = false
        }
    }
    SliderTextInputContent(
        value = value,
        text = text,
        isNotApplied = isNotApplied,
        isValid = isValid,
        onTextChange = { text = it; hasTypedEdit = true },
        onFocusChanged = { hasFocus ->
            if (isFocused && !hasFocus) {
                text = stateText
                hasTypedEdit = false
            }
            isFocused = hasFocus
        },
        onValueChange = callback,
        modifier = modifier,
        valueRange = valueRange,
        valueDiff = valueDiff,
        fractionDigits = fractionDigits,
        steps = steps,
        sliderColors = sliderColors,
    )
}
