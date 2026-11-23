package com.shifthackz.aisdv1.presentation.donate

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.shifthackz.aisdv1.core.extensions.fadedEdge
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative

/**
 * Fades supporter rows into the page canvas while keeping native iOS glass in the view hierarchy.
 * An offscreen alpha mask cannot punch native interop cutouts through the final Compose canvas,
 * so iOS draws the equivalent page-color gradient above the content instead.
 *
 * @param gradientOffset Distance from the top that remains fully visible before fading begins.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun Modifier.donateSupporterListFade(gradientOffset: Dp): Modifier {
    if (!isPlatformToolbarNative) return fadedEdge(gradientOffset = gradientOffset)
    val background = MaterialTheme.colorScheme.background
    return drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, background),
                startY = gradientOffset.toPx(),
            ),
        )
    }
}
