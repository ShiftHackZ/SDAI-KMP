package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Installs platform back handling while visible gallery selection should be dismissed first.
 *
 * @param enabled Whether the visible gallery is currently in selection mode.
 * @param onBack Requests leaving selection mode without leaving the home tab.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun GalleryBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
