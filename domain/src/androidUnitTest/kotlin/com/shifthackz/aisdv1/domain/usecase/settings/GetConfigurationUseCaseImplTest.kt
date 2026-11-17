package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.mocks.ConfigurationStoreStub
import com.shifthackz.aisdv1.domain.mocks.mockConfiguration
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.gateway.MnnBackendPolicy
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultGetConfigurationUseCaseImplTest {

    private val stubConfigurationStore = ConfigurationStoreStub(mockConfiguration)
    private val stubAuthorizationStore = mockk<AuthorizationStore>()

    private val useCase = DefaultGetConfigurationUseCaseImpl(
        configurationStore = stubConfigurationStore,
        authorizationStore = stubAuthorizationStore,
        mnnBackendPolicy = mockk<MnnBackendPolicy> {
            every { supportedBackends } returns listOf(MnnBackend.CPU, MnnBackend.OPENCL)
        },
    )

    @Test
    fun `given configuration loading, authorization read succeeds, expected complete saved configuration`() =
        runTest {
            // Given
            coEvery {
                stubAuthorizationStore.getAuthorizationCredentials()
            } returns AuthorizationCredentials.None

            // When
            val actual = useCase()

            // Then
            assertEquals(mockConfiguration, actual)
        }

    @Test
    fun `given configuration loading, foreign MNN backend is restored, expected persisted CPU before capture`() =
        runTest {
            // Given
            stubConfigurationStore.localMnnBackend = MnnBackend.METAL
            coEvery { stubAuthorizationStore.getAuthorizationCredentials() } returns AuthorizationCredentials.None

            // When
            val actual = useCase()

            // Then
            assertEquals(MnnBackend.CPU, actual.localMnnBackend)
            assertEquals(MnnBackend.CPU, stubConfigurationStore.localMnnBackend)
        }
}
