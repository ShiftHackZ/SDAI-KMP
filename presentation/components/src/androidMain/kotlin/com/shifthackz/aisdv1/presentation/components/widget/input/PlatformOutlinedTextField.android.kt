package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Edits caller-owned text with Compose Material's outlined field on Android.
 *  The platform implementation owns native focus/keyboard integration and reports edits through the callback.
 *
 * @param value Current untransformed text.
 * @param onValueChange Receives user-edited text for publication by the caller.
 * @param label Field caption or placeholder used by the platform adapter.
 * @param containerColor Background color for the input surface.
 * @param textColor Foreground color for entered text.
 * @param hintColor Foreground color for the caption/placeholder.
 * @param modifier Layout modifiers for the complete field.
 * @param enabled Whether text input and field actions are interactive.
 * @param error Optional validation message shown with error styling.
 * @param keyboardType Requested native keyboard category.
 * @param visualTransformation Display transformation; the underlying value remains untransformed.
 * @param trailingIcon Optional action/icon content placed at the field's trailing edge.
 * @param singleLine Whether editing is restricted to one visual text line.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    containerColor: Color,
    textColor: Color,
    hintColor: Color,
    modifier: Modifier,
    enabled: Boolean,
    error: String?,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation,
    trailingIcon: @Composable (() -> Unit)?,
    singleLine: Boolean,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let {
            {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            disabledTextColor = textColor.copy(alpha = 0.38f),
            errorTextColor = textColor,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = hintColor,
            disabledLabelColor = hintColor.copy(alpha = 0.38f),
            errorLabelColor = MaterialTheme.colorScheme.error,
            focusedPlaceholderColor = hintColor,
            unfocusedPlaceholderColor = hintColor,
            disabledPlaceholderColor = hintColor.copy(alpha = 0.38f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
