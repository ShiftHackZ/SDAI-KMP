package com.shifthackz.aisdv1.presentation.components.widget.frame

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Frames preview content inside a clipped portrait phone illustration.
 * Controls inside the illustration use Compose rendering to follow its density and clipping.
 *
 * @param modifier Size, aspect ratio and placement supplied by the preview page.
 * @param content UI demonstration drawn inside the decorative phone bezel.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun PhoneFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .background(Color.Black, RoundedCornerShape(24.dp))
            .border(
                border = BorderStroke(6.dp, Color.Black),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
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
                    .padding(top = 8.dp)
                    .width(54.dp)
                    .height(8.dp)
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
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    color = Color.Black,
                    shape = RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp,
                    ),
                ),
        )
    }
}
