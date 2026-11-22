package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import com.shifthackz.aisdv1.domain.entity.DownloadState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Exercises real file activation and cleanup while isolating HTTP transport at the downloader boundary.
 *
 * @author Dmitriy Moroz
 */
class AndroidModelDownloadLifecycleTest {
    @get:Rule
    val folder = TemporaryFolder()
    private val client = mockk<OkHttpClient>()
    private val call = mockk<Call>()
    private val url = "https://models/model.bin"

    @Before
    fun initialize() {
        mockkConstructor(OkHttpClient.Builder::class)
        every { anyConstructed<OkHttpClient.Builder>().build() } returns client
        every { client.newCall(any()) } returns call
    }

    @After
    fun releaseTransport() {
        unmockkConstructor(OkHttpClient.Builder::class)
    }

    @Test
    fun `given owned model download, transfer completes, expected pending marker removed before Complete`() =
        runTest {
            // Given
            every { call.execute() } answers {
                assertTrue(File(folder.root, "model/.download-in-progress").isFile)
                response(200)
            }
            val downloader = downloader()
            // When
            val states = downloader.download("model", url).toList()
            // Then
            assertTrue(states.last() is DownloadState.Complete)
            assertFalse(File(folder.root, "model/.download-in-progress").exists())
            assertEquals("model bytes", File(folder.root, "model/model.bin").readText())
        }

    @Test
    fun `given owned model download, server rejects archive, expected Error and partial directory removed`() =
        runTest {
            // Given
            every { call.execute() } returns response(503)
            val downloader = downloader()
            // When
            val states = downloader.download("model", url).toList()
            // Then
            assertTrue(states.last() is DownloadState.Error)
            assertFalse(File(folder.root, "model").exists())
        }

    @Test
    fun `given owned model download, transport is cancelled, expected cancellation and private partial cleanup only`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled")
            every { call.execute() } throws cancellation
            val source = folder.newFile("user-model.bin").apply { writeText("original") }
            val downloader = downloader()
            // When
            val actual =
                runCatching { downloader.download("model", url).toList() }.exceptionOrNull()
            // Then
            assertTrue(actual is CancellationException)
            assertEquals(cancellation.message, actual?.message)
            assertFalse(File(folder.root, "model").exists())
            assertEquals("original", source.readText())
        }

    /**
     * Builds the real downloader with a private temporary model root and isolated accounting.
     *
     * @author Dmitriy Moroz
     */
    private fun downloader() = AndroidDownloadableModelFileDownloader(
        mockk<FileProviderDescriptor> { every { localModelDirPath } returns folder.root.path },
        mockk(relaxed = true), mockk(relaxed = true),
    )

    /**
     * Creates a bounded unit transport response; no network server or app flow is substituted.
     *
     * @param code HTTP outcome supplied by the isolated transport boundary.
     * @author Dmitriy Moroz
     */
    private fun response(code: Int) = Response.Builder().request(Request.Builder().url(url).build())
        .protocol(Protocol.HTTP_1_1).code(code).message("unit response")
        .body("model bytes".toResponseBody()).build()
}
