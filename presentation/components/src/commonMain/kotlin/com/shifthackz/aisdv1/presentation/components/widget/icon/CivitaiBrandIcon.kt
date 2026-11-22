package com.shifthackz.aisdv1.presentation.components.widget.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Returns Civitai's square favicon mark using the official badge geometry and blue palette.
 * Source: https://github.com/civitai/civitai/tree/main/packages/civitai-brand/src
 *
 * @author Dmitriy Moroz
 */
internal fun civitaiBrandIcon(): ImageVector = civitaiIcon

/**
 * Retains the three original badge layers so Image can preserve the brand colors in either app theme.
 *
 * @author Dmitriy Moroz
 */
private val civitaiIcon: ImageVector by lazy {
    ImageVector.Builder("Brand.Civitai", 24.dp, 24.dp, 22.7f, 22.7f).apply {
        addGroup(translationX = 1f)
        addPath(
            pathData = PathParser().parsePathString("M1.5,6.6v10l8.7,5l8.7-5v-10l-8.7-5L1.5,6.6z")
                .toNodes(),
            fill = Brush.linearGradient(
                listOf(Color(0xFF081692), Color(0xFF1E043C)),
                Offset(10.156f, 1.55f),
                Offset(10.156f, 21.5386f)
            ),
        )
        addPath(
            pathData = PathParser().parsePathString("M10.2,4.7l5.9,3.4V15l-5.9,3.4L4.2,15V8.1L10.2,4.7 M10.2,1.6l-8.7,5v10l8.7,5l8.7-5v-10C18.8,6.6,10.2,1.6,10.2,1.6z")
                .toNodes(),
            pathFillType = PathFillType.EvenOdd,
            fill = Brush.linearGradient(
                listOf(Color(0xFF1284F7), Color(0xFF0A20C9)),
                Offset(10.156f, 1.55f),
                Offset(10.156f, 21.55f)
            ),
        )
        addPath(
            pathData = PathParser().parsePathString("M11.8,12.4l-1.7,1l-1.7-1v-1.9l1.7-1l1.7,1h2.1V9.3l-3.8-2.2L6.4,9.3v4.3l3.8,2.2l3.8-2.2v-1.2H11.8z")
                .toNodes(),
            fill = SolidColor(Color.White),
        )
        clearGroup()
    }.build()
}
