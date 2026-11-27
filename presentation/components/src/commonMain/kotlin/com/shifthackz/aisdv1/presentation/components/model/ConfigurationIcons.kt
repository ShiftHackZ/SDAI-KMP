package com.shifthackz.aisdv1.presentation.components.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.unit.dp

/**
 * Shared generation-setup artwork for Settings and drawer navigation.
 *
 * @author Dmitriy Moroz
 */
val generationConfigurationIcon: ImageVector get() = Icons.Default.Tune

/**
 * Marks upscaler configuration with a small upper-right gear, distinct from the Upscaler destination.
 * The cutout beneath the badge keeps both shapes readable at navigation-icon sizes.
 *
 * @author Dmitriy Moroz
 */
val upscalerConfigurationIcon: ImageVector by lazy {
    ImageVector.Builder("UpscalerConfiguration", 24.dp, 24.dp, 24f, 24f).apply {
        addGroup(
            clipPathData = PathParser().parsePathString("M0,0 H12 V12 H24 V24 H0 Z").toNodes(),
        )
        appendMaterialPaths(Icons.Default.PhotoSizeSelectLarge)
        clearGroup()
        addGroup(scaleX = 0.5f, scaleY = 0.5f, translationX = 12f)
        appendMaterialPaths(Icons.Default.Settings)
        clearGroup()
    }.build()
}

/**
 * Adds the filled paths of a Material icon inside the builder's current transform and clipping group.
 *
 * @param icon Filled Material artwork whose root contains the source paths.
 * @author Dmitriy Moroz
 */
private fun ImageVector.Builder.appendMaterialPaths(icon: ImageVector) {
    icon.root.filterIsInstance<VectorPath>().forEach { path ->
        addPath(
            pathData = path.pathData,
            pathFillType = path.pathFillType,
            fill = SolidColor(Color.Black),
        )
    }
}
