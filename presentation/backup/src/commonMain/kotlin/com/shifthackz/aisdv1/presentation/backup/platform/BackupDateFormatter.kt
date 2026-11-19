package com.shifthackz.aisdv1.presentation.backup.platform

/**
 * Formats a UTC timestamp using the current platform locale and date/time conventions.
 * @param epochMillis Archive creation time in milliseconds since the Unix epoch.
 * @author Dmitriy Moroz
 */
internal expect fun formatBackupCreatedAt(epochMillis: Long): String
