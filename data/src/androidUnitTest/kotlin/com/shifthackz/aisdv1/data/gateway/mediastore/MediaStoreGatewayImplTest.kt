package com.shifthackz.aisdv1.data.gateway.mediastore

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.shifthackz.aisdv1.data.export.MediaStoreImageFixture
import com.shifthackz.aisdv1.data.export.MediaStoreImageFormatResolverImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Exercises the production MediaStore writer with controlled Android provider responses.
 * Uses actual encoded image files and output streams; no bitmap or format resolver is mocked.
 *
 * @param fixture Image whose expected suffix, MIME and complete bytes must reach the provider.
 * @author Dmitriy Moroz
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class MediaStoreGatewayImplTest(private val fixture: MediaStoreImageFixture) {

    private val context = mockk<Context>()
    private val resolver = mockk<ContentResolver>()
    private val cursor = mockk<Cursor>(relaxed = true)
    private val volumeUri = Uri.parse("content://media/external/file")
    private val destinationUri = Uri.parse("content://media/external/file/42")
    private val insertedValues = slot<ContentValues>()
    private val capturedBytes = ByteArrayOutputStream()
    private var output: OutputStream? = capturedBytes
    private var hasExistingEntry = false
    private val gateway =
        MediaStoreGatewayImpl(context, mockk(), MediaStoreImageFormatResolverImpl())

    @Before
    fun setUp() {
        every { context.contentResolver } returns resolver
        every { resolver.query(volumeUri, any(), any(), any(), null) } returns cursor
        every { cursor.moveToFirst() } answers { hasExistingEntry }
        every { cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID) } returns 0
        every { cursor.getLong(0) } returns 42L
        every { resolver.insert(volumeUri, capture(insertedValues)) } returns destinationUri
        every { resolver.openOutputStream(destinationUri, "wt") } answers { output }
        every { resolver.delete(destinationUri, null, null) } returns 1
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `new export writes all metadata bytes with the detected suffix and MIME`() {
        gateway.exportToFile("image", fixture.bytes())

        assertArrayEquals(fixture.bytes(), capturedBytes.toByteArray())
        assertEquals(
            "image.${fixture.extension}",
            insertedValues.captured.getAsString(MediaStore.MediaColumns.DISPLAY_NAME)
        )
        assertEquals(
            fixture.mimeType,
            insertedValues.captured.getAsString(MediaStore.MediaColumns.MIME_TYPE)
        )
        assertEquals(
            "Download/SDAI/",
            insertedValues.captured.getAsString(MediaStore.MediaColumns.RELATIVE_PATH)
        )
        verify(exactly = 1) {
            resolver.query(
                volumeUri,
                match { it.contentEquals(arrayOf(MediaStore.MediaColumns._ID)) },
                "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND " +
                        "${MediaStore.MediaColumns.MIME_TYPE} = ? AND " +
                        "${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
                match {
                    it.contentEquals(
                        arrayOf(
                            "image.${fixture.extension}",
                            fixture.mimeType,
                            "Download/SDAI/"
                        )
                    )
                },
                null,
            )
            resolver.insert(volumeUri, any())
            resolver.openOutputStream(destinationUri, "wt")
            cursor.close()
        }
        verify(exactly = 0) { resolver.delete(any(), any(), any()) }
    }

    @Test
    fun `existing matching destination is reused without inserting a second entry`() {
        hasExistingEntry = true
        gateway.exportToFile("image", fixture.bytes())

        assertArrayEquals(fixture.bytes(), capturedBytes.toByteArray())
        verify(exactly = 0) { resolver.insert(any(), any()) }
        verify(exactly = 1) { cursor.close() }
    }

    @Test
    fun `unsupported image fails before accessing storage`() {
        assertThrows(IllegalArgumentException::class.java) {
            gateway.exportToFile(
                "image",
                "GIF89a".toByteArray()
            )
        }
        verify(exactly = 0) { context.contentResolver }
    }

    @Test
    fun `missing query cursor fails before inserting a destination`() {
        every { resolver.query(volumeUri, any(), any(), any(), null) } returns null
        assertThrows(IOException::class.java) { gateway.exportToFile("image", fixture.bytes()) }
        verify(exactly = 0) { resolver.insert(any(), any()) }
    }

    @Test
    fun `provider query error propagates without inserting a destination`() {
        val failure = SecurityException("query denied")
        every { resolver.query(volumeUri, any(), any(), any(), null) } throws failure
        assertSame(
            failure,
            assertThrows(SecurityException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        verify(exactly = 0) { resolver.insert(any(), any()) }
    }

    @Test
    fun `cursor is closed when reading the existing entry fails`() {
        hasExistingEntry = true
        val failure = IllegalStateException("missing id")
        every { cursor.getColumnIndexOrThrow(any()) } throws failure
        assertSame(
            failure,
            assertThrows(IllegalStateException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        verify(exactly = 1) { cursor.close() }
        verify(exactly = 0) { resolver.insert(any(), any()) }
    }

    @Test
    fun `null insert result fails without opening or deleting a destination`() {
        every { resolver.insert(volumeUri, any()) } returns null
        assertThrows(IOException::class.java) { gateway.exportToFile("image", fixture.bytes()) }
        verify(exactly = 0) { resolver.openOutputStream(any(), any()) }
        verify(exactly = 0) { resolver.delete(any(), any(), any()) }
    }

    @Test
    fun `null stream removes the newly created entry and reports failure`() {
        output = null
        assertThrows(IOException::class.java) { gateway.exportToFile("image", fixture.bytes()) }
        verify(exactly = 1) { resolver.delete(destinationUri, null, null) }
    }

    @Test
    fun `open error propagates and removes the newly created entry`() {
        val failure = IOException("open failed")
        every { resolver.openOutputStream(destinationUri, "wt") } throws failure
        assertSame(
            failure,
            assertThrows(IOException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        verify(exactly = 1) { resolver.delete(destinationUri, null, null) }
    }

    @Test
    fun `write error closes the stream and removes only the new entry`() {
        val failure = IOException("write failed")
        val stream = mockk<OutputStream>(relaxed = true)
        every { stream.write(any<ByteArray>()) } throws failure
        output = stream

        assertSame(
            failure,
            assertThrows(IOException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        verify(exactly = 1) { stream.close() }
        verify(exactly = 1) { resolver.delete(destinationUri, null, null) }
    }

    @Test
    fun `stream close error also removes the new entry`() {
        val failure = IOException("close failed")
        output = object : ByteArrayOutputStream() {
            override fun close() = throw failure
        }
        assertSame(
            failure,
            assertThrows(IOException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        verify(exactly = 1) { resolver.delete(destinationUri, null, null) }
    }

    @Test
    fun `existing entry is retained when opening its stream fails`() {
        hasExistingEntry = true
        output = null
        assertThrows(IOException::class.java) { gateway.exportToFile("image", fixture.bytes()) }
        verify(exactly = 0) { resolver.delete(any(), any(), any()) }
    }

    @Test
    fun `existing entry is retained when writing fails`() {
        hasExistingEntry = true
        val failure = IOException("write failed")
        val stream = mockk<OutputStream>(relaxed = true)
        every { stream.write(any<ByteArray>()) } throws failure
        output = stream
        assertSame(
            failure,
            assertThrows(IOException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        verify(exactly = 0) { resolver.delete(any(), any(), any()) }
    }

    @Test
    fun `cleanup error is suppressed on the original write failure`() {
        val failure = IOException("open failed")
        val cleanupFailure = SecurityException("delete denied")
        every { resolver.openOutputStream(destinationUri, "wt") } throws failure
        every { resolver.delete(destinationUri, null, null) } throws cleanupFailure
        assertSame(
            failure,
            assertThrows(IOException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        assertArrayEquals(arrayOf(cleanupFailure), failure.suppressed)
    }

    @Test
    fun `zero deleted rows preserves the original failure with cleanup details`() {
        val failure = IOException("open failed")
        every { resolver.openOutputStream(destinationUri, "wt") } throws failure
        every { resolver.delete(destinationUri, null, null) } returns 0
        assertSame(
            failure,
            assertThrows(IOException::class.java) {
                gateway.exportToFile(
                    "image",
                    fixture.bytes()
                )
            })
        assertEquals(1, failure.suppressed.size)
        assertTrue(failure.suppressed.single() is IOException)
    }

    companion object {
        /**
         * Repeats the export contract for PNG, JPEG and WebP fixtures.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameters(name = "{0}")
        fun formats(): Array<MediaStoreImageFixture> = MediaStoreImageFixture.entries.toTypedArray()
    }
}
