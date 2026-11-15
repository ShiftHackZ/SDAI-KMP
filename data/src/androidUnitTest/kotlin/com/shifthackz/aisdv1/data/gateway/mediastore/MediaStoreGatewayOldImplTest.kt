package com.shifthackz.aisdv1.data.gateway.mediastore

import android.os.Environment
import com.shifthackz.aisdv1.data.export.MediaStoreImageFixture
import com.shifthackz.aisdv1.data.export.MediaStoreImageFormatResolverImpl
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Exercises the legacy writer against real temporary files, including overwrite and failure cleanup.
 * Only Android's public-storage root and the injected write failure are substituted.
 *
 * @param fixture Encoded image whose suffix and bytes must survive filesystem export.
 * @author Dmitriy Moroz
 */
@Suppress("DEPRECATION")
@RunWith(Parameterized::class)
class MediaStoreGatewayOldImplTest(private val fixture: MediaStoreImageFixture) {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val gateway = MediaStoreGatewayOldImpl(MediaStoreImageFormatResolverImpl())
    private val directory get() = File(temporaryFolder.root, "Download/SDAI")
    private val destination get() = File(directory, "image.${fixture.extension}")

    @Before
    fun setUp() {
        mockkStatic(Environment::class)
        every { Environment.getExternalStorageDirectory() } returns temporaryFolder.root
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `export creates the correct suffix and preserves all metadata bytes`() {
        gateway.exportToFile("image", fixture.bytes())
        assertArrayEquals(fixture.bytes(), destination.readBytes())
        assertEquals(listOf("image.${fixture.extension}"), directory.listFiles()!!.map { it.name })
    }

    @Test
    fun `repeated export overwrites rather than appending or duplicating`() {
        directory.mkdirs()
        destination.writeBytes(ByteArray(fixture.bytes().size + 100) { 1 })
        gateway.exportToFile("image", fixture.bytes())
        gateway.exportToFile("image", fixture.bytes())
        assertArrayEquals(fixture.bytes(), destination.readBytes())
        assertEquals(1, directory.listFiles()!!.size)
    }

    @Test
    fun `unsupported bytes fail before creating the export directory`() {
        assertThrows(IllegalArgumentException::class.java) {
            gateway.exportToFile(
                "image",
                byteArrayOf()
            )
        }
        assertFalse(directory.exists())
    }

    @Test
    fun `unavailable directory reports failure without writing another location`() {
        File(temporaryFolder.root, "Download").writeText("blocked")
        assertThrows(IOException::class.java) { gateway.exportToFile("image", fixture.bytes()) }
        assertFalse(directory.exists())
    }

    @Test
    fun `pre-existing destination is retained on write failure`() {
        destination.mkdirs()
        val child = File(destination, "keep.txt").apply { writeText("keep") }
        assertThrows(IOException::class.java) { gateway.exportToFile("image", fixture.bytes()) }
        assertTrue(destination.isDirectory)
        assertEquals("keep", child.readText())
    }

    @Test
    fun `failed write removes the new partial file and preserves the original failure`() {
        val failure = IOException("disk full")
        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")
        every { destination.writeBytes(any()) } answers {
            FileOutputStream(destination).use { it.write(fixture.bytes().copyOf(10)) }
            throw failure
        }
        assertSame(
            failure,
            assertThrows(IOException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        assertFalse(destination.exists())
    }

    @Test
    fun `failed overwrite retains the pre-existing file`() {
        directory.mkdirs()
        destination.writeText("existing")
        val failure = IOException("disk full")
        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")
        every { destination.writeBytes(any()) } throws failure
        assertSame(
            failure,
            assertThrows(IOException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        assertTrue(destination.exists())
        assertEquals("existing", destination.readText())
    }

    companion object {
        /**
         * Repeats the export contract for PNG, JPEG and WebP fixtures.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun formats(): Array<MediaStoreImageFixture> = MediaStoreImageFixture.entries.toTypedArray()
    }
}
