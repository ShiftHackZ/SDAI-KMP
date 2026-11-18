package com.shifthackz.aisdv1.presentation.configuration.loader

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.router.ConfigurationLoaderRouter
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Ensures local MNN startup never waits for remote provider metadata.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MnnConfigurationLoaderTest {
    @Test
    fun `given MNN startup configuration, catalog network is unavailable, expected home without remote preload`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dispatchers = object : DispatchersProvider {
                override val io: CoroutineDispatcher = dispatcher
                override val ui: CoroutineDispatcher = dispatcher
                override val immediate: CoroutineDispatcher = dispatcher
            }
            val configuration = mockk<GetConfigurationUseCase>()
            val preload = mockk<DataPreLoaderUseCase>()
            val router = mockk<ConfigurationLoaderRouter>(relaxed = true)
            coEvery { configuration() } returns Configuration(
                source = ServerSource.LOCAL_MNN,
                localMnnModelId = "mnn_sd15"
            )

            // When
            val model = ConfigurationLoaderViewModel(
                diagnostics = mockk(relaxed = true),
                dispatchersProvider = dispatchers,
                getConfigurationUseCase = configuration,
                dataPreLoaderUseCase = preload,
                router = router
            )
            advanceUntilIdle()

            // Then
            assertEquals(ConfigurationLoaderState.Status.Launching, model.state.value.status)
            coVerify(exactly = 0) { preload() }
            verify(exactly = 1) { router.navigateToHomeScreen() }
        }
}
