package com.shifthackz.aisdv1.presentation.backup.platform

import java.text.DateFormat
import java.util.Date

/**
 * Formats a UTC timestamp using the current platform locale and date/time conventions.
 * @param epochMillis Archive creation time in milliseconds since the Unix epoch.
 * @author Dmitriy Moroz
 */
internal actual fun formatBackupCreatedAt(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
