package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Edits text using the platform's outlined-field adapter while keeping the text value caller-owned.
 * The platform implementation owns native focus/keyboard integration and reports edits through the callback.
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
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    containerColor: Color,
    textColor: Color,
    hintColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
)
