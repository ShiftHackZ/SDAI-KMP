package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.entity.AuthenticatedConnection
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111GenerationApi
import com.shifthackz.aisdv1.network.auth.BasicHttpAuthorization
import com.shifthackz.aisdv1.network.response.Automatic1111ProgressResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Tests strict request correlation and preserves transport failures for domain recovery policy.
 *
 * @author Dmitriy Moroz
 */
class Automatic1111ProgressRepositoryImplTest {
    private val api = mockk<Automatic1111GenerationApi>()
    private val connections = mockk<AuthenticatedConnectionProvider>()
    private val connection = AuthenticatedConnection(
        "https://server.example/proxy", "", "", AuthorizationCredentials.None, 1
    )
    private val repository = Automatic1111ProgressRepositoryImpl(api, connections)

    @Test
    fun `given AUTOMATIC1111 progress, matching job is returned, expected bounded correlated fraction`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(0.45f, "mine")
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(0.45f, actual)
        }

    @Test
    fun `given AUTOMATIC1111 progress, different job is returned, expected no measurement`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(0.45f, "other")
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(null, actual)
        }

    @Test
    fun `given AUTOMATIC1111 progress, legacy server without correlation is returned, expected no measurement`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(0.45f)
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(null, actual)
        }

    @Test
    fun `given AUTOMATIC1111 progress, missing measurement is returned, expected no measurement`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(null, "mine")
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(null, actual)
        }

    @Test
    fun `given AUTOMATIC1111 progress, negative progress is returned, expected bounded correlated fraction`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(-0.1f, "mine")
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(0f, actual)
        }

    @Test
    fun `given AUTOMATIC1111 progress, excess progress is returned, expected bounded correlated fraction`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(2f, "mine")
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(1f, actual)
        }

    @Test
    fun `given AUTOMATIC1111 progress, NaN progress is returned, expected no measurement`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(Float.NaN, "mine")
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(null, actual)
        }

    @Test
    fun `given AUTOMATIC1111 progress, infinite progress is returned, expected no measurement`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(Float.POSITIVE_INFINITY, "mine")
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(null, actual)
        }

    @Test
    fun `given AUTOMATIC1111 progress, negative infinite progress is returned, expected no measurement`() =
        runTest {
            // Given
            coEvery { connections.snapshot() } returns connection
            coEvery {
                api.progress(
                    connection.automatic1111Url,
                    null
                )
            } returns Automatic1111ProgressResponse(Float.NEGATIVE_INFINITY, "mine")
            // When
            val actual = repository.getProgress("mine")
            // Then
            assertEquals(null, actual)
        }

    @Test
    fun `given authenticated progress, connection is captured, expected configured endpoint and Basic credentials forwarded`() =
        runTest {
            // Given
            val authenticated = connection.copy(
                credentials = AuthorizationCredentials.HttpBasic(
                    "test",
                    "password"
                )
            )
            coEvery { connections.snapshot() } returns authenticated
            coEvery { api.progress(any(), any()) } returns Automatic1111ProgressResponse(
                0.5f,
                "mine"
            )
            // When
            repository.getProgress("mine")
            // Then
            coVerify(exactly = 1) {
                api.progress(
                    connection.automatic1111Url,
                    BasicHttpAuthorization("test", "password")
                )
            }
        }

    @Test
    fun `given strict progress repository, transport fails, expected original failure rather than cached progress`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { connections.snapshot() } returns connection
            coEvery { api.progress(any(), any()) } throws failure
            // When
            val result = runCatching { repository.getProgress("mine") }
            // Then
            assertSame(failure, result.exceptionOrNull())
        }

    @Test
    fun `given strict progress repository, request is cancelled, expected cancellation propagates`() =
        runTest {
            // Given
            val failure = CancellationException("cancelled")
            coEvery { connections.snapshot() } returns connection
            coEvery { api.progress(any(), any()) } throws failure
            // When
            val result = runCatching { repository.getProgress("mine") }
            // Then
            assertSame(failure, result.exceptionOrNull())
        }

    @Test
    fun `given strict progress repository, connection capture fails, expected no provider request`() =
        runTest {
            // Given
            val failure = IllegalArgumentException("stale connection")
            coEvery { connections.snapshot() } throws failure
            // When
            val result = runCatching { repository.getProgress("mine") }
            // Then
            assertSame(failure, result.exceptionOrNull())
            coVerify(exactly = 0) { api.progress(any(), any()) }
        }
}
