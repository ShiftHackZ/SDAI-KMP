package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Nonsecret lifecycle of one staged export, retained independently from a screen composition.
 * @author Dmitriy Moroz
 */
enum class BackupExportStatus { NONE, PROCESSING, READY, COPYING, COMPLETE, FAILED, CANCELED }
