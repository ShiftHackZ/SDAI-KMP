package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTimeoutFailure
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.mocks.mockConfiguration
import com.shifthackz.aisdv1.domain.mocks.assertRetainsFailure
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestEasyDiffusionConnectivityUseCaseImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises real readiness/catalog policy before candidate publication and rollback after failed commit.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectToEasyDiffusionUseCaseImplTest {
    private val repository = mockk<EasyDiffusionRepository>()
    private val get = mockk<GetConfigurationUseCase>()
    private val set = mockk<SetServerConfigurationUseCase>()
    private val preferences = mockk<PreferenceManager>()
    private val previous = mockConfiguration.copy(easyDiffusionModel = "nested/model")
    private val candidate = "https://server.example/proxy"
    private val useCase = ConnectToEasyDiffusionUseCaseImpl(
        repository, get, set, preferences,
        TestEasyDiffusionConnectivityUseCaseImpl(repository)
    )

    init {
        every { preferences.connectionTimeout } returns NetworkTimeout.TWO_MINUTES
        coEvery { repository.ping(candidate, null) } returns "Online"
        coEvery { repository.fetchModels(candidate) } returns Unit
        coEvery { repository.getModels(candidate) } returns listOf(
            EasyDiffusionModel(
                "nested/model",
                "Model"
            )
        )
        coEvery { get() } returns previous
        coEvery { set(any()) } returns Unit
    }

    @Test
    fun `busy and loading candidates are reachable and publish anonymous independent settings`() =
        runTest {
            listOf("Online", "Rendering", "LoadingModel").forEach { status ->
                coEvery { repository.ping(candidate, null) } returns status
                assertTrue(useCase("  $candidate/  ").isSuccess)
            }
            coVerify(exactly = 3) {
                set(
                    previous.copy(
                        source = ServerSource.EASY_DIFFUSION,
                        demoMode = false,
                        easyDiffusionUrl = candidate,
                        authCredentials = AuthorizationCredentials.None
                    )
                )
            }
        }

    @Test
    fun `missing old checkpoint selects first exact folder ID`() = runTest {
        coEvery { repository.getModels(candidate) } returns listOf(
            EasyDiffusionModel(
                "folder/other",
                "Other"
            )
        )
        assertTrue(useCase(candidate).isSuccess)
        coVerify { set(match { it.easyDiffusionModel == "folder/other" && it.serverUrl == previous.serverUrl }) }
    }

    @Test
    fun `invalid URL startup status empty catalog and fetch failure never commit`() = runTest {
        assertTrue(useCase("file:///tmp/server").isFailure)
        coEvery { repository.ping(candidate, null) } returns "Starting"
        assertTrue(useCase(candidate).isFailure)
        coEvery { repository.ping(candidate, null) } returns "Online"
        coEvery { repository.getModels(candidate) } returns emptyList()
        assertTrue(useCase(candidate).isFailure)
        val failure = IllegalStateException("catalog unavailable")
        coEvery { repository.fetchModels(candidate) } throws failure
        assertRetainsFailure(failure, useCase(candidate).exceptionOrNull())
        coVerify(exactly = 0) { set(any()) }
    }

    @Test
    fun `failed configuration publication rolls back the complete previous snapshot`() = runTest {
        val failure = IllegalStateException("refresh failed")
        coEvery { set(match { it.source == ServerSource.EASY_DIFFUSION }) } throws failure
        assertRetainsFailure(failure, useCase(candidate).exceptionOrNull())
        coVerifyOrder { set(match { it.source == ServerSource.EASY_DIFFUSION }); set(previous) }
    }

    @Test
    fun `rollback failure is retained as suppressed and original failure survives`() = runTest {
        val failure = IllegalStateException("publish")
        val rollback = IllegalStateException("rollback")
        coEvery { set(any()) } throws failure
        coEvery { set(previous) } throws rollback
        assertRetainsFailure(failure, useCase(candidate).exceptionOrNull())
        assertTrue(failure.suppressed.any {
            generateSequence(it) { cause -> cause.cause }.take(16)
                .any { cause -> cause === rollback }
        })
    }

    @Test
    fun `connection deadline returns typed timeout without committing`() = runTest {
        coEvery { repository.fetchModels(candidate) } coAnswers { awaitCancellation() }
        assertTrue(useCase(candidate).exceptionOrNull() is EasyDiffusionTimeoutFailure)
        assertEquals(120000L, testScheduler.currentTime)
        coVerify(exactly = 0) { set(any()) }
    }

    @Test
    fun `caller cancellation propagates instead of becoming failed connection result`() = runTest {
        val cancellation = CancellationException("caller")
        coEvery { repository.fetchModels(candidate) } throws cancellation
        val actual = runCatching { useCase(candidate) }.exceptionOrNull()
        assertTrue(actual is CancellationException)
        assertRetainsFailure(cancellation, actual)
        coVerify(exactly = 0) { set(any()) }
    }
}
