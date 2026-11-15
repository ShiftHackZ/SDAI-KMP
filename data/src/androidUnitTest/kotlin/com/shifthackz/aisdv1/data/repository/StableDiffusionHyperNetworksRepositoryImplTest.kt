package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.gateway.AuthenticatedConnectionProviderImpl
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.data.mocks.mockStableDiffusionHyperNetworks
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionHyperNetworksDataSource
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class StableDiffusionHyperNetworksRepositoryImplTest {

    private val stubException = IllegalStateException("Something went wrong.")
    private val stubRemoteDataSource = mockk<StableDiffusionHyperNetworksDataSource.Remote>()
    private val stubLocalDataSource = mockk<StableDiffusionHyperNetworksDataSource.Local>()
    private val stubPreferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val stubAuthorizationStore = mockk<AuthorizationStore>()

    private val repository = StableDiffusionHyperNetworksRepositoryImpl(
        remoteDataSource = stubRemoteDataSource,
        localDataSource = stubLocalDataSource,
        preferenceManager = stubPreferenceManager,
        connections = AuthenticatedConnectionProviderImpl(
            stubPreferenceManager,
            stubAuthorizationStore,
            ProviderSelectionGuard()
        ),
    )

    @Before
    fun initialize() {
        every {
            stubPreferenceManager.automatic1111ServerUrl
        } returns A1111_URL

        coEvery {
            stubAuthorizationStore.getAuthorizationCredentials()
        } returns AuthorizationCredentials.None
    }

    @Test
    fun `given attempt to fetch hyper networks, source is AUTOMATIC1111, remote returns data, local insert success, expected complete value`() =
        runTest {
            every {
                stubPreferenceManager.source
            } returns ServerSource.AUTOMATIC1111

            coEvery {
                stubRemoteDataSource.fetchHyperNetworks(A1111_URL, AuthorizationCredentials.None)
            } returns mockStableDiffusionHyperNetworks

            coEvery {
                stubLocalDataSource.insertHyperNetworks(any())
            } returns Unit

            repository.fetchHyperNetworks()

            coVerify {
                stubLocalDataSource.insertHyperNetworks(mockStableDiffusionHyperNetworks)
            }
        }

    @Test
    fun `given attempt to fetch hyper networks, source is SWARM_UI, expected no remote fetch`() =
        runTest {
            every {
                stubPreferenceManager.source
            } returns ServerSource.SWARM_UI

            repository.fetchHyperNetworks()

            coVerify(exactly = 0) {
                stubRemoteDataSource.fetchHyperNetworks(any(), any())
                stubLocalDataSource.insertHyperNetworks(any())
            }
        }

    @Test
    fun `given attempt to fetch hyper networks, remote throws exception, expected error value`() =
        runTest {
            every {
                stubPreferenceManager.source
            } returns ServerSource.AUTOMATIC1111

            coEvery {
                stubRemoteDataSource.fetchHyperNetworks(A1111_URL, AuthorizationCredentials.None)
            } throws stubException

            val actual = runCatching { repository.fetchHyperNetworks() }

            Assert.assertEquals(stubException, actual.exceptionOrNull())
        }

    @Test
    fun `given attempt to fetch hyper networks, remote returns data, local insert fails, expected error value`() =
        runTest {
            every {
                stubPreferenceManager.source
            } returns ServerSource.AUTOMATIC1111

            coEvery {
                stubRemoteDataSource.fetchHyperNetworks(A1111_URL, AuthorizationCredentials.None)
            } returns mockStableDiffusionHyperNetworks

            coEvery {
                stubLocalDataSource.insertHyperNetworks(any())
            } throws stubException

            val actual = runCatching { repository.fetchHyperNetworks() }

            Assert.assertEquals(stubException, actual.exceptionOrNull())
        }

    @Test
    fun `given attempt to get hyper networks, local data source returns list, expected valid domain models list value`() =
        runTest {
            coEvery {
                stubLocalDataSource.getHyperNetworks()
            } returns mockStableDiffusionHyperNetworks

            val actual = repository.getHyperNetworks()

            Assert.assertEquals(mockStableDiffusionHyperNetworks, actual)
        }

    @Test
    fun `given attempt to get hyper networks, local data source returns empty list, expected empty domain models list value`() =
        runTest {
            coEvery {
                stubLocalDataSource.getHyperNetworks()
            } returns emptyList()

            val actual = repository.getHyperNetworks()

            Assert.assertEquals(emptyList<Any>(), actual)
        }

    @Test
    fun `given attempt to get hyper networks, local data source throws exception, expected error value`() =
        runTest {
            coEvery {
                stubLocalDataSource.getHyperNetworks()
            } throws stubException

            val actual = runCatching { repository.getHyperNetworks() }

            Assert.assertEquals(stubException, actual.exceptionOrNull())
        }

    @Test
    fun `given discovery cancelled, repository propagates cancellation without reading or replacing cache`() = runTest {
        val cancellation = CancellationException("Picker closed.")
        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        coEvery {
            stubRemoteDataSource.fetchHyperNetworks(A1111_URL, AuthorizationCredentials.None)
        } throws cancellation

        val failure = runCatching { repository.fetchHyperNetworks() }.exceptionOrNull()

        Assert.assertSame(cancellation, failure)
        coVerify(exactly = 0) {
            stubLocalDataSource.getHyperNetworks()
            stubLocalDataSource.insertHyperNetworks(any())
        }
    }

    @Test
    fun `given unrecoverable discovery error, repository propagates it without reading or replacing cache`() = runTest {
        val error = AssertionError("Unrecoverable discovery error.")
        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        coEvery {
            stubRemoteDataSource.fetchHyperNetworks(A1111_URL, AuthorizationCredentials.None)
        } throws error

        val failure = runCatching { repository.fetchHyperNetworks() }.exceptionOrNull()

        Assert.assertSame(error, failure)
        coVerify(exactly = 0) {
            stubLocalDataSource.getHyperNetworks()
            stubLocalDataSource.insertHyperNetworks(any())
        }
    }

    private companion object {
        const val A1111_URL = "http://192.168.0.1:7860"
    }
}
