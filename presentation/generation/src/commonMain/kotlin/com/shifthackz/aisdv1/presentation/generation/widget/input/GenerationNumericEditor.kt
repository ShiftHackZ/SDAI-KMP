package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.presentation.components.widget.input.SliderTextInputField

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.sliderColors

/**
 * Renders all numeric advanced parameters through one structural call site. Compatible recycled
 * lazy items can update this editor's values instead of switching between separate field branches.
 *
 * @param parameters Current label, value, limits and precision for this logical field.
 * @param onValueChange Receives an applied numeric edit for the owning screen event mapper.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationNumericEditor(
    parameters: GenerationNumericFieldParameters,
    onValueChange: (Float) -> Unit,
) {
    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = Localization.string(parameters.labelKey, parameters.labelValue),
    )
    SliderTextInputField(
        value = parameters.value,
        valueRange = parameters.minimum..parameters.maximum,
        valueDiff = parameters.valueDiff,
        fractionDigits = parameters.fractionDigits,
        steps = parameters.steps,
        sliderColors = sliderColors,
        onValueChange = onValueChange,
    )
}
