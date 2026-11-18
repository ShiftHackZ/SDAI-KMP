package com.shifthackz.aisdv1.presentation.gallery.detail

import platform.Foundation.NSDate

/**
 * Formats a stored Unix timestamp through the current platform date representation.
 *
 * @param epochMillis Creation time in milliseconds since the Unix epoch.
 * @return Platform-formatted date text for detail display.
 *
 * @author Dmitriy Moroz
 */
internal actual fun formatGalleryCreatedAt(epochMillis: Long): String =
    NSDate(
        timeIntervalSinceReferenceDate = epochMillis.toDouble() / 1000.0 - APPLE_REFERENCE_EPOCH_SECONDS,
    ).toString()

private const val APPLE_REFERENCE_EPOCH_SECONDS = 978307200.0
