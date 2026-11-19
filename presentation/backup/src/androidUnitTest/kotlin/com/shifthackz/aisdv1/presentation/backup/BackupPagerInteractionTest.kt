package com.shifthackz.aisdv1.presentation.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupPagerInteractionTest {
    @Test
    fun `given create page, vertical scroll has no horizontal displacement, expected action remains available`() {
        // Given
        val selectedPage = 0
        // When
        val canInteract = canInteractWithBackupPage(selectedPage, 0, 0f)
        // Then
        assertTrue(canInteract)
    }

    @Test
    fun `given create page, incidental swipe stays below forty percent, expected action remains available`() {
        // Given
        val offsets = listOf(0.001f, 0.1f, 0.3999f)
        // When
        val availability = offsets.map { canInteractWithBackupPage(0, 0, it) }
        // Then
        assertTrue(availability.all { it })
    }

    @Test
    fun `given create page, swipe reaches forty percent, expected action pauses`() {
        // Given
        val offset = 0.4f
        // When
        val canInteract = canInteractWithBackupPage(0, 0, offset)
        // Then
        assertFalse(canInteract)
    }

    @Test
    fun `given create page still selected, nearest page flips during swipe, expected original action stays paused`() {
        // Given
        val newNearestPage = 1
        // When
        val canInteract = canInteractWithBackupPage(0, newNearestPage, -0.49f)
        // Then
        assertFalse(canInteract)
    }

    @Test
    fun `given restore page, reverse swipe stays below forty percent, expected action remains available`() {
        // Given
        val offsets = listOf(-0.001f, -0.1f, -0.3999f)
        // When
        val availability = offsets.map { canInteractWithBackupPage(1, 1, it) }
        // Then
        assertTrue(availability.all { it })
    }

    @Test
    fun `given restore page, reverse swipe reaches forty percent, expected action pauses`() {
        // Given
        val offset = -0.4f
        // When
        val canInteract = canInteractWithBackupPage(1, 1, offset)
        // Then
        assertFalse(canInteract)
    }

    @Test
    fun `given restore page still selected, nearest page flips on reverse swipe, expected original action stays paused`() {
        // Given
        val newNearestPage = 0
        // When
        val canInteract = canInteractWithBackupPage(1, newNearestPage, 0.49f)
        // Then
        assertFalse(canInteract)
    }

    @Test
    fun `given restore tab chosen, animation approaches selected page, expected action returns inside forty percent`() {
        // Given
        val selectedPage = 1
        // When
        val isPaused = !canInteractWithBackupPage(selectedPage, 0, 0f)
        val canInteract = canInteractWithBackupPage(selectedPage, 1, -0.3f)
        // Then
        assertTrue(isPaused)
        assertTrue(canInteract)
    }
}
