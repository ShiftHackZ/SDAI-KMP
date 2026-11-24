package com.shifthackz.aisdv1.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks that upscaler configuration and saved results return to the retained Home tab.
 *
 * @author Dmitriy Moroz
 */
class UpscalerNavigationTest {
    @Test
    fun `given upscaler navigation, opened from another home tab, expected home route without an overlay`() {
        // Given
        val router = RootAppRouter(AppRoute.TextToImage)

        // When
        router.navigateToUpscaler()

        // Then
        assertEquals(AppRoute.Upscaler, router.route.value)
        assertTrue(router.route.value.isHomeRoute())
        assertEquals(AppRoute.Upscaler, router.route.value.asHomeTabRoute())
    }

    @Test
    fun `given upscaler configuration, wizard closes, expected return to upscaler home tab`() {
        // Given
        val router = RootAppRouter(AppRoute.Upscaler)

        // When
        router.navigateToUpscalerSetup()
        val setup = router.route.value
        router.navigateBack()

        // Then
        assertEquals(AppRoute.Setup(LaunchSource.SETTINGS, isUpscaler = true), setup)
        assertEquals(AppRoute.Upscaler, router.route.value)
    }

    @Test
    fun `given completed upscale, saved result closes, expected return to upscaler home tab`() {
        // Given
        val router = RootAppRouter(AppRoute.Upscaler)

        // When
        router.navigateToGalleryDetails(42)
        router.navigateBack()

        // Then
        assertEquals(AppRoute.Upscaler, router.route.value)
    }
}
