package com.shifthackz.aisdv1.presentation.components.widget.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import com.shifthackz.aisdv1.presentation.components.widget.surface.isPlatformSurfaceGlassAvailable

/**
 * Shows a selectable color swatch with an inset fill and a checkmark above its selection backing.
 * Android retains the flat swatch; supported iOS versions render glass inside the same fill bounds.
 * Native glass fills the inset swatch without adding another frame inside its bounds.
 * The caller owns selection and persists any resulting preference change.
 *
 * @param color Swatch fill and native glass tint.
 * @param isSelected Whether this choice has the selected backing and icon.
 * @param onClick Requests selection when enabled.
 * @param contentDescription Accessible name of the color or palette represented by this chip.
 * @param modifier Size and placement; unconstrained chips occupy 32dp square bounds.
 * @param iconTint Foreground of the selected icon.
 * @param selectedBorderTint Backing visible around the inset fill when selected.
 * @param unselectedBorderTint Backing visible around the inset fill when unselected.
 * @param borderSize Space reserved around the swatch fill, retaining the full interactive bounds.
 * @param shape Outline of both the backing and the inset swatch.
 * @param selectedIcon Symbol centered over the complete chip when selected.
 * @param iconSize Size of the selected symbol, independent of the fill inset.
 * @param isEnabled Whether touch and accessibility can request selection.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ColorIconChip(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconTint: Color = contentColorFor(color),
    selectedBorderTint: Color = color.copy(alpha = 0.4f),
    unselectedBorderTint: Color = Color.Transparent,
    borderSize: Dp = 4.dp,
    shape: Shape = CircleShape,
    selectedIcon: ImageVector = Icons.Default.Check,
    iconSize: Dp = 24.dp,
    isEnabled: Boolean = true,
) {
    val shouldUseNativeGlass = isPlatformSurfaceGlassAvailable()
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = if (isSelected) selectedBorderTint else unselectedBorderTint,
                shape = shape,
            )
            .clip(shape)
            .selectable(
                selected = isSelected,
                enabled = isEnabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        val fillModifier = Modifier.padding(borderSize).size(1000.dp)
        if (shouldUseNativeGlass) {
            PlatformSurface(
                modifier = fillModifier,
                color = color,
                shape = shape,
                glassInset = 0.dp,
            ) {}
        } else {
            Box(fillModifier.background(color = color, shape = shape).clip(shape))
        }
        if (isSelected) {
            Icon(
                imageVector = selectedIcon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = iconTint,
            )
        }
    }
}
