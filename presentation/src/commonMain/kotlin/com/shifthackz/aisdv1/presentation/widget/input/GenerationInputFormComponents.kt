package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.theme.sliderColors
import com.shifthackz.aisdv1.presentation.theme.textFieldColors
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.BATCH_RANGE_FAL_AI_MAX
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.BATCH_RANGE_MAX
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.BATCH_RANGE_MIN
import com.shifthackz.aisdv1.presentation.widget.input.chip.ChipTextFieldEvent
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Displays provider-bounded batch size without depending on prompt or other form values.
 *
 * @param mode Provider determining the supported batch range.
 * @param requestedBatchCount Current batch size before display clamping.
 * @param onEvent callback invoked by the component.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationBatchComponent(
    mode: ServerSource,
    requestedBatchCount: Int,
    onEvent: (GenerationInputFormEvent) -> Unit,
) {
    val maxBatchCount = if (mode == ServerSource.FAL_AI) {
        BATCH_RANGE_FAL_AI_MAX
    } else {
        BATCH_RANGE_MAX
    }
    val batchCount = requestedBatchCount.coerceIn(BATCH_RANGE_MIN, maxBatchCount)
    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = Localization.string("hint_batch", "$batchCount"),
    )
    SliderTextInputField(
        value = batchCount * 1f,
        valueRange = (BATCH_RANGE_MIN * 1f)..(maxBatchCount * 1f),
        valueDiff = 1f,
        fractionDigits = 0,
        steps = abs(BATCH_RANGE_MIN - maxBatchCount) - 1,
        sliderColors = sliderColors,
        onValueChange = {
            onEvent(GenerationInputFormEvent.UpdateBatch(it.roundToInt()))
        },
    )
}

/**
 * Displays validated pixel dimensions and aspect-ratio actions independently of other form values.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @param width Current editable width text.
 * @param height Current editable height text.
 * @param widthValidationError Owning screen's width validation feedback.
 * @param heightValidationError Owning screen's height validation feedback.
 * @param onEvent callback invoked by the component.
 * @author Dmitriy Moroz
 */
@Composable
internal fun RowScope.GenerationSizeTextFieldsComponent(
    modifier: Modifier = Modifier,
    width: String,
    height: String,
    widthValidationError: UiText?,
    heightValidationError: UiText?,
    onEvent: (GenerationInputFormEvent) -> Unit,
) {
    var isRatioMenuExpanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier.padding(end = 4.dp),
        value = width,
        onValueChange = { value ->
            if (value.length <= 4) {
                onEvent(GenerationInputFormEvent.UpdateWidth(value.filter { it.isDigit() }))
            }
        },
        isError = widthValidationError != null,
        supportingText = {
            widthValidationError?.let {
                Text(
                    it.asString(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        label = { Text(Localization.string("width")) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = textFieldColors,
    )
    OutlinedTextField(
        modifier = modifier.padding(horizontal = 4.dp),
        value = height,
        onValueChange = { value ->
            if (value.length <= 4) {
                onEvent(GenerationInputFormEvent.UpdateHeight(value.filter { it.isDigit() }))
            }
        },
        isError = heightValidationError != null,
        supportingText = {
            heightValidationError?.let {
                Text(
                    it.asString(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        label = { Text(Localization.string("height")) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = textFieldColors,
    )
    Row(modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)) {
        IconButton(
            onClick = { onEvent(GenerationInputFormEvent.SwapDimensions) },
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = Localization.string("action_swap_dimensions"),
            )
        }
        Box {
            IconButton(
                onClick = { isRatioMenuExpanded = true },
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = Localization.string("action_aspect_ratio"),
                )
            }
            DropdownMenu(
                expanded = isRatioMenuExpanded,
                onDismissRequest = { isRatioMenuExpanded = false },
            ) {
                GenerationAspectRatio.entries.forEach { ratio ->
                    DropdownMenuItem(
                        text = { Text(ratio.displayName) },
                        onClick = {
                            isRatioMenuExpanded = false
                            onEvent(GenerationInputFormEvent.ApplyAspectRatio(ratio))
                        },
                    )
                }
            }
        }
    }
}


/**
 * Executes the `processTaggedPrompt` step in the SDAI presentation layer.
 *
 * @param keywords keywords value consumed by the API.
 * @param event event value consumed by the API.
 * @return Result produced by `processTaggedPrompt`.
 * @author Dmitriy Moroz
 */
internal fun processTaggedPrompt(
    keywords: List<String>,
    event: ChipTextFieldEvent<String>
): String {
    val newKeywords = when (event) {
        is ChipTextFieldEvent.Add -> buildList {
            addAll(keywords)
            add(event.item)
        }

        is ChipTextFieldEvent.AddBatch -> buildList {
            addAll(keywords)
            addAll(event.items)
        }

        is ChipTextFieldEvent.Remove -> keywords.filterIndexed { i, _ -> i != event.index }
        is ChipTextFieldEvent.Update -> keywords.mapIndexed { i, s -> if (i == event.index) event.item else s }
    }
    return newKeywords.joinToString(", ")
}
