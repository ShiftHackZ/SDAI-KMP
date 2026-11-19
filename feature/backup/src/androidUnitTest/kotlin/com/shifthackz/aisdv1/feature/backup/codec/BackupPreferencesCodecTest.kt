package com.shifthackz.aisdv1.feature.backup.codec

import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupPreferencesCodecTest {
    @Test
    fun `given future preference shapes, structured and unsupported scalar fields decode, expected rejected identifiers retained for skipped counts`() {
        // Given
        val codec = DefaultBackupPreferencesCodec(Json)
        val text =
            """{"known":"value","object":{},"array":[],"fraction":1.5,"large":9223372036854775807,"null":null}"""
        // When
        val decoded = codec.decode(text)
        // Then
        assertEquals(6, decoded.size)
        assertEquals(BackupValue.Text("value"), decoded["known"])
        assertEquals(5, decoded.values.count { it == BackupValue.Unsupported })
    }

    @Test
    fun `given future provider group, provider has unknown structured contract, expected provider retained for skipped accounting`() {
        // Given
        val codec = DefaultBackupPreferencesCodec(Json)
        // When
        val decoded = codec.decodeProviders("""{"future":[],"known":{"setting":true}}""")
        // Then
        assertEquals(2, decoded.size)
        assertEquals(BackupValue.Unsupported, decoded.getValue("future").getValue(""))
        assertEquals(BackupValue.Flag(true), decoded.getValue("known").getValue("setting"))
    }
}
