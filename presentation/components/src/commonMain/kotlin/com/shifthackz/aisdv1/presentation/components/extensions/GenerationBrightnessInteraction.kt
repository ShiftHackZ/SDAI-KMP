package com.shifthackz.aisdv1.presentation.components.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Reports interaction to the application-owned dimming timer. Null disables observation when
 * foreground dimming is unavailable; descendants never own or persist the brightness preference.
 *
 * @author Dmitriy Moroz
 */
val LocalGenerationBrightnessInteraction = compositionLocalOf<(() -> Unit)?> { null }

/**
 * Observes touches before child controls without consuming their pointer events. Press, release
 * and movement while pressed restart the dimming timer, including while brightness is restored.
 *
 * @receiver Surface whose interactions temporarily restore generation brightness.
 * @return The receiver with passive pointer observation when foreground dimming is enabled.
 * @author Dmitriy Moroz
 */
@Composable
fun Modifier.generationBrightnessInteraction(): Modifier {
    val onInteraction = LocalGenerationBrightnessInteraction.current ?: return this
    return pointerInput(onInteraction) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type == PointerEventType.Press || event.type == PointerEventType.Release ||
                    event.changes.any { it.pressed && it.position != it.previousPosition }
                ) {
                    onInteraction()
                }
            }
        }
    }
}
