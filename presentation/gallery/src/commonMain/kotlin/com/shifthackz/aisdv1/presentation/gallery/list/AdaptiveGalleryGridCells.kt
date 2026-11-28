package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/**
 * Preserves the selected column count in compact windows and its thumbnail density when unfolded.
 * Uses the grid's measured space, so split-screen and navigation rails need no device-name checks.
 *
 * @param compactColumns User-selected column count for the folded phone layout.
 * @author Dmitriy Moroz
 */
internal class AdaptiveGalleryGridCells(private val compactColumns: Int) : GridCells {
    /**
     * Distributes all available pixels across columns while retaining the compact density choice.
     *
     * @param availableSize Measured grid width after content padding, in pixels.
     * @param spacing Pixel gap between adjacent cells.
     * @return Cell widths whose sum plus the gaps exactly fills the measured grid.
     * @author Dmitriy Moroz
     */
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val columns = if (availableSize < 568.dp.roundToPx()) compactColumns else {
            val compactCellSize =
                (328.dp.roundToPx() - spacing * (compactColumns - 1)) / compactColumns
            maxOf(compactColumns, (availableSize + spacing) / (compactCellSize + spacing))
        }
        return with(GridCells.Fixed(columns)) {
            calculateCrossAxisCellSizes(availableSize, spacing)
        }
    }
}
