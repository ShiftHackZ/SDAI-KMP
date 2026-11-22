package com.shifthackz.aisdv1.data.preference.backup

import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportIntent
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportStatus
import com.shifthackz.aisdv1.domain.gateway.backup.BackupExportStateStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Stores only export handles and result association in a separate checked preference suite.
 *
 * @param store Dedicated backup-operation preference owner; never the provider/settings suite.
 * @author Dmitriy Moroz
 */
internal class BackupExportStateStoreImpl(private val store: KeyValueStore) :
    BackupExportStateStore {
    override fun recoverPendingWrite() {
        store.retryPendingWrite()
    }

    override fun read(): BackupExportIntent? {
        val raw = store.getString(KEY)
        if (raw.isEmpty()) return null
        val fields = Json.parseToJsonElement(raw).jsonObject
        check(fields.getValue("version").jsonPrimitive.content == "1") { "Unsupported pending export state" }
        return validated(
            BackupExportIntent(
                fields.getValue("handle").jsonPrimitive.content,
                fields.getValue("filename").jsonPrimitive.content,
                BackupExportStatus.valueOf(fields.getValue("status").jsonPrimitive.content),
                fields["destination"]?.jsonPrimitive?.content,
            )
        )
    }

    override fun write(intent: BackupExportIntent) {
        validated(intent)
        store.putStringDurably(KEY, buildJsonObject {
            put("version", JsonPrimitive(1))
            put("handle", JsonPrimitive(intent.handle))
            put("filename", JsonPrimitive(intent.filename))
            put("status", JsonPrimitive(intent.status.name))
            intent.destination?.let { put("destination", JsonPrimitive(it)) }
        }.toString())
    }

    private fun validated(intent: BackupExportIntent): BackupExportIntent {
        check(intent.handle.isNotBlank() && intent.filename.isNotBlank()) { "Missing pending export identity" }
        check(intent.status != BackupExportStatus.NONE && intent.status != BackupExportStatus.PROCESSING) { "Invalid durable export stage" }
        check(intent.status != BackupExportStatus.COPYING || !intent.destination.isNullOrBlank()) { "Missing pending export destination" }
        return intent
    }

    override fun clear() = store.putStringDurably(KEY, "")

    private companion object {
        const val KEY = "pending_export"
    }
}
