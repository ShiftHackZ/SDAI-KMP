package com.shifthackz.aisdv1.presentation.components.widget.connectivity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.measureTextWidth
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Display state of the selected provider connection before, during and after its first status check.
 *
 * @author Dmitriy Moroz
 */
enum class ConnectivityStatus {
    Uninitialized,
    Connected,
    Disconnected,
}

/**
 * Maps provider connectivity state to localized text and semantic status colors.
 *
 * @param state Connection result and provider-controlled indicator visibility.
 * @param modifier Layout modifiers for the status indicator.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ConnectivityWidget(
    state: ConnectivityState,
    modifier: Modifier = Modifier,
) {
    ConnectivityComposableContent(
        modifier = modifier,
        text = when (state) {
            is ConnectivityState.Connected -> Localization.string("status_connected")
            is ConnectivityState.Disconnected -> Localization.string("status_disconnected")
            is ConnectivityState.Uninitialized -> Localization.string("status_communicating")
        },
        status = when (state) {
            is ConnectivityState.Connected -> ConnectivityStatus.Connected
            is ConnectivityState.Disconnected -> ConnectivityStatus.Disconnected
            is ConnectivityState.Uninitialized -> ConnectivityStatus.Uninitialized
        },
        containerColor = when (state) {
            is ConnectivityState.Connected -> MaterialTheme.colorScheme.primaryContainer
            is ConnectivityState.Disconnected -> MaterialTheme.colorScheme.errorContainer
            is ConnectivityState.Uninitialized -> MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = when (state) {
            is ConnectivityState.Connected -> MaterialTheme.colorScheme.onPrimaryContainer
            is ConnectivityState.Disconnected -> MaterialTheme.colorScheme.onErrorContainer
            is ConnectivityState.Uninitialized -> MaterialTheme.colorScheme.onSecondaryContainer
        },
        visible = state.enabled,
    )
}

/**
 * Displays an animated connection status label, shimmering until the first result is known.
 *
 * @param text Already localized connection status.
 * @param status Selects the initial-check shimmer behavior.
 * @param containerColor Status-label background.
 * @param contentColor Status-label foreground.
 * @param visible Whether this provider/build should display the indicator.
 * @param modifier Layout modifiers for the visibility container.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ConnectivityComposableContent(
    text: String,
    status: ConnectivityStatus,
    containerColor: Color,
    contentColor: Color,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val targetOffset = with(LocalDensity.current) {
        measureTextWidth(text = text).toPx()
    }

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "offset",
    )
    AnimatedVisibility(modifier = modifier, visible = visible) {
        Column(
            modifier = Modifier.padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier
                    .drawWithCache {
                        val brushSize = 40f
                        val brushAnimated = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.5f),
                                Color.Transparent,
                            ),
                            start = Offset(offset, offset),
                            end = Offset(offset + brushSize, offset + brushSize),
                            tileMode = TileMode.Clamp,
                        )
                        onDrawBehind {
                            drawRoundRect(
                                color = containerColor,
                                cornerRadius = CornerRadius(8.dp.toPx()),
                            )
                            if (status != ConnectivityStatus.Uninitialized) return@onDrawBehind
                            drawRoundRect(
                                cornerRadius = CornerRadius(8.dp.toPx()),
                                brush = brushAnimated,
                            )
                        }
                    }
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                text = text,
                color = contentColor,
            )
        }
    }
}
