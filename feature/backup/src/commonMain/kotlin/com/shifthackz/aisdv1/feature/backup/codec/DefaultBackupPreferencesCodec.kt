package com.shifthackz.aisdv1.feature.backup.codec

import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Serializes portable scalar fields without leaking preference storage keys or inventing absent values.
 *
 * @param json Forward-compatible JSON parser.
 * @author Dmitriy Moroz
 */
internal class DefaultBackupPreferencesCodec(private val json: Json) : BackupPreferencesCodec {

    override fun encode(values: Map<String, BackupValue>): String =
        JsonObject(values.mapValues { (_, value) ->
            when (value) {
                BackupValue.Unsupported -> error("Unsupported portable values cannot be exported")
                is BackupValue.Text -> JsonPrimitive(value.value)
                is BackupValue.Flag -> JsonPrimitive(value.isTrue)
                is BackupValue.Integer -> JsonPrimitive(value.value)
            }
        }).toString()

    override fun decode(text: String): Map<String, BackupValue> {
        val values =
            json.parseToJsonElement(text) as? JsonObject ?: error("Invalid backup preferences")
        return values.mapNotNull { (key, value) ->
            val scalar = value as? JsonPrimitive ?: return@mapNotNull key to BackupValue.Unsupported
            val decoded = when {
                scalar.isString -> BackupValue.Text(scalar.content)
                scalar.booleanOrNull != null -> BackupValue.Flag(scalar.booleanOrNull!!)
                scalar.intOrNull != null -> BackupValue.Integer(scalar.intOrNull!!)
                else -> BackupValue.Unsupported
            }
            key to decoded
        }.toMap()
    }

    override fun encodeProviders(values: Map<String, Map<String, BackupValue>>): String =
        JsonObject(values.mapValues { (_, fields) -> json.parseToJsonElement(encode(fields)) }).toString()

    override fun decodeProviders(text: String): Map<String, Map<String, BackupValue>> {
        val values =
            json.parseToJsonElement(text) as? JsonObject ?: error("Invalid backup providers")
        return values.mapNotNull { (provider, fields) ->
            if (fields !is JsonObject) return@mapNotNull provider to mapOf("" to BackupValue.Unsupported)
            provider to decode(fields.toString())
        }.toMap()
    }
}
