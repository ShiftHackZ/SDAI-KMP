package com.shifthackz.aisdv1.presentation.settings.platform

import androidx.compose.runtime.Composable

/**
 * Remembers the native permission, storage and external-navigation adapter used by Settings.
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun rememberSettingsPlatformActions(): SettingsPlatformActions
