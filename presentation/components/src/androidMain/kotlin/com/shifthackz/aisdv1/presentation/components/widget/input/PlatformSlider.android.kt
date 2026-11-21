package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Retains the existing Material slider layout, colors, stop behavior and gestures.
 *
 * @param value Current shared value.
 * @param onValueChange Publishes user changes.
 * @param modifier Existing placement and sizing.
 * @param isEnabled Whether interaction is available.
 * @param valueRange Inclusive bounds.
 * @param steps Intermediate stops.
 * @param colors Existing caller-supplied Material colors.
 * @param onValueChangeFinished Gesture completion callback.
 * @param contentDescription Parameter label retained by the surrounding Android form.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    isEnabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    colors: SliderColors,
    onValueChangeFinished: (() -> Unit)?,
    contentDescription: String?,
) {
    Slider(
        value,
        onValueChange,
        modifier,
        isEnabled,
        valueRange,
        steps,
        onValueChangeFinished,
        colors
    )
}
