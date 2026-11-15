package com.shifthackz.aisdv1.domain.usecase.sdhypernet

import com.shifthackz.aisdv1.domain.mocks.mockStableDiffusionHyperNetworks
import com.shifthackz.aisdv1.domain.repository.StableDiffusionHyperNetworksRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Test

class FetchAndGetHyperNetworksUseCaseImplTest {

    private val stubRepository = mockk<StableDiffusionHyperNetworksRepository>()

    private val useCase = FetchAndGetHyperNetworksUseCaseImpl(stubRepository)

    @Test
    fun `given refresh succeeds, reads updated hypernetworks after fetching`() = runTest {
        coEvery { stubRepository.fetchHyperNetworks() } returns Unit
        coEvery { stubRepository.getHyperNetworks() } returns mockStableDiffusionHyperNetworks

        val actual = useCase()

        Assert.assertEquals(mockStableDiffusionHyperNetworks, actual)
        coVerifyOrder {
            stubRepository.fetchHyperNetworks()
            stubRepository.getHyperNetworks()
        }
    }

    @Test
    fun `given refresh succeeds with no hypernetworks, returns empty list`() = runTest {
        coEvery { stubRepository.fetchHyperNetworks() } returns Unit
        coEvery { stubRepository.getHyperNetworks() } returns emptyList()

        val actual = useCase()

        Assert.assertEquals(emptyList<Any>(), actual)
    }

    @Test
    fun `given refresh fails, uses cached hypernetworks`() = runTest {
        coEvery { stubRepository.fetchHyperNetworks() } throws IllegalStateException("Discovery failed.")
        coEvery { stubRepository.getHyperNetworks() } returns mockStableDiffusionHyperNetworks

        Assert.assertEquals(mockStableDiffusionHyperNetworks, useCase())
        coVerifyOrder {
            stubRepository.fetchHyperNetworks()
            stubRepository.getHyperNetworks()
        }
    }

    @Test
    fun `given refresh fails before anything was cached, returns empty list`() = runTest {
        coEvery { stubRepository.fetchHyperNetworks() } throws IllegalStateException("Discovery failed.")
        coEvery { stubRepository.getHyperNetworks() } returns emptyList()

        Assert.assertTrue(useCase().isEmpty())
    }

    @Test
    fun `given discovery initially fails, next picker request retries and reads updated entries`() = runTest {
        coEvery {
            stubRepository.fetchHyperNetworks()
        } throws IllegalStateException("Discovery failed.") andThen Unit
        coEvery { stubRepository.getHyperNetworks() } returns emptyList() andThen mockStableDiffusionHyperNetworks

        Assert.assertTrue(useCase().isEmpty())
        Assert.assertEquals(mockStableDiffusionHyperNetworks, useCase())

        coVerify(exactly = 2) {
            stubRepository.fetchHyperNetworks()
            stubRepository.getHyperNetworks()
        }
    }

    @Test
    fun `given cache read fails after successful refresh, cache failure propagates`() = runTest {
        val cacheFailure = IllegalStateException("Cache unavailable.")
        coEvery { stubRepository.fetchHyperNetworks() } returns Unit
        coEvery { stubRepository.getHyperNetworks() } throws cacheFailure

        Assert.assertSame(cacheFailure, runCatching { useCase() }.exceptionOrNull())
    }

    @Test
    fun `given refresh and cache read both fail, cache failure propagates`() = runTest {
        val cacheFailure = IllegalStateException("Cache unavailable.")
        coEvery { stubRepository.fetchHyperNetworks() } throws IllegalStateException("Discovery failed.")
        coEvery { stubRepository.getHyperNetworks() } throws cacheFailure

        Assert.assertSame(cacheFailure, runCatching { useCase() }.exceptionOrNull())
    }

    @Test
    fun `given discovery cancelled, cancellation propagates without reading cache`() = runTest {
        val cancellation = CancellationException("Picker closed.")
        coEvery { stubRepository.fetchHyperNetworks() } throws cancellation

        Assert.assertSame(cancellation, runCatching { useCase() }.exceptionOrNull())
        coVerify(exactly = 0) { stubRepository.getHyperNetworks() }
    }

    @Test
    fun `given deadline expires during discovery, timeout propagates without reading cache`() = runTest {
        coEvery {
            stubRepository.fetchHyperNetworks()
        } coAnswers { awaitCancellation() }

        val failure = runCatching { withTimeout(100) { useCase() } }.exceptionOrNull()

        Assert.assertTrue(failure is CancellationException)
        coVerify(exactly = 0) { stubRepository.getHyperNetworks() }
    }

    @Test
    fun `given unrecoverable discovery error, error propagates without reading cache`() = runTest {
        val error = AssertionError("Unrecoverable discovery error.")
        coEvery { stubRepository.fetchHyperNetworks() } throws error

        Assert.assertSame(error, runCatching { useCase() }.exceptionOrNull())
        coVerify(exactly = 0) { stubRepository.getHyperNetworks() }
    }
}
