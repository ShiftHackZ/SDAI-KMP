package com.shifthackz.aisdv1.domain.usecase.caching

import com.shifthackz.aisdv1.domain.repository.EmbeddingsRepository
import com.shifthackz.aisdv1.domain.repository.ServerConfigurationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionHyperNetworksRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionModelsRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionSamplersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test

class DataPreLoaderUseCaseImplTest {

    private val stubServerConfigurationRepository = mockk<ServerConfigurationRepository>()
    private val stubStableDiffusionModelsRepository = mockk<StableDiffusionModelsRepository>()
    private val stubStableDiffusionSamplersRepository = mockk<StableDiffusionSamplersRepository>()
    private val stubStableDiffusionHyperNetworksRepository = mockk<StableDiffusionHyperNetworksRepository>()
    private val stubEmbeddingsRepository = mockk<EmbeddingsRepository>()

    private val useCase = DataPreLoaderUseCaseImpl(
        serverConfigurationRepository = stubServerConfigurationRepository,
        sdModelsRepository = stubStableDiffusionModelsRepository,
        sdSamplersRepository = stubStableDiffusionSamplersRepository,
        sdHyperNetworksRepository = stubStableDiffusionHyperNetworksRepository,
        sdEmbeddingsRepository = stubEmbeddingsRepository,
    )

    @Test
    fun `given all data fetched successfully, expected complete value`() = runTest {
        stubSuccess()

        useCase()
    }

    @Test
    fun `given configuration fetch failed, expected error value`() = runTest {
        val stubException = Throwable("Can not fetch configuration.")
        stubSuccess()

        coEvery {
            stubServerConfigurationRepository.fetchConfiguration()
        } throws stubException

        assertUseCaseFails(stubException)
    }

    @Test
    fun `given models fetch failed, expected error value`() = runTest {
        val stubException = Throwable("Can not fetch models.")
        stubSuccess()

        coEvery {
            stubStableDiffusionModelsRepository.fetchModels()
        } throws stubException

        assertUseCaseFails(stubException)
    }

    @Test
    fun `given samplers fetch failed, expected error value`() = runTest {
        val stubException = Throwable("Can not fetch samplers.")
        stubSuccess()

        coEvery {
            stubStableDiffusionSamplersRepository.fetchSamplers()
        } throws stubException

        assertUseCaseFails(stubException)
    }

    @Test
    fun `preload fetches only connection metadata in order`() = runTest {
        stubSuccess()

        useCase()

        coVerifyOrder {
            stubServerConfigurationRepository.fetchConfiguration()
            stubStableDiffusionModelsRepository.fetchModels()
            stubStableDiffusionSamplersRepository.fetchSamplers()
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
            stubEmbeddingsRepository.fetchEmbeddings()
        }
        confirmVerified(
            stubServerConfigurationRepository,
            stubStableDiffusionModelsRepository,
            stubStableDiffusionSamplersRepository,
            stubStableDiffusionHyperNetworksRepository,
            stubEmbeddingsRepository,
        )
    }

    @Test
    fun `given hypernetworks fetch failed, preload continues to embeddings without retry`() = runTest {
        val stubException = IllegalStateException("Can not fetch hypernetworks.")
        stubSuccess()

        coEvery {
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
        } throws stubException

        useCase()

        coVerifyOrder {
            stubServerConfigurationRepository.fetchConfiguration()
            stubStableDiffusionModelsRepository.fetchModels()
            stubStableDiffusionSamplersRepository.fetchSamplers()
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
            stubEmbeddingsRepository.fetchEmbeddings()
        }
        coVerify(exactly = 1) {
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
            stubEmbeddingsRepository.fetchEmbeddings()
        }
    }

    @Test
    fun `given hypernetworks fetch cancelled, cancellation propagates without fetching embeddings`() = runTest {
        val cancellation = CancellationException("Preload cancelled.")
        stubSuccess()
        coEvery {
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
        } throws cancellation

        assertUseCaseFails(cancellation)

        coVerify(exactly = 0) { stubEmbeddingsRepository.fetchEmbeddings() }
    }

    @Test
    fun `given unrecoverable hypernetworks error, preload does not resume`() = runTest {
        val error = AssertionError("Unrecoverable discovery error.")
        stubSuccess()
        coEvery {
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
        } throws error

        assertUseCaseFails(error)

        coVerify(exactly = 0) { stubEmbeddingsRepository.fetchEmbeddings() }
    }

    @Test
    fun `given deadline expires during hypernetworks discovery, preload does not resume`() = runTest {
        stubSuccess()
        coEvery {
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
        } coAnswers { awaitCancellation() }

        val failure = runCatching { withTimeout(100) { useCase() } }.exceptionOrNull()

        assertTrue(failure is CancellationException)
        coVerify(exactly = 0) { stubEmbeddingsRepository.fetchEmbeddings() }
    }

    @Test
    fun `given hypernetworks and embeddings fetch fail, embeddings failure still propagates`() = runTest {
        val embeddingsFailure = IllegalStateException("Can not fetch embeddings.")
        stubSuccess()
        coEvery {
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
        } throws IllegalStateException("Can not fetch hypernetworks.")
        coEvery {
            stubEmbeddingsRepository.fetchEmbeddings()
        } throws embeddingsFailure

        assertUseCaseFails(embeddingsFailure)
    }

    @Test
    fun `given embeddings fetch failed, expected error value`() = runTest {
        val stubException = Throwable("Can not fetch embeddings.")
        stubSuccess()

        coEvery {
            stubEmbeddingsRepository.fetchEmbeddings()
        } throws stubException

        assertUseCaseFails(stubException)
    }

    private fun stubSuccess() {
        coEvery {
            stubServerConfigurationRepository.fetchConfiguration()
        } returns Unit

        coEvery {
            stubStableDiffusionModelsRepository.fetchModels()
        } returns Unit

        coEvery {
            stubStableDiffusionSamplersRepository.fetchSamplers()
        } returns Unit

        coEvery {
            stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks()
        } returns Unit

        coEvery {
            stubEmbeddingsRepository.fetchEmbeddings()
        } returns Unit
    }

    private suspend fun assertUseCaseFails(expected: Throwable) {
        val actual = runCatching { useCase() }.exceptionOrNull()
        assertTrue(actual === expected || actual?.cause === expected)
    }
}
