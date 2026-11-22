package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Applies selection, sequencing and failure contracts to every supported local model family.
 *
 * @param type Provider whose cached catalog and download ownership must be retained.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class DownloadModelsUseCaseImplTest(private val type: LocalAiModel.Type) {
    private val catalog = mockk<GetCachedLocalModelCatalogUseCase>()
    private val download = mockk<DownloadModelUseCase>()
    private val useCase = DownloadModelsUseCaseImpl(catalog, download)
    private val first = LocalAiModel(
        "first",
        type,
        "First",
        "1 GB",
        listOf("https://models/first", "https://mirror/first")
    )
    private val second =
        first.copy(id = "second", name = "Second", sources = listOf("https://models/second"))

    @Test
    fun `given provider batch, first archive remains active, expected next archive starts only after activation`() =
        runTest {
            // Given
            val release = CompletableDeferred<Unit>()
            val started = mutableListOf<String>()
            val snapshots = mutableListOf<ModelDownloadBatch>()
            coEvery { catalog(type) } returns listOf(first, second)
            every { download(any(), any()) } answers {
                val id = firstArg<String>()
                flow {
                    started += id
                    if (id == first.id) release.await()
                    emit(DownloadState.Complete("/owned/$id"))
                }
            }
            // When
            val job = launch { useCase(ModelDownloadRequest(type)).toList(snapshots) }
            runCurrent()
            val whileBlocked = started.toList()
            release.complete(Unit)
            job.join()
            // Then
            assertEquals(listOf(first.id), whileBlocked)
            assertEquals(listOf(first.id, second.id), started)
            assertEquals(listOf(first.id, second.id), snapshots.last().completedIds)
            assertEquals(100, snapshots.last().totalProgress)
            assertFalse(snapshots.last().isRunning)
            assertEquals(type, snapshots.last().type)
            verify(exactly = 1) { download(first.id, first.sources.first()) }
        }

    @Test
    fun `given provider batch, installed and sourceless entries exist, expected only missing downloadable models counted`() =
        runTest {
            // Given
            coEvery { catalog(type) } returns listOf(
                first.copy(downloaded = true),
                second,
                first.copy(id = "custom", sources = emptyList())
            )
            every {
                download(
                    second.id,
                    second.sources.first()
                )
            } returns flowOf(DownloadState.Complete("/owned/second"))
            // When
            val snapshots = useCase(ModelDownloadRequest(type)).toList()
            // Then
            assertTrue(snapshots.all { it.totalModelCount == 1 && it.isBatch })
            assertEquals(listOf(second.id), snapshots.last().completedIds)
            verify(exactly = 1) { download(any(), any()) }
        }

    @Test
    fun `given single model request, alternative mirror is confirmed, expected only selected archive uses that mirror`() =
        runTest {
            // Given
            coEvery { catalog(type) } returns listOf(first, second)
            every {
                download(
                    first.id,
                    first.sources.last()
                )
            } returns flowOf(DownloadState.Complete("/owned/first"))
            // When
            val snapshots =
                useCase(ModelDownloadRequest(type, first.id, first.sources.last())).toList()
            // Then
            assertTrue(snapshots.all { !it.isBatch && it.totalModelCount == 1 })
            assertEquals(listOf(first.id), snapshots.last().completedIds)
            verify(exactly = 0) { download(second.id, any()) }
        }

    @Test
    fun `given single model request, activation already exists, expected no transfer and completed empty selection`() =
        runTest {
            // Given
            coEvery { catalog(type) } returns listOf(first.copy(downloaded = true))
            // When
            val result =
                useCase(ModelDownloadRequest(type, first.id, first.sources.first())).toList()
                    .single()
            // Then
            assertFalse(result.isRunning)
            assertEquals(100, result.totalProgress)
            assertEquals(0, result.totalModelCount)
            verify(exactly = 0) { download(any(), any()) }
        }

    @Test
    fun `given single model request, identity is absent from catalog, expected rejection before any archive starts`() =
        runTest {
            // Given
            coEvery { catalog(type) } returns listOf(second)
            // When
            val failure = runCatching {
                useCase(
                    ModelDownloadRequest(
                        type,
                        first.id,
                        first.sources.first()
                    )
                ).collect()
            }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            verify(exactly = 0) { download(any(), any()) }
        }

    @Test
    fun `given provider batch, cache read fails, expected same failure and no download`() =
        runTest {
            // Given
            val failure = IllegalStateException("cache unavailable")
            coEvery { catalog(type) } throws failure
            // When
            val actual =
                runCatching { useCase(ModelDownloadRequest(type)).collect() }.exceptionOrNull()
            // Then
            assertSame(failure, actual)
            coVerify(exactly = 1) { catalog(type) }
            verify(exactly = 0) { download(any(), any()) }
        }

    @Test
    fun `given provider batch, archive emits error state, expected original error and remaining models untouched`() =
        runTest {
            // Given
            val failure = IllegalStateException("network unavailable")
            coEvery { catalog(type) } returns listOf(first, second)
            every { download(first.id, any()) } returns flowOf(
                DownloadState.Downloading(20),
                DownloadState.Error(failure)
            )
            // When
            val actual =
                runCatching { useCase(ModelDownloadRequest(type)).collect() }.exceptionOrNull()
            // Then
            assertSame(failure, actual)
            verify(exactly = 0) { download(second.id, any()) }
        }

    @Test
    fun `given provider batch, transfer throws cancellation, expected same cancellation without next model or terminal success`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled by owner")
            val snapshots = mutableListOf<ModelDownloadBatch>()
            coEvery { catalog(type) } returns listOf(first, second)
            every { download(first.id, any()) } returns flow { throw cancellation }
            // When
            val actual =
                runCatching { useCase(ModelDownloadRequest(type)).toList(snapshots) }.exceptionOrNull()
            // Then
            assertSame(cancellation, actual)
            assertTrue(snapshots.none { !it.isRunning })
            verify(exactly = 0) { download(second.id, any()) }
        }

    @Test
    fun `given provider batch, transfer ends without activation, expected failure without advancing queue`() =
        runTest {
            // Given
            coEvery { catalog(type) } returns listOf(first, second)
            every { download(first.id, any()) } returns emptyFlow()
            // When
            val failure =
                runCatching { useCase(ModelDownloadRequest(type)).collect() }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalStateException)
            verify(exactly = 0) { download(second.id, any()) }
        }

    @Test
    fun `given provider batch, progress repeats before completion, expected distinct updates and one completed identity`() =
        runTest {
            // Given
            coEvery { catalog(type) } returns listOf(first)
            every { download(first.id, any()) } returns flowOf(
                DownloadState.Downloading(25),
                DownloadState.Downloading(25),
                DownloadState.Complete("/owned/first"),
                DownloadState.Complete("/owned/first")
            )
            // When
            val snapshots = useCase(ModelDownloadRequest(type)).toList()
            // Then
            assertEquals(1, snapshots.count { it.progress == DownloadState.Downloading(25) })
            assertEquals(listOf(first.id), snapshots.last().completedIds)
            assertEquals(first.name, snapshots.last().modelName)
        }

    @Test
    fun `given provider batch, catalog is empty, expected terminal success without transfer`() =
        runTest {
            // Given
            coEvery { catalog(type) } returns emptyList()
            // When
            val result = useCase(ModelDownloadRequest(type)).toList().single()
            // Then
            assertFalse(result.isRunning)
            assertEquals(100, result.totalProgress)
            verify(exactly = 0) { download(any(), any()) }
        }

    @Test
    fun `given single model request, selected catalog entry has no archive sources, expected rejection before transfer`() =
        runTest {
            // Given
            coEvery { catalog(type) } returns listOf(first.copy(sources = emptyList()))
            // When
            val failure = runCatching {
                useCase(
                    ModelDownloadRequest(
                        type,
                        first.id,
                        "https://models/first"
                    )
                ).collect()
            }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            verify(exactly = 0) { download(any(), any()) }
        }

    @Test
    fun `given interrupted provider batch, completed model is installed before retry, expected retry skips it and downloads remaining model`() =
        runTest {
            // Given
            coEvery { catalog(type) } returnsMany listOf(
                listOf(first, second),
                listOf(first.copy(downloaded = true), second)
            )
            every {
                download(
                    first.id,
                    any()
                )
            } returns flowOf(DownloadState.Complete("/owned/first"))
            every {
                download(
                    second.id,
                    any()
                )
            } returnsMany listOf(
                flow { throw CancellationException("cancelled") },
                flowOf(DownloadState.Complete("/owned/second"))
            )
            // When
            val interruption =
                runCatching { useCase(ModelDownloadRequest(type)).collect() }.exceptionOrNull()
            val resumed = useCase(ModelDownloadRequest(type)).toList()
            // Then
            assertTrue(interruption is CancellationException)
            assertEquals(1, resumed.last().totalModelCount)
            assertEquals(listOf(second.id), resumed.last().completedIds)
            verify(exactly = 1) { download(first.id, any()) }
            verify(exactly = 2) { download(second.id, any()) }
        }

    companion object {
        /**
         * Supplies each provider family to the same shared download contracts.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun providers() = LocalAiModel.Type.entries.map { arrayOf(it) }
    }
}
