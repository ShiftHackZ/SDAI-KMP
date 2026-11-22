package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection

/**
 * Typed BackupSessionCodec operations used by backup orchestration and implementations.
 *
 * @author Dmitriy Moroz
 */
internal interface BackupSessionCodec {
    /**
     * Produces deterministic nonsecret selection JSON for binding a retry session to its original choices.
     *
     * @param selection Selected category/provider IDs only.
     * @author Dmitriy Moroz
     */
    fun encode(selection: BackupSelection): String

    /**
     * Restores known category/provider identifiers from the journal; unknown categories are ignored.
     *
     * @param value Persisted nonsecret selection JSON.
     * @author Dmitriy Moroz
     */
    fun decode(value: String): BackupSelection
}
