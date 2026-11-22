package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Allowlisted portable fields keyed by stable provider and field identifiers. Never log credential values.
 *
 * @param providers Saved portable fields for each represented provider.
 * @param settings Saved user preferences, excluding lifecycle and device state.
 * @author Dmitriy Moroz
 */
data class BackupPreferences(
    val providers: Map<String, Map<String, BackupValue>>,
    val settings: Map<String, BackupValue>,
)
