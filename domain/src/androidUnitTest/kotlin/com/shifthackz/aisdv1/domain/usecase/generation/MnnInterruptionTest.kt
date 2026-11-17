package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import io.mockk.every

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Keeps foreground interruption and exact worker cancellation on their distinct ownership paths.
 *
 * @author Dmitriy Moroz
 */
class MnnInterruptionTest {
    @Test
    fun `given iOS MNN worker interruption, stale owner is supplied, expected exact repository cancellation without foreground fallback`() =
        runTest {
            // Given
            val repository = mockk<MnnGenerationRepository>()
            val gateway = mockk<NativeGenerationGateway>()
            val foreground = mockk<InterruptSourceGenerationUseCase>()
            val build = mockk<BuildInfoProvider> { every { platform } returns Platform.IOS }
            coEvery { repository.interruptOwnedGeneration("stale-worker") } returns Unit
            val useCase = InterruptWorkerGenerationUseCaseImpl(
                gateway,
                foreground,
                build,
                mockk(),
                repository
            )

            // When
            useCase(ServerSource.LOCAL_MNN, "stale-worker")

            // Then
            coVerify(exactly = 1) { repository.interruptOwnedGeneration("stale-worker") }
            coVerify(exactly = 0) { gateway.cancelActive(any()); foreground(any()); repository.interruptGeneration() }
        }

    @Test
    fun `given MNN foreground interruption, source is captured, expected MNN repository cancellation`() =
        runTest {
            // Given
            val repository = mockk<MnnGenerationRepository>()
            coEvery { repository.interruptGeneration() } returns Unit
            val useCase = InterruptSourceGenerationUseCaseImpl(
                mockk(),
                mockk(),
                mockk(),
                mockk(),
                mockk(),
                mockk(),
                repository
            )

            // When
            useCase(ServerSource.LOCAL_MNN)

            // Then
            coVerify(exactly = 1) { repository.interruptGeneration() }
        }

    @Test
    fun `given MNN worker interruption, owner is stale, expected exact cancellation without foreground fallback`() =
        runTest {
            // Given
            val gateway = mockk<NativeGenerationGateway>()
            val source = mockk<InterruptSourceGenerationUseCase>()
            coEvery { gateway.cancelActive("stale-worker") } returns false
            val useCase = InterruptWorkerGenerationUseCaseImpl(
                gateway,
                source,
                BuildInfoProvider.stub,
                mockk(),
                mockk()
            )

            // When
            useCase(ServerSource.LOCAL_MNN, "stale-worker")

            // Then
            coVerify(exactly = 1) { gateway.cancelActive("stale-worker") }
            coVerify(exactly = 0) { gateway.cancelActive(null); source(any()) }
        }

    @Test
    fun `given MNN worker interruption, gateway cancellation fails, expected same failure propagated`() =
        runTest {
            // Given
            val gateway = mockk<NativeGenerationGateway>()
            val failure = IllegalStateException("transport")
            coEvery { gateway.cancelActive("worker") } throws failure
            val useCase = InterruptWorkerGenerationUseCaseImpl(
                gateway,
                mockk(),
                BuildInfoProvider.stub,
                mockk(),
                mockk()
            )

            // When
            val actual = runCatching { useCase(ServerSource.LOCAL_MNN, "worker") }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
        }
}
