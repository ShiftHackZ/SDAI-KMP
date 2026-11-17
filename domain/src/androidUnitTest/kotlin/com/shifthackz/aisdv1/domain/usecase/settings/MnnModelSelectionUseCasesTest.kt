package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.MnnModelSelectionLease
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks selection commits while the validated immutable snapshot remains leased.
 *
 * @author Dmitriy Moroz
 */
class MnnModelSelectionUseCasesTest {
    private val repository = mockk<MnnGenerationRepository>()
    private val getConfiguration = mockk<GetConfigurationUseCase>()
    private val setConfiguration = mockk<SetServerConfigurationUseCase>()
    private val lease = mockk<MnnModelSelectionLease>(relaxed = true)
    private val initial = Configuration(
        source = ServerSource.LOCAL_MNN,
        localMnnCustomActivationId = "a".repeat(64),
        localMnnModelPath = "/Downloads/model",
        localMnnBackend = MnnBackend.OPENCL,
        serverUrl = "https://retained.example"
    )
    private val activation = "b".repeat(64)

    @Test
    fun `given connect MNN, custom validation succeeds, expected leased identity and backend committed`() =
        runTest {
            // Given
            every { lease.activationId } returns activation
            coEvery {
                repository.validateModel(
                    LocalAiModel.CustomMnn.id,
                    "/Downloads/new"
                )
            } returns lease
            coEvery { getConfiguration() } returns initial
            coEvery { setConfiguration(any()) } coAnswers {
                verify(exactly = 0) { lease.close() }
                Unit
            }
            val useCase = ConnectToMnnUseCaseImpl(repository, getConfiguration, setConfiguration)

            // When
            val result = useCase(LocalAiModel.CustomMnn.id, "/Downloads/new", MnnBackend.CPU)

            // Then
            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                setConfiguration(
                    initial.copy(
                        localMnnModelId = LocalAiModel.CustomMnn.id,
                        localMnnActivationId = activation,
                        localMnnCustomActivationId = activation,
                        localMnnModelPath = "/Downloads/new",
                        localMnnBackend = MnnBackend.CPU
                    )
                )
            }
            verify(exactly = 1) { lease.close() }
        }

    @Test
    fun `given connect MNN, catalog validation succeeds, expected previous custom snapshot retained`() =
        runTest {
            // Given
            every { lease.activationId } returns activation
            coEvery { repository.validateModel("mnn_sd15", "") } returns lease
            coEvery { getConfiguration() } returns initial
            coEvery { setConfiguration(any()) } returns Unit

            // When
            val result = ConnectToMnnUseCaseImpl(repository, getConfiguration, setConfiguration)(
                "mnn_sd15",
                "",
                MnnBackend.OPENCL
            )

            // Then
            assertTrue(result.isSuccess)
            coVerify { setConfiguration(match { it.localMnnCustomActivationId == initial.localMnnCustomActivationId && it.localMnnActivationId == activation }) }
            verify(exactly = 1) { lease.close() }
        }

    @Test
    fun `given connect MNN, validation fails, expected configuration untouched`() = runTest {
        // Given
        val failure = IllegalArgumentException("corrupt")
        coEvery { repository.validateModel(any(), any()) } throws failure

        // When
        val result = ConnectToMnnUseCaseImpl(repository, getConfiguration, setConfiguration)(
            "mnn_sd15",
            "",
            MnnBackend.CPU
        )

        // Then
        assertSame(failure, result.exceptionOrNull())
        coVerify(exactly = 0) { getConfiguration(); setConfiguration(any()) }
    }

    @Test
    fun `given connect MNN, persistence fails, expected original failure and closed lease`() =
        runTest {
            // Given
            val failure = IllegalStateException("disk")
            every { lease.activationId } returns activation
            coEvery { repository.validateModel(any(), any()) } returns lease
            coEvery { getConfiguration() } returns initial
            coEvery { setConfiguration(any()) } throws failure

            // When
            val result = ConnectToMnnUseCaseImpl(repository, getConfiguration, setConfiguration)(
                "mnn_sd15",
                "",
                MnnBackend.CPU
            )

            // Then
            assertSame(failure, result.exceptionOrNull())
            verify(exactly = 1) { lease.close() }
        }

    @Test
    fun `given connect MNN, commit is cancelled, expected cancellation and closed lease`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled")
            every { lease.activationId } returns activation
            coEvery { repository.validateModel(any(), any()) } returns lease
            coEvery { getConfiguration() } returns initial
            coEvery { setConfiguration(any()) } throws cancellation

            // When
            val failure = runCatching {
                ConnectToMnnUseCaseImpl(
                    repository,
                    getConfiguration,
                    setConfiguration
                )("mnn_sd15", "", MnnBackend.CPU)
            }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
            verify(exactly = 1) { lease.close() }
        }

    @Test
    fun `given connect MNN, commit cancellation and cleanup both fail, expected cancellation with suppressed cleanup`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled")
            val cleanup = IllegalStateException("close")
            every { lease.activationId } returns activation
            every { lease.close() } throws cleanup
            coEvery { repository.validateModel(any(), any()) } returns lease
            coEvery { getConfiguration() } returns initial
            coEvery { setConfiguration(any()) } throws cancellation

            // When
            val failure = runCatching {
                ConnectToMnnUseCaseImpl(
                    repository,
                    getConfiguration,
                    setConfiguration
                )("mnn_sd15", "", MnnBackend.CPU)
            }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
            assertEquals(listOf(cleanup), cancellation.suppressed.toList())
        }

    @Test
    fun `given select MNN, custom source is gone, expected owned snapshot reused without import`() =
        runTest {
            // Given
            every { lease.activationId } returns activation
            coEvery { getConfiguration() } returns initial
            coEvery {
                repository.validateOwnedModel(
                    LocalAiModel.CustomMnn.id,
                    initial.localMnnCustomActivationId
                )
            } returns lease
            coEvery { setConfiguration(any()) } returns Unit

            // When
            val result = SelectMnnModelUseCaseImpl(repository, getConfiguration, setConfiguration)(
                LocalAiModel.CustomMnn.id
            )

            // Then
            assertTrue(result.isSuccess)
            coVerify {
                setConfiguration(
                    initial.copy(
                        localMnnModelId = LocalAiModel.CustomMnn.id,
                        localMnnActivationId = activation
                    )
                )
            }
            coVerify(exactly = 0) { repository.validateModel(any(), any()) }
            verify(exactly = 1) { lease.close() }
        }

    @Test
    fun `given select MNN, catalog selected, expected validation without custom identity`() =
        runTest {
            // Given
            every { lease.activationId } returns activation
            coEvery { getConfiguration() } returns initial
            coEvery { repository.validateOwnedModel("mnn_sd15", "") } returns lease
            coEvery { setConfiguration(any()) } returns Unit

            // When
            val result = SelectMnnModelUseCaseImpl(
                repository,
                getConfiguration,
                setConfiguration
            )("mnn_sd15")

            // Then
            assertTrue(result.isSuccess)
            coVerify {
                setConfiguration(
                    initial.copy(
                        localMnnModelId = "mnn_sd15",
                        localMnnActivationId = activation
                    )
                )
            }
            verify(exactly = 1) { lease.close() }
        }

    @Test
    fun `given select MNN, configuration changes during validation, expected stale commit rejected`() =
        runTest {
            // Given
            coEvery { getConfiguration() } returnsMany listOf(
                initial,
                initial.copy(localMnnBackend = MnnBackend.CPU)
            )
            coEvery { repository.validateOwnedModel(any(), any()) } returns lease

            // When
            val result = SelectMnnModelUseCaseImpl(
                repository,
                getConfiguration,
                setConfiguration
            )("mnn_sd15")

            // Then
            assertTrue(result.exceptionOrNull() is IllegalStateException)
            coVerify(exactly = 0) { setConfiguration(any()) }
            verify(exactly = 1) { lease.close() }
        }

    @Test
    fun `given select MNN, another provider is active, expected no model access`() = runTest {
        // Given
        coEvery { getConfiguration() } returns initial.copy(source = ServerSource.AUTOMATIC1111)

        // When
        val result =
            SelectMnnModelUseCaseImpl(repository, getConfiguration, setConfiguration)("mnn_sd15")

        // Then
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        coVerify(exactly = 0) {
            repository.validateOwnedModel(
                any(),
                any()
            ); setConfiguration(any())
        }
    }

    @Test
    fun `given select MNN, configuration read is cancelled after validation, expected cancellation and lease release`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled")
            var reads = 0
            coEvery { getConfiguration() } answers { if (reads++ == 0) initial else throw cancellation }
            coEvery { repository.validateOwnedModel(any(), any()) } returns lease

            // When
            val failure = runCatching {
                SelectMnnModelUseCaseImpl(
                    repository,
                    getConfiguration,
                    setConfiguration
                )("mnn_sd15")
            }.exceptionOrNull()

            // Then
            assertEquals(cancellation, failure)
            verify(exactly = 1) { lease.close() }
            coVerify(exactly = 0) { setConfiguration(any()) }
        }

    @Test
    fun `given select MNN, commit cancellation and cleanup both fail, expected cancellation with suppressed cleanup`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled")
            val cleanup = IllegalStateException("close")
            every { lease.activationId } returns activation
            every { lease.close() } throws cleanup
            coEvery { getConfiguration() } returns initial
            coEvery { repository.validateOwnedModel(any(), any()) } returns lease
            coEvery { setConfiguration(any()) } throws cancellation

            // When
            val failure = runCatching {
                SelectMnnModelUseCaseImpl(
                    repository,
                    getConfiguration,
                    setConfiguration
                )("mnn_sd15")
            }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
            assertEquals(listOf(cleanup), cancellation.suppressed.toList())
        }
}
