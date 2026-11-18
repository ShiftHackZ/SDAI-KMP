package com.shifthackz.aisdv1.presentation.gallery.list

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
internal expect fun GalleryBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
