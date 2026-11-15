package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import kotlinx.coroutines.Dispatchers
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.mocks.ConfigurationStoreStub
import com.shifthackz.aisdv1.domain.mocks.mockConfiguration
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultSetServerConfigurationUseCaseImplTest {

    private val stubConfigurationStore = ConfigurationStoreStub()
    private val stubAuthorizationStore = mockk<AuthorizationStore>()
    private val stubPreferenceManager = mockk<PreferenceManager>()

    private val useCase = DefaultSetServerConfigurationUseCaseImpl(
        dispatchersProvider = mockk<DispatchersProvider> { every { io } returns Dispatchers.Unconfined },
        configurationStore = stubConfigurationStore,
        providerSelectionGuard = ProviderSelectionGuard(),
        authorizationStore = stubAuthorizationStore,
        preferenceManager = stubPreferenceManager,
    )

    @Test
    fun `given configuration apply success, expected complete value`() = runTest {
        coEvery {
            stubAuthorizationStore.storeAuthorizationCredentials(any(), any())
        } coAnswers {
            secondArg<suspend (String) -> Unit>().invoke("configuration-test")
        }
        coEvery {
            stubPreferenceManager.refresh()
        } returns Unit

        useCase(mockConfiguration)

        assertEquals(
            mockConfiguration,
            stubConfigurationStore.getConfiguration(mockConfiguration.authCredentials)
        )
        coVerify {
            stubPreferenceManager.refresh()
        }
    }
}
