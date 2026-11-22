package com.shifthackz.aisdv1.data.image

/**
 * Supplies native metadata-only probing and sampled validation on each supported platform.
 *
 * @author Dmitriy Moroz
 */
internal expect class PlatformBackupImageDecoder() : BackupImageDecoder
