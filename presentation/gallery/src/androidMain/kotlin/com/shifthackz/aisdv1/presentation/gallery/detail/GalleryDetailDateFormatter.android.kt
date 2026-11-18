package com.shifthackz.aisdv1.presentation.gallery.detail

import java.util.Date

/**
 * Formats a stored Unix timestamp through the current platform date representation.
 *
 * @param epochMillis Creation time in milliseconds since the Unix epoch.
 * @return Platform-formatted date text for detail display.
 *
 * @author Dmitriy Moroz
 */
internal actual fun formatGalleryCreatedAt(epochMillis: Long): String =
    Date(epochMillis).toString()
