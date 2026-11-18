package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.mocks.mockLocalAiModel
import com.shifthackz.aisdv1.data.mocks.mockLocalAiModels
import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Verifies strict catalog persistence and faithful transport state propagation.
 *
 * @author Dmitriy Moroz
 */
class DownloadableModelRepositoryImplTest {

    private val stubException = Throwable("Something went wrong.")
    private val stubFilePath = "/storage/emulated/0/file.dat"
    private val stubRemoteDataSource = mockk<DownloadableModelDataSource.Remote>()
    private val stubLocalDataSource = mockk<DownloadableModelDataSource.Local>()

    private val repository = DownloadableModelRepositoryImpl(
        remoteDataSource = stubRemoteDataSource,
        localDataSource = stubLocalDataSource,
    )

    @Before
    fun initialize() {
        every {
            stubLocalDataSource.observeAllOnnx()
        } returns flowOf(mockLocalAiModels)

        every {
            stubLocalDataSource.observeAllSdxl()
        } returns flowOf(mockLocalAiModels)

        every {
            stubRemoteDataSource.download(any(), any())
        } returns flowOf(DownloadState.Unknown)
    }

    @Test
    fun `given model deletion, local storage completes, expected successful result`() =
        runTest {
            // Given
            coEvery {
                stubLocalDataSource.delete(any())
            } returns Unit

            // When
            val actual = runCatching { repository.delete("5598") }

            // Then
            Assert.assertTrue(actual.isSuccess)
        }

    @Test
    fun `given model deletion, local storage fails, expected original error`() =
        runTest {
            // Given
            coEvery {
                stubLocalDataSource.delete(any())
            } throws stubException

            // When
            val actual = runCatching { repository.delete("5598") }

            // Then
            Assert.assertSame(stubException, actual.exceptionOrNull())
        }

    @Test
    fun `given cached catalog read, local metadata exists, expected result without remote fetch`() =
        runTest {
            // Given
            coEvery { stubLocalDataSource.getAllOnnx() } returns mockLocalAiModels
            // When
            val actual = repository.readCachedCatalog(LocalAiModel.Type.ONNX)
            // Then
            Assert.assertEquals(mockLocalAiModels, actual)
            coVerify(exactly = 0) { stubRemoteDataSource.fetchCatalog(any()) }
        }

    @Test
    fun `given catalog fetch, network fails, expected original transfer failure`() = runTest {
        // Given
        coEvery { stubRemoteDataSource.fetchCatalog(LocalAiModel.Type.CoreMl) } throws stubException
        // When
        val actual =
            runCatching { repository.fetchCatalog(LocalAiModel.Type.CoreMl) }.exceptionOrNull()
        // Then
        Assert.assertSame(stubException, actual)
        coVerify(exactly = 0) { stubLocalDataSource.save(any()) }
    }

    @Test
    fun `given catalog fetch, local save fails, expected original storage failure`() = runTest {
        // Given
        coEvery { stubRemoteDataSource.fetchCatalog(LocalAiModel.Type.CoreMl) } returns mockLocalAiModels
        coEvery { stubLocalDataSource.save(mockLocalAiModels) } throws stubException
        // When
        val actual =
            runCatching { repository.fetchCatalog(LocalAiModel.Type.CoreMl) }.exceptionOrNull()
        // Then
        Assert.assertSame(stubException, actual)
    }

    @Test
    fun `given ONNX catalog observation, empty cache becomes populated, expected both snapshots in order`() =
        runTest {
            // Given
            every {
                stubLocalDataSource.observeAllOnnx()
            } returns flowOf(emptyList(), mockLocalAiModels)

            // When
            val actual = repository.observeAllOnnx().toList()

            // Then
            Assert.assertEquals(listOf(emptyList(), mockLocalAiModels), actual)
        }

    @Test
    fun `given ONNX catalog observation, cached models change, expected both model snapshots`() =
        runTest {
            // Given
            every {
                stubLocalDataSource.observeAllOnnx()
            } returns flowOf(mockLocalAiModels, mockLocalAiModels.map { it.copy(id = "1") })

            // When
            val actual = repository.observeAllOnnx().toList()

            // Then
            Assert.assertEquals(
                listOf(mockLocalAiModels, listOf(mockLocalAiModel.copy(id = "1"))),
                actual
            )
        }

    @Test
    fun `given ONNX catalog observation, storage flow fails, expected original error`() =
        runTest {
            // Given
            every {
                stubLocalDataSource.observeAllOnnx()
            } returns flow { throw stubException }

            // When
            val actual = runCatching { repository.observeAllOnnx().toList() }

            // Then
            Assert.assertSame(stubException, actual.exceptionOrNull())
        }

    @Test
    fun `given SDXL catalog observation, storage emits models, expected unchanged model list`() =
        runTest {
            // Given
            every {
                stubLocalDataSource.observeAllSdxl()
            } returns flowOf(mockLocalAiModels)

            // When
            val actual = repository.observeAllSdxl().toList()

            // Then
            Assert.assertEquals(listOf(mockLocalAiModels), actual)
        }

    @Test
    fun `given model download, transfer completes, expected ordered progress and completion`() =
        runTest {
            // Given
            every {
                stubRemoteDataSource.download(any(), any())
            } returns flowOf(
                DownloadState.Unknown,
                DownloadState.Downloading(44),
                DownloadState.Downloading(100),
                DownloadState.Complete(stubFilePath),
            )

            // When
            val actual = repository.download("5598", "https://moroz.cc/stub.zip").toList()

            // Then
            Assert.assertEquals(
                listOf(
                    DownloadState.Unknown,
                    DownloadState.Downloading(44),
                    DownloadState.Downloading(100),
                    DownloadState.Complete(stubFilePath),
                ),
                actual,
            )
        }

    @Test
    fun `given model download, transfer reports error, expected ordered progress and original error state`() =
        runTest {
            // Given
            every {
                stubRemoteDataSource.download(any(), any())
            } returns flowOf(
                DownloadState.Unknown,
                DownloadState.Downloading(44),
                DownloadState.Error(stubException),
            )

            // When
            val actual = repository.download("5598", "https://moroz.cc/stub.zip").toList()

            // Then
            Assert.assertEquals(
                listOf(
                    DownloadState.Unknown,
                    DownloadState.Downloading(44),
                    DownloadState.Error(stubException),
                ),
                actual,
            )
        }

    @Test
    fun `given model download, remote flow fails, expected original exception`() =
        runTest {
            // Given
            every {
                stubRemoteDataSource.download(any(), any())
            } returns flow { throw stubException }

            // When
            val actual = runCatching {
                repository.download("5598", "https://moroz.cc/stub.zip").toList()
            }

            // Then
            Assert.assertSame(stubException, actual.exceptionOrNull())
        }
}
