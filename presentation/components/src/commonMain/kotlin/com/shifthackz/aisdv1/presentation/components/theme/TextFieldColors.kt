package com.shifthackz.aisdv1.presentation.components.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

/**
 * Shared outlined-field colors with theme borders, primary cursor and a translucent error background.
 *
 * @author Dmitriy Moroz
 */
val textFieldColors: TextFieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        errorBorderColor = MaterialTheme.colorScheme.error,
        cursorColor = MaterialTheme.colorScheme.primary,
    )
