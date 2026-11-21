package com.shifthackz.aisdv1.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NavigationEntryTest {
    @Test
    fun `given a navigation preview observer, a push occurs before detachment, expected outgoing snapshot before mutation only`() {
        // Given
        val router = RootAppRouter(AppRoute.Settings)
        val observed = mutableListOf<Pair<NavigationEntry, List<NavigationEntry>>>()
        val detach = router.observeBeforeCover { observed.add(it to router.backStack.value) }
        val initial = router.backStack.value

        // When
        router.navigateToBackup()
        detach()
        router.navigateToDebugMenu()

        // Then
        assertEquals(listOf(initial.single() to initial), observed)
        assertEquals(AppRoute.Debug, router.route.value)
    }

    @Test
    fun `given navigation from a drawer, the destination is opened, expected one preview before the drawer covers the route`() {
        // Given
        val router = RootAppRouter(AppRoute.TextToImage)
        val previews = mutableListOf<Boolean>()
        router.observeBeforeCover { previews.add(router.drawerOpen.value) }

        // When
        router.openDrawer()
        router.navigateToServerSetup()

        // Then
        assertEquals(listOf(false), previews)
        assertEquals(AppRoute.Setup(LaunchSource.SETTINGS), router.route.value)
    }

    @Test
    fun `given retained navigation, a screen is pushed and popped, expected original entry and snapshot survive`() {
        // Given
        val router = RootAppRouter(AppRoute.Settings)
        val original = router.backStack.value

        // When
        router.navigateToBackup()
        val pushed = router.backStack.value
        router.navigateBack()

        // Then
        assertEquals(1, original.size)
        assertEquals(original.single(), pushed.first())
        assertNotEquals(pushed.first().id, pushed.last().id)
        assertEquals(original, router.backStack.value)
    }

    @Test
    fun `given home navigation, another peer tab is selected, expected replacement identity without added history`() {
        // Given
        val router = RootAppRouter(AppRoute.TextToImage)
        val initialId = router.backStack.value.single().id

        // When
        router.navigateToSettings()

        // Then
        assertEquals(1, router.backStack.value.size)
        assertEquals(AppRoute.Settings, router.backStack.value.single().route)
        assertNotEquals(initialId, router.backStack.value.single().id)
    }

    @Test
    fun `given repeated destinations, a detail is opened above a report, expected distinct screen lifetimes`() {
        // Given
        val router = RootAppRouter(AppRoute.Gallery)
        router.navigateToGalleryDetails(42L)
        val firstDetail = router.backStack.value.last()
        router.navigateToReportImage(42L)

        // When
        router.navigateToGalleryDetails(42L)

        // Then
        assertEquals(firstDetail.route, router.backStack.value.last().route)
        assertNotEquals(firstDetail.id, router.backStack.value.last().id)
    }

    @Test
    fun `given an open drawer, Back is requested, expected unchanged screen identities`() {
        // Given
        val router = RootAppRouter(AppRoute.Settings)
        router.navigateToBackup()
        val entries = router.backStack.value
        router.openDrawer()

        // When
        router.navigateBack()

        // Then
        assertEquals(entries, router.backStack.value)
        assertEquals(false, router.drawerOpen.value)
    }
}
