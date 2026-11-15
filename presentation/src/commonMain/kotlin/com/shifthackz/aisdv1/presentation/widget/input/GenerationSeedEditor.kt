package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.presentation.theme.textFieldColors
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormConstants.RANDOM_SEED_BOUND_EXCLUSIVE
import kotlin.random.Random

/**
 * Renders seed and variation-seed controls through the same reusable editor structure.
 * The screen owns text and validation; clear and dice retain their existing seed event semantics.
 *
 * @param value Current seed text, including incomplete or invalid input.
 * @param labelKey Localization key distinguishing seed from variation seed.
 * @param validationError Feedback computed by the owning screen's seed validator.
 * @param onValueChange Receives typing, clear and web-compatible random-seed values.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationSeedEditor(
    value: String,
    labelKey: String,
    validationError: UiText?,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        value = value,
        onValueChange = onValueChange,
        label = { Text(Localization.string(labelKey)) },
        isError = validationError != null,
        supportingText = validationError?.let { error -> { Text(error.asString()) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            Row {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.string("action_clear"),
                        )
                    }
                }
                IconButton(onClick = {
                    onValueChange(Random.nextLong(RANDOM_SEED_BOUND_EXCLUSIVE).toString())
                }) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = Localization.string("action_image_picker_random"),
                    )
                }
            }
        },
        colors = textFieldColors,
    )
}
