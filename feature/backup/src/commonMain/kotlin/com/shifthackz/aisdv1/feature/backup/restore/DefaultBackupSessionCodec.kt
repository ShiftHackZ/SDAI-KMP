package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Encodes journal selection IDs only; no credentials or application payloads enter the restore ledger.
 *
 * @param json Serializer for nonsecret selection lists.
 * @author Dmitriy Moroz
 */
internal class DefaultBackupSessionCodec(private val json: Json) : BackupSessionCodec {

    override fun encode(selection: BackupSelection): String = json.encodeToString(
        mapOf(
            "categories" to selection.categories.map { it.key }.sorted(),
            "providers" to selection.providers.sorted(),
        )
    )

    override fun decode(value: String): BackupSelection {
        val entries = json.decodeFromString<Map<String, List<String>>>(value)
        return BackupSelection(BackupCategory.entries.filter { it.key in entries["categories"].orEmpty() }
            .toSet(), entries["providers"].orEmpty().toSet())
    }
}
