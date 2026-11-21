package com.shifthackz.aisdv1.presentation.app

import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.NavigationEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavigationMotionTest {
    @Test
    fun `given a push, a new foreground retains its predecessor, expected forward animation`() {
        // Given
        val home = NavigationEntry(1, AppRoute.Settings)
        val backup = NavigationEntry(2, AppRoute.Backup())

        // When
        val motion = navigationMotion(listOf(home), listOf(home, backup))

        // Then
        assertEquals(NavigationMotion(home, backup, false), motion)
    }

    @Test
    fun `given a pop, the foreground is removed, expected predecessor reveal`() {
        // Given
        val home = NavigationEntry(1, AppRoute.Settings)
        val backup = NavigationEntry(2, AppRoute.Backup())

        // When
        val motion = navigationMotion(listOf(home, backup), listOf(home))

        // Then
        assertEquals(NavigationMotion(backup, home, true), motion)
    }

    @Test
    fun `given a consumed screen, its entry is replaced, expected no preview of invalid history`() {
        // Given
        val setup = NavigationEntry(1, AppRoute.Setup())
        val loader = NavigationEntry(2, AppRoute.ConfigurationLoader)

        // When
        val motion = navigationMotion(listOf(setup), listOf(loader))

        // Then
        assertNull(motion)
    }
}
