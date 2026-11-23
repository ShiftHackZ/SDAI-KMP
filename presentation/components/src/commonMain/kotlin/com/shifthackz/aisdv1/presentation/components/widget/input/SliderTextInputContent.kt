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
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.textFieldColors

/**
 * Renders the numeric editor draft and its applied slider value independently of draft persistence.
 *
 * @param value Applied parameter used by the slider and step buttons.
 * @param text Current editor draft.
 * @param isNotApplied Whether a typed change exposes Apply.
 * @param isValid Whether the draft can be applied.
 * @param onTextChange Receives editor changes.
 * @param onFocusChanged Reports editor focus transitions.
 * @param onValueChange Requests an applied numeric change.
 * @param modifier Bounds of the editor and slider.
 * @param valueRange Inclusive slider and editor bounds.
 * @param valueDiff Step-button increment.
 * @param fractionDigits Displayed decimal precision.
 * @param steps Number of intermediate slider stops.
 * @param sliderColors Slider thumb and track palette.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SliderTextInputContent(
    value: Float,
    text: String,
    isNotApplied: Boolean,
    isValid: Boolean,
    onTextChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueDiff: Float = 1f,
    fractionDigits: Int = 2,
    steps: Int = 0,
    sliderColors: SliderColors = SliderDefaults.colors(),
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .onFocusChanged { onFocusChanged(it.isFocused) },
            value = if (fractionDigits != 0) text else text.replace(".0", ""),
            onValueChange = onTextChange,
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
                    PrimaryButton(
                        modifier = Modifier.padding(end = 6.dp),
                        onClick = { text.toFloatOrNull()?.let(onValueChange) },
                        isEnabled = isValid,
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
                            onClick = { onValueChange(value - valueDiff) },
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
                            onClick = { onValueChange(value + valueDiff) },
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
        PlatformSlider(
            value = value,
            valueRange = valueRange,
            steps = steps,
            colors = sliderColors,
            onValueChange = { onValueChange(it) },
        )
    }
}
