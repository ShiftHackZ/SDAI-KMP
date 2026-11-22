package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.NetworkUsageBucket
import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import com.shifthackz.aisdv1.feature.backup.archive.StagedBackup
import com.shifthackz.aisdv1.feature.backup.codec.BackupManifest
import com.shifthackz.aisdv1.feature.backup.codec.BinaryBackupArchiveCodec
import com.shifthackz.aisdv1.storage.db.persistent.dao.NetworkUsageDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.NetworkUsageEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NetworkBackupRestoreWriterTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private val json = Json { encodeDefaults = true }
    private val codec = BinaryBackupArchiveCodec(json)
    private val database = mockk<BackupStorage>()
    private val network = mockk<NetworkUsageDao>(relaxed = true)
    private val ledger = mockk<RestoreSessionDao>(relaxed = true)
    private val firstBucket = NetworkUsageBucket.entries[0].key
    private val secondBucket = NetworkUsageBucket.entries[1].key
    private val writer = RoomBackupRestoreWriter(
        database,
        codec,
        mockk(),
        mockk(),
        json,
        DefaultBackupImageFingerprint(),
        BackupImageBudget(1024, 100, 4096, 16),
        mockk(),
    )

    @Before
    fun setup() {
        every { database.networkUsageDao() } returns network
        every { database.restoreSessionDao() } returns ledger
        coEvery { database.writeTransaction<Unit>(any()) } coAnswers { firstArg<suspend () -> Unit>()() }
        coEvery { ledger.completed(any()) } returns emptyList()
        coEvery { network.query() } returns emptyList()
    }

    /**
     * Builds real framing around network counters without involving native document transport.
     *
     * @param values Incoming typed network buckets, including future bucket names in compatibility cases.
     * @param hasTrailingData Whether an extra byte follows the declared JSON record.
     * @author Dmitriy Moroz
     */
    private suspend fun archive(
        values: Map<String, Long>,
        hasTrailingData: Boolean = false
    ): StagedBackup {
        val payload = temporary.newFile().absolutePath.toPath()
        FileSystem.SYSTEM.sink(payload).buffer().use {
            codec.writeJson(it, json.encodeToString(values))
            if (hasTrailingData) it.writeByte(1)
        }
        val manifest = BackupManifest(
            "snapshot",
            1,
            "1",
            "ANDROID",
            listOf(codec.describe(payload, "network", values.size.toLong()))
        )
        val path = temporary.newFile().absolutePath.toPath()
        codec.write(path, manifest, listOf(payload))
        return StagedBackup("handle", path, manifest)
    }

    @Test
    fun `given Civitai counter in statistics backup, restore completes, expected additive persisted Civitai traffic`() =
        runTest {
            // Given
            val archive = archive(mapOf(NetworkUsageBucket.CIVITAI.key to 4096))
            // When
            writer.network("session", archive)
            // Then
            coVerify(exactly = 1) { network.addBytes("civitai", 4096) }
            coVerify { ledger.mark(RestoreSectionEntity("session", "network", 1, 0)) }
        }

    @Test
    fun `given existing traffic bucket, backup contains additional bytes, expected additive write with category ledger`() =
        runTest {
            // Given
            coEvery { network.query() } returns listOf(NetworkUsageEntity(firstBucket, 100))
            val archive = archive(mapOf(firstBucket to 25, secondBucket to 50))
            // When
            writer.network("session", archive)
            // Then
            coVerify(exactly = 1) {
                network.addBytes(
                    firstBucket,
                    25
                ); network.addBytes(secondBucket, 50)
            }
            coVerify { ledger.mark(RestoreSectionEntity("session", "network", 2, 0)) }
        }

    @Test
    fun `given future traffic bucket, restore runs, expected unknown bucket skipped and known bytes added`() =
        runTest {
            // Given
            val archive = archive(mapOf(firstBucket to 10, "future" to Long.MAX_VALUE))
            // When
            writer.network("session", archive)
            // Then
            coVerify {
                network.addBytes(
                    firstBucket,
                    10
                ); ledger.mark(RestoreSectionEntity("session", "network", 1, 1))
            }
            coVerify(exactly = 0) { network.addBytes("future", any()) }
        }

    @Test
    fun `given committed network category, same session retries, expected no repeated counter writes`() =
        runTest {
            // Given
            val archive = archive(mapOf(firstBucket to 10))
            coEvery { ledger.completed("session") } returns listOf("network")
            // When
            writer.network("session", archive)
            // Then
            coVerify(exactly = 0) {
                network.query(); network.addBytes(
                any(),
                any()
            ); ledger.mark(any())
            }
        }

    @Test
    fun `given maximum resulting counter, valid boundary restore runs, expected exact final increment allowed`() =
        runTest {
            // Given
            val archive = archive(mapOf(firstBucket to 1))
            coEvery { network.query() } returns listOf(
                NetworkUsageEntity(
                    firstBucket,
                    Long.MAX_VALUE - 1
                )
            )
            // When
            writer.network("session", archive)
            // Then
            coVerify { network.addBytes(firstBucket, 1); ledger.mark(any()) }
        }

    @Test
    fun `given individual counter overflow, restore runs, expected rejection before any counter or ledger write`() =
        runTest {
            // Given
            val archive = archive(mapOf(firstBucket to 1))
            coEvery { network.query() } returns listOf(
                NetworkUsageEntity(
                    firstBucket,
                    Long.MAX_VALUE
                )
            )
            // When
            val failure = runCatching { writer.network("session", archive) }.exceptionOrNull()
            // Then
            assertEquals("Network counter overflow", failure?.message)
            coVerify(exactly = 0) { network.addBytes(any(), any()); ledger.mark(any()) }
        }

    @Test
    fun `given aggregate overflow across valid buckets, restore runs, expected rejection before first write`() =
        runTest {
            // Given
            val archive = archive(mapOf(secondBucket to 1))
            coEvery { network.query() } returns listOf(
                NetworkUsageEntity(
                    firstBucket,
                    Long.MAX_VALUE
                )
            )
            // When
            val failure = runCatching { writer.network("session", archive) }.exceptionOrNull()
            // Then
            assertEquals("Network total overflow", failure?.message)
            coVerify(exactly = 0) { network.addBytes(any(), any()); ledger.mark(any()) }
        }

    @Test
    fun `given negative archived counter, restore runs, expected rejection before transaction mutations`() =
        runTest {
            // Given
            val archive = archive(mapOf(firstBucket to -1))
            // When
            val failure = runCatching { writer.network("session", archive) }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) {
                database.writeTransaction<Unit>(any()); network.addBytes(
                any(),
                any()
            ); ledger.mark(any())
            }
        }

    @Test
    fun `given invalid existing counter, restore runs, expected rejection preserving stored data`() =
        runTest {
            // Given
            val archive = archive(mapOf(firstBucket to 1))
            coEvery { network.query() } returns listOf(NetworkUsageEntity(firstBucket, -1))
            // When
            val failure = runCatching { writer.network("session", archive) }.exceptionOrNull()
            // Then
            assertEquals("Network counter overflow", failure?.message)
            coVerify(exactly = 0) { network.addBytes(any(), any()); ledger.mark(any()) }
        }

    @Test
    fun `given unexpected network payload suffix, restore runs, expected rejection without counter changes`() =
        runTest {
            // Given
            val archive = archive(mapOf(firstBucket to 1), hasTrailingData = true)
            // When
            val failure = runCatching { writer.network("session", archive) }.exceptionOrNull()
            // Then
            assertEquals("Invalid network counter", failure?.message)
            coVerify(exactly = 0) { network.addBytes(any(), any()); ledger.mark(any()) }
        }

    @Test
    fun `given network storage cancellation, restore reads current buckets, expected original cancellation and no completion marker`() =
        runTest {
            // Given
            val archive = archive(mapOf(firstBucket to 1))
            val cancellation = CancellationException("storage canceled")
            coEvery { network.query() } throws cancellation
            // When
            val failure = runCatching { writer.network("session", archive) }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            coVerify(exactly = 0) { network.addBytes(any(), any()); ledger.mark(any()) }
        }
}
