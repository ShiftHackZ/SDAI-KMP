package com.shifthackz.aisdv1.feature.backup.codec

import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import okio.Path
import okio.Buffer
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BinaryBackupArchiveCodecTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private val codec =
        BinaryBackupArchiveCodec(Json { ignoreUnknownKeys = true; encodeDefaults = true })

    private suspend fun fixture(
        id: String = "settings",
        body: String = "portable fields"
    ): Pair<Path, BackupManifest> {
        val payload = temporary.newFile().absolutePath.toPath()
        FileSystem.SYSTEM.sink(payload).buffer().use { it.writeUtf8(body) }
        val section = codec.describe(payload, id, 1)
        val manifest = BackupManifest("archive", 100, "1.2.0", "ANDROID", listOf(section))
        val archive = temporary.newFile().absolutePath.toPath()
        codec.write(archive, manifest, listOf(payload))
        return archive to manifest
    }

    @Test
    fun `given binary backup, archive round trips, expected stable manifest and section bytes`() =
        runTest {
            // Given
            val (archive, expected) = fixture()
            // When
            val actual = codec.inspect(archive)
            val body = codec.openSection(archive, actual, "settings").use { it.readUtf8() }
            // Then
            assertEquals(expected, actual)
            assertEquals("portable fields", body)
        }

    @Test
    fun `given binary backup, payload byte is changed, expected checksum rejection`() = runTest {
        // Given
        val (archive, _) = fixture()
        val bytes = archive.toFile().readBytes()
            .also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        archive.toFile().writeBytes(bytes)
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { runBlocking { codec.inspect(archive) } }
        // Then
        assertEquals("Backup checksum mismatch", failure.message)
    }

    @Test
    fun `given binary backup, file is truncated, expected rejection before restoration`() =
        runTest {
            // Given
            val (archive, _) = fixture()
            archive.toFile().writeBytes(archive.toFile().readBytes().dropLast(1).toByteArray())
            // When
            val failure = assertThrows(IllegalArgumentException::class.java) {
                runBlocking {
                    codec.inspect(archive)
                }
            }
            // Then
            assertEquals("Invalid section length", failure.message)
        }

    @Test
    fun `given future backup section, framing is compatible, expected integrity validation without field knowledge`() =
        runTest {
            // Given
            val (archive, expected) = fixture("future_feature")
            // When
            val actual = codec.inspect(archive)
            // Then
            assertEquals(expected.sections, actual.sections)
        }

    @Test
    fun `given binary backup, duplicate section IDs exist, expected rejection`() = runTest {
        // Given
        val payload = temporary.newFile().absolutePath.toPath()
        payload.toFile().writeText("data")
        val section = codec.describe(payload, "gallery", 1)
        val manifest = BackupManifest("archive", 100, "1", "IOS", listOf(section, section))
        val archive = temporary.newFile().absolutePath.toPath()
        codec.write(archive, manifest, listOf(payload, payload))
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { runBlocking { codec.inspect(archive) } }
        // Then
        assertEquals("Duplicate backup sections", failure.message)
    }

    @Test
    fun `given binary backup, unsupported major framing is present, expected rejection`() =
        runTest {
            // Given
            val (archive, _) = fixture()
            archive.toFile().writeBytes(archive.toFile().readBytes().also { it[8] = 2 })
            // When
            val failure = assertThrows(IllegalArgumentException::class.java) {
                runBlocking {
                    codec.inspect(archive)
                }
            }
            // Then
            assertEquals("Unsupported backup container version", failure.message)
        }

    @Test
    fun `given binary backup, additive minor framing is present, expected compatible read`() =
        runTest {
            // Given
            val (archive, manifest) = fixture()
            archive.toFile().writeBytes(archive.toFile().readBytes().also { it[10] = 2 })
            // When
            val actual = codec.inspect(archive)
            // Then
            assertEquals(manifest, actual)
        }

    @Test
    fun `given binary backup, bytes follow declared sections, expected rejection`() = runTest {
        // Given
        val (archive, _) = fixture()
        archive.toFile().appendBytes(byteArrayOf(1))
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { runBlocking { codec.inspect(archive) } }
        // Then
        assertEquals("Unexpected trailing backup data", failure.message)
    }

    @Test
    fun `given binary section, decoder reaches its boundary, expected adjacent bytes remain inaccessible`() =
        runTest {
            // Given
            val source = Buffer().writeUtf8("firstSECOND")
            val section = BoundedBackupSource(source, 5).buffer()
            // When
            val text = section.readUtf8()
            // Then
            assertEquals("first", text)
            assertEquals("SECOND", source.readUtf8())
        }
}
