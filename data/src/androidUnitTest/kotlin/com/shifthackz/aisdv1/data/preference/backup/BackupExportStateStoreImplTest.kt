package com.shifthackz.aisdv1.data.preference.backup

import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportIntent
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupExportStateStoreImplTest {
    private var raw = ""
    private val store = mockk<KeyValueStore>(relaxed = true) {
        every { getString(any(), any()) } answers { raw }
        every { putStringDurably(any(), any()) } answers { raw = secondArg() }
    }
    private val records = BackupExportStateStoreImpl(store)

    @Test
    fun `given pending destination copy, checked operation record round trips, expected source and destination association preserved`() {
        // Given
        val intent = BackupExportIntent(
            "handle",
            "backup.sdaibackup",
            BackupExportStatus.COPYING,
            "content://destination"
        )
        // When
        records.write(intent)
        val restored = records.read()
        // Then
        assertEquals(intent, restored)
        verify(exactly = 1) { store.putStringDurably("pending_export", any()) }
        verify(exactly = 0) { store.putConfigurationDurably(any(), any()) }
    }

    @Test
    fun `given native export acknowledged, checked clear completes, expected no recoverable pending source`() {
        // Given
        records.write(BackupExportIntent("handle", "backup"))
        // When
        records.clear()
        // Then
        assertNull(records.read())
    }

    @Test
    fun `given quarantined operation record, startup recovers exact write, expected dedicated preference owner retried`() {
        // Given
        every { store.retryPendingWrite() } returns true
        // When
        records.recoverPendingWrite()
        // Then
        verify(exactly = 1) { store.retryPendingWrite() }
    }

    @Test
    fun `given invalid operation record, startup reads unknown schema, expected failure without silent pending export loss`() {
        // Given
        raw = """{"version":2}"""
        // When
        val error = assertThrows(IllegalStateException::class.java) { records.read() }
        // Then
        assertEquals("Unsupported pending export state", error.message)
    }

    @Test
    fun `given transient export stages, durable write is requested, expected rejection before persistence`() {
        // Given
        val stages = listOf(BackupExportStatus.NONE, BackupExportStatus.PROCESSING)
        // When
        val failures = stages.map {
            runCatching {
                records.write(
                    BackupExportIntent(
                        "handle",
                        "backup",
                        it
                    )
                )
            }.exceptionOrNull()
        }
        // Then
        assertTrue(failures.all { it?.message == "Invalid durable export stage" })
        verify(exactly = 0) { store.putStringDurably(any(), any()) }
    }

    @Test
    fun `given missing source identity, durable export write is requested, expected rejection without storage changes`() {
        // Given
        val intents = listOf(BackupExportIntent("", "backup"), BackupExportIntent("handle", " "))
        // When
        val failures = intents.map { runCatching { records.write(it) }.exceptionOrNull() }
        // Then
        assertTrue(failures.all { it?.message == "Missing pending export identity" })
        verify(exactly = 0) { store.putStringDurably(any(), any()) }
    }

    @Test
    fun `given copying state without destination, durable write is requested, expected explicit rejection`() {
        // Given
        val intent = BackupExportIntent("handle", "backup", BackupExportStatus.COPYING)
        // When
        val failure = runCatching { records.write(intent) }.exceptionOrNull()
        // Then
        assertEquals("Missing pending export destination", failure?.message)
        verify(exactly = 0) { store.putStringDurably(any(), any()) }
    }

    @Test
    fun `given corrupted durable JSON, startup reads operation, expected failure without silent record clearing`() {
        // Given
        raw = "not JSON"
        // When
        val failure = runCatching { records.read() }.exceptionOrNull()
        // Then
        assertTrue(failure != null)
        assertEquals("not JSON", raw)
        verify(exactly = 0) { store.putStringDurably(any(), any()) }
    }

    @Test
    fun `given retained pending export, checked clear fails, expected failure propagated and association retained`() {
        // Given
        val intent = BackupExportIntent("handle", "backup")
        records.write(intent)
        val failure = IllegalStateException("cannot clear")
        every { store.putStringDurably("pending_export", "") } throws failure
        // When
        val caught = runCatching { records.clear() }.exceptionOrNull()
        // Then
        assertTrue(failure === caught)
        assertEquals(intent, records.read())
    }
}
