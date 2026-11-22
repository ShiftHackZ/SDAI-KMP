package com.shifthackz.aisdv1.presentation.backup.platform

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle

/**
 * Formats a UTC timestamp using the current platform locale and date/time conventions.
 *
 * @param epochMillis Archive creation time in milliseconds since the Unix epoch.
 * @author Dmitriy Moroz
 */
internal actual fun formatBackupCreatedAt(epochMillis: Long): String = NSDateFormatter().apply {
    dateStyle = NSDateFormatterMediumStyle
    timeStyle = NSDateFormatterShortStyle
}.stringFromDate(NSDate(timeIntervalSinceReferenceDate = epochMillis / 1000.0 - 978307200.0))
