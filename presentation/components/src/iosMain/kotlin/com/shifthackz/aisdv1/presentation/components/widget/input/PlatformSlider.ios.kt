@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Embeds UISlider with its native thumb, gestures and adjustable accessibility.
 * Tutorial illustrations stay in Compose so their scaled phone frames clip correctly.
 *
 * @param value Caller-owned value synchronized into UIKit without emitting another change.
 * @param onValueChange Receives bounded, stop-aligned user values.
 * @param modifier Placement of the native control.
 * @param isEnabled Whether native input is enabled.
 * @param valueRange Inclusive slider bounds.
 * @param steps Number of intermediate stops.
 * @param colors Material preview colors; UIKit uses the selected app accent.
 * @param onValueChangeFinished Gesture completion callback.
 * @param contentDescription Localized parameter label announced by VoiceOver.
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
    if (LocalPlatformControlPreview.current) {
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
        return
    }
    val boundsModifier = modifier.fillMaxWidth().height(48.dp)
    if (!LocalMviVisibility.current) {
        Spacer(boundsModifier)
        return
    }
    val theme = MaterialTheme.colorScheme
    val backing = LocalPlatformControlBackgroundColor.current ?: theme.background
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    UIKitView(
        modifier = boundsModifier,
        factory = { NativeSliderView() },
        update = { view ->
            view.update(
                value, valueRange, steps, isEnabled,
                theme.background.luminance() < 0.5f, isRtl,
                theme.primary.toUIColor(), backing.toUIColor(),
                contentDescription ?: Localization.string("hint_value"),
                onValueChange, onValueChangeFinished,
            )
        },
        onRelease = { it.onValueChange = null; it.onFinished = null },
        properties = UIKitInteropProperties(
            interactionMode = if (isEnabled) UIKitInteropInteractionMode.Cooperative() else null,
            isNativeAccessibilityEnabled = true,
        ),
    )
}
