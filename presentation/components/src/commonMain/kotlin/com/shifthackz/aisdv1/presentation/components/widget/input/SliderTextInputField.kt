package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.math.roundTo
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.textFieldColors

/**
 * Displays a bounded numeric editor and slider. The float editor saves its draft by the owning
 * lazy item key, while actual parameter values remain owned by the generation screen.
 * Losing focus discards unapplied edits; external values synchronize when the editor is unfocused.
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
    var isFocused by remember { mutableStateOf(false) }
    val callback: (Float) -> Unit = { newValue ->
        newValue
            .coerceIn(valueRange)
            .roundTo(fractionDigits)
            .also { text = "$it" }
            .let(onValueChange::invoke)
    }
    val isNotApplied =
        text.toFloatOrNull()?.roundTo(fractionDigits) != value.roundTo(fractionDigits)
    val isValid = text.isNotBlank() && (text.toFloatOrNull()?.let { it in valueRange } ?: false)
    LaunchedEffect(isFocused, stateText) {
        if (!isFocused && lastStateText != stateText) {
            text = stateText
            lastStateText = stateText
        }
    }
    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused) text = stateText
                    isFocused = focusState.isFocused
                },
            value = if (fractionDigits != 0) text else text.replace(".0", ""),
            onValueChange = { newText ->
                text = newText
            },
            enabled = true,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            label = { Text(Localization.string("hint_value")) },
            trailingIcon = {
                if (isNotApplied) {
                    Button(
                        modifier = Modifier.padding(end = 6.dp),
                        onClick = { text.toFloatOrNull()?.let(callback::invoke) },
                        enabled = isValid,
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 4.dp),
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = LocalContentColor.current,
                        )
                        Text(
                            text = Localization.string("apply"),
                            color = LocalContentColor.current,
                        )
                    }
                } else {
                    Row {
                        val isDecrementEnabled = value > valueRange.start
                        val isIncrementEnabled = value < valueRange.endInclusive
                        IconButton(
                            enabled = isDecrementEnabled,
                            onClick = { callback(value - valueDiff) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = if (isDecrementEnabled) 1f else 0.5f,
                                ),
                            )
                        }
                        IconButton(
                            enabled = isIncrementEnabled,
                            onClick = { callback(value + valueDiff) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = if (isIncrementEnabled) 1f else 0.5f,
                                ),
                            )
                        }
                    }
                }
            },
            colors = textFieldColors,
        )
        Slider(
            value = value,
            valueRange = valueRange,
            steps = steps,
            colors = sliderColors,
            onValueChange = { callback(it) },
        )
    }
}
