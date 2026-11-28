package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.ui.unit.Density
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects thumbnail density and exact grid bounds while the real layout resizes between displays.
 *
 * @author Dmitriy Moroz
 */
class AdaptiveGalleryGridCellsTest {
    @Test
    fun `given compact gallery, user selects five columns, expected five columns retained`() {
        // Given
        val cells = AdaptiveGalleryGridCells(5)
        // When
        val sizes = with(cells) { Density(1f).calculateCrossAxisCellSizes(288, 8) }
        // Then
        assertEquals(5, sizes.size)
        assertEquals(288, sizes.sum() + 8 * (sizes.size - 1))
    }

    @Test
    fun `given expanded gallery, display unfolds, expected more thumbnails with no overflow`() {
        // Given
        val cells = AdaptiveGalleryGridCells(3)
        // When
        val sizes = with(cells) { Density(1f).calculateCrossAxisCellSizes(780, 8) }
        // Then
        assertTrue(sizes.size > 3)
        assertEquals(780, sizes.sum() + 8 * (sizes.size - 1))
    }

    @Test
    fun `given expanded gallery, display folds again, expected original column preference restored`() {
        // Given
        val cells = AdaptiveGalleryGridCells(4)
        with(cells) { Density(1f).calculateCrossAxisCellSizes(780, 8) }
        // When
        val sizes = with(cells) { Density(1f).calculateCrossAxisCellSizes(328, 8) }
        // Then
        assertEquals(4, sizes.size)
        assertEquals(328, sizes.sum() + 8 * (sizes.size - 1))
    }
}
