package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestSwarmUiConnectivityUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Verifies provider setup deadlines and rollback through both production connection use cases.
 *
 * @param isSwarmUi Whether to exercise Swarm instead of A1111.
 * @param selectedTimeout Persisted connection timeout selected before setup starts.
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class ConnectionSetupTimeoutTest(
    private val isSwarmUi: Boolean,
    private val selectedTimeout: NetworkTimeout,
) {
    private val previous = Configuration(serverUrl = "http://previous:7860")
    private val configuration = mockk<GetConfigurationUseCase> { coEvery { this@mockk.invoke() } returns previous }
    private val save = mockk<SetServerConfigurationUseCase> { coEvery { this@mockk.invoke(any()) } returns Unit }
    private val preferences = mockk<PreferenceManager> {
        every { connectionTimeout } returns selectedTimeout
    }
    private var responseDelayMillis = 45_000L
    private val connectivity = mockk<TestConnectivityUseCase> {
        coEvery { this@mockk.invoke(any()) } coAnswers { delay(responseDelayMillis) }
    }
    private val swarmConnectivity = mockk<TestSwarmUiConnectivityUseCase> {
        coEvery { this@mockk.invoke(any()) } coAnswers { delay(responseDelayMillis) }
    }
    private val preload = mockk<DataPreLoaderUseCase> { coEvery { this@mockk.invoke() } returns Unit }

    @Test
    fun `setup can complete after the former 30 second deadline`() = runTest {
        assertTrue(connect().isSuccess)
        coVerify(exactly = 0) { save(previous) }
    }

    @Test
    fun `selected deadline rolls back while no limit permits a long setup`() = runTest {
        responseDelayMillis = if (selectedTimeout == NetworkTimeout.NO_LIMIT) 3_600_000L
            else selectedTimeout.seconds * 1000L + 1L

        val outcome = connect()

        if (selectedTimeout == NetworkTimeout.NO_LIMIT) {
            assertTrue(outcome.isSuccess)
            coVerify(exactly = 0) { save(previous) }
        } else {
            assertTrue(outcome.isFailure)
            coVerify(exactly = 1) { save(previous) }
        }
    }

    /** Runs the selected production setup with the same response delay and saved settings.
     * @author Dmitriy Moroz */
    private suspend fun connect(): Result<Unit> = if (isSwarmUi) {
        DefaultConnectToSwarmUiUseCaseImpl(configuration, save, swarmConnectivity, preferences)
            .invoke("http://candidate:7860", AuthorizationCredentials.None)
    } else {
        DefaultConnectToA1111UseCaseImpl(configuration, save, connectivity, preload, preferences)
            .invoke("http://candidate:7860", false, AuthorizationCredentials.None)
    }

    companion object {
        /** All settings are exercised for both providers without wall-clock waiting.
         * @author Dmitriy Moroz */
        @JvmStatic
        @Parameterized.Parameters(name = "swarm={0}, timeout={1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { isSwarmUi ->
            NetworkTimeout.entries.map { arrayOf(isSwarmUi, it) }
        }
    }
}
