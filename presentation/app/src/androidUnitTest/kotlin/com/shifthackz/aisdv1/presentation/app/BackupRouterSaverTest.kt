package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.saveable.SaverScope
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupRouterSaverTest {
    private val scope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    private fun saved(router: RootAppRouter): String =
        with(backupRouterSaver) { scope.save(router)!! }

    @Test
    fun `given settings native backup picker, host route is saved and restored, expected backup callback destination and settings back route retained`() {
        // Given
        val router = RootAppRouter(AppRoute.Settings).also { it.navigateToBackup() }
        // When
        val restored = backupRouterSaver.restore(saved(router))!!
        // Then
        assertEquals(AppRoute.Backup(LaunchSource.SETTINGS), restored.route.value)
        restored.navigateBack()
        assertEquals(AppRoute.Settings, restored.route.value)
    }

    @Test
    fun `given initial restore route, host route is saved and restored, expected first launch mode retained`() {
        // Given
        val router = RootAppRouter(AppRoute.Backup(LaunchSource.SPLASH))
        // When
        val restored = backupRouterSaver.restore(saved(router))!!
        // Then
        assertEquals(AppRoute.Backup(LaunchSource.SPLASH), restored.route.value)
    }

    @Test
    fun `given route with unrelated transient arguments, host route is saved, expected existing splash startup validation`() {
        // Given
        val router = RootAppRouter(AppRoute.TextToImage)
        // When
        val restored = backupRouterSaver.restore(saved(router))!!
        // Then
        assertEquals(AppRoute.Splash, restored.route.value)
    }
}
