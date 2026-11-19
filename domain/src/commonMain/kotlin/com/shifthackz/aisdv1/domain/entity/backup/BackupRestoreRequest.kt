package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Immutable restore submission; retries retain the same session ID to avoid duplicate additive imports.
 * @param handle Validated staged archive identity.
 * @param selection Categories and providers explicitly selected by the user.
 * @param sessionId Stable operation identity retained across retries and process recovery.
 * @param confirmedFingerprint Opaque conflict revision previously confirmed, or null before consent.
 * @param shouldProceedToConfiguration Whether a completed first-launch import must durably hand off to configuration.
 * @author Dmitriy Moroz
 */
data class BackupRestoreRequest(
    val handle: String,
    val selection: BackupSelection,
    val sessionId: String,
    val confirmedFingerprint: String? = null,
    val shouldProceedToConfiguration: Boolean = false,
)
