package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.saveable.Saver
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.RootAppRouter

/**
 * Retains only nonsecret backup launch context so native document results reconnect after host recreation.
 * Other routes retain the existing cold-start behavior and are rebuilt through startup validation.
 *
 * @author Dmitriy Moroz
 */
internal val backupRouterSaver = Saver<RootAppRouter, String>(
    save = { router ->
        when (val route = router.route.value) {
            is AppRoute.Backup -> if (route.source == LaunchSource.SPLASH) "initial" else "settings"
            AppRoute.BackupWelcome -> "welcome"
            else -> "splash"
        }
    },
    restore = { context ->
        when (context) {
            "initial" -> RootAppRouter(AppRoute.Backup(LaunchSource.SPLASH))
            "settings" -> RootAppRouter(AppRoute.Settings).also { it.navigateToBackup() }
            "welcome" -> RootAppRouter(AppRoute.BackupWelcome)
            else -> RootAppRouter()
        }
    }
)
