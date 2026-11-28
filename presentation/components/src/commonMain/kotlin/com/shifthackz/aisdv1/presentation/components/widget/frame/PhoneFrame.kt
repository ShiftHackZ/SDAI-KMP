package com.shifthackz.aisdv1.presentation.components.widget.frame

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * Frames preview content inside a clipped phone or foldable display illustration.
 * Controls inside the illustration use Compose rendering to follow its density and clipping.
 * The bezel follows the measured preview when a short window limits its aspect-ratio content.
 *
 * @param modifier Size, aspect ratio and placement supplied by the preview page.
 * @param isLandscape Whether the bezel and speaker sit beside a wider display rather than above it.
 * @param content UI demonstration drawn inside the decorative phone bezel.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun PhoneFrame(
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    Layout(
        modifier = modifier.wrapContentSize()
            .background(Color.Black, RoundedCornerShape(24.dp))
            .border(
                border = BorderStroke(6.dp, Color.Black),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp)),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .alpha(0.5f)
                        .padding(if (isLandscape) PaddingValues(start = 8.dp) else PaddingValues(top = 8.dp))
                        .width(if (isLandscape) 8.dp else 54.dp)
                        .height(if (isLandscape) 54.dp else 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.35f)),
                    )
                }
            }
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            border = BorderStroke(6.dp, Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(6.dp),
                ) {
                    CompositionLocalProvider(LocalPlatformControlPreview provides true) {
                        content()
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp,
                        ),
                    ),
            )
        },
    ) { measurables, constraints ->
        val topHeight = 18.dp.roundToPx()
        val bottomHeight = 12.dp.roundToPx()
        val chromeHeight = topHeight + bottomHeight
        val preview = measurables[1].measure(
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
                maxWidth = if (isLandscape && constraints.hasBoundedWidth) {
                    (constraints.maxWidth - chromeHeight).coerceAtLeast(0)
                } else constraints.maxWidth,
                maxHeight = if (!isLandscape && constraints.hasBoundedHeight) {
                    (constraints.maxHeight - chromeHeight).coerceAtLeast(0)
                } else constraints.maxHeight,
            )
        )
        if (isLandscape) {
            val leading = measurables[0].measure(Constraints.fixed(topHeight, preview.height))
            val trailing = measurables[2].measure(Constraints.fixed(bottomHeight, preview.height))
            layout(preview.width + chromeHeight, preview.height) {
                leading.placeRelative(0, 0)
                preview.placeRelative(topHeight, 0)
                trailing.placeRelative(topHeight + preview.width, 0)
            }
        } else {
            val top = measurables[0].measure(Constraints.fixed(preview.width, topHeight))
            val bottom = measurables[2].measure(Constraints.fixed(preview.width, bottomHeight))
            layout(preview.width, preview.height + chromeHeight) {
                top.placeRelative(0, 0)
                preview.placeRelative(0, topHeight)
                bottom.placeRelative(0, topHeight + preview.height)
            }
        }
    }
}
