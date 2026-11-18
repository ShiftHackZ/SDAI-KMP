package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.runtime.Composable

/**
 * Leaves system-back interception to iOS; selection dismissal remains available through gallery controls.
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
) = Unit
