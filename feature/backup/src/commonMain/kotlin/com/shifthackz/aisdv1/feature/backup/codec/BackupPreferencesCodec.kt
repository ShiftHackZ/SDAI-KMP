package com.shifthackz.aisdv1.feature.backup.codec

import com.shifthackz.aisdv1.domain.entity.backup.BackupValue

/**
 * Encodes portable scalar fields and preserves unsupported field identifiers for compatibility reporting.
 * @author Dmitriy Moroz
 */
internal interface BackupPreferencesCodec {
    /**
     * Serializes only explicitly present typed scalar fields.
     * @param values Stable field IDs and their portable scalar values.
     * @author Dmitriy Moroz
     */
    fun encode(values: Map<String, BackupValue>): String

    /**
     * Decodes scalar fields while retaining missing-entry semantics; unsupported value identifiers remain available for skipped-field accounting.
     * @param text One bounded metadata JSON object.
     * @author Dmitriy Moroz
     */
    fun decode(text: String): Map<String, BackupValue>

    /**
     * Encodes independent provider groups using stable provider and field IDs.
     * @param values Selected portable provider groups; may contain credentials and must not be logged.
     * @author Dmitriy Moroz
     */
    fun encodeProviders(values: Map<String, Map<String, BackupValue>>): String

    /**
     * Decodes independent provider objects without requiring any currently mandatory setup fields.
     * @param text Bounded provider-section metadata JSON.
     * @author Dmitriy Moroz
     */
    fun decodeProviders(text: String): Map<String, Map<String, BackupValue>>
}
