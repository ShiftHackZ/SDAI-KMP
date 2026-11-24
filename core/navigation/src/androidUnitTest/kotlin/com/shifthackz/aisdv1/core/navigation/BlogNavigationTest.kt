package com.shifthackz.aisdv1.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Protects the native Blog route and its reader's return path to Settings.
 *
 * @author Dmitriy Moroz
 */
class BlogNavigationTest {

    @Test
    fun `given root router on Settings, blog and versioned post are opened and dismissed, expected article list and Settings are restored in order`() {
        // Given
        val router = RootAppRouter(AppRoute.Settings)
        val slug = "1.3.0-release"

        // When
        router.navigateToBlog()
        val list = router.route.value
        router.navigateToBlogDetail(slug)
        val reader = router.route.value
        router.navigateBack()
        val restoredList = router.route.value
        router.navigateBack()

        // Then
        assertEquals(AppRoute.Blog, list)
        assertEquals(AppRoute.BlogDetail(slug), reader)
        assertEquals(AppRoute.Blog, restoredList)
        assertEquals(AppRoute.Settings, router.route.value)
    }
}
