package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Edits a bounded value with Material on Android and the system slider on iOS.
 * Shared state remains authoritative; discrete stops include both range endpoints.
 *
 * @param value Current caller-owned value.
 * @param onValueChange Receives continuous or stop-aligned user changes.
 * @param modifier Placement of the complete control and its touch target.
 * @param isEnabled Whether the value can be changed.
 * @param valueRange Inclusive value bounds.
 * @param steps Number of intermediate stops, or zero for continuous movement.
 * @param colors Existing Material colors; iOS uses the app accent and native neutral track.
 * @param onValueChangeFinished Called after the user finishes a gesture.
 * @param contentDescription Localized parameter name for accessibility.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors(),
    onValueChangeFinished: (() -> Unit)? = null,
    contentDescription: String? = null,
)
