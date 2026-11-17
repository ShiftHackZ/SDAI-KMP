package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Protects platform routing and the worker UUID cancellation boundary.
 *
 * @author Dmitriy Moroz
 */
class InterruptWorkerGenerationUseCaseImplTest {
    private val gateway = mockk<NativeGenerationGateway>()
    private val interruptSource = mockk<InterruptSourceGenerationUseCase>(relaxed = true)
    private val buildInfo = mockk<BuildInfoProvider>()
    private val useCase =
        InterruptWorkerGenerationUseCaseImpl(gateway, interruptSource, buildInfo, mockk(), mockk())

    @Test
    fun `given Android Bonsai worker, cancellation uses its exact owner`() = runTest {
        every { buildInfo.platform } returns Platform.ANDROID
        coEvery { gateway.cancelActive("worker-id") } returns true

        useCase(ServerSource.LOCAL_APPLE_BONSAI, "worker-id")

        coVerify(exactly = 1) { gateway.cancelActive("worker-id") }
        coVerify(exactly = 0) { interruptSource(any()) }
    }

    @Test
    fun `given stale Android Bonsai owner, cancellation never falls back to unscoped interrupt`() =
        runTest {
            every { buildInfo.platform } returns Platform.ANDROID
            coEvery { gateway.cancelActive("old-worker") } returns false

            useCase(ServerSource.LOCAL_APPLE_BONSAI, "old-worker")

            coVerify(exactly = 1) { gateway.cancelActive("old-worker") }
            coVerify(exactly = 0) { gateway.cancelActive(null) }
            coVerify(exactly = 0) { interruptSource(any()) }
        }

    @Test
    fun `given iOS Bonsai worker, legacy runtime receives cancellation`() = runTest {
        every { buildInfo.platform } returns Platform.IOS

        useCase(ServerSource.LOCAL_APPLE_BONSAI, "worker-id")

        coVerify(exactly = 1) { interruptSource(ServerSource.LOCAL_APPLE_BONSAI) }
        coVerify(exactly = 0) { gateway.cancelActive(any()) }
    }

    @Test
    fun `given other Android native engines, cancellation keeps worker ownership`() = runTest {
        coEvery { gateway.cancelActive("worker-id") } returns true
        for (source in listOf(
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP
        )) {
            useCase(source, "worker-id")
        }

        coVerify(exactly = 3) { gateway.cancelActive("worker-id") }
        coVerify(exactly = 0) { interruptSource(any()) }
    }

    @Test
    fun `given captured remote source, cancellation uses that source directly`() = runTest {
        useCase(ServerSource.AUTOMATIC1111, "worker-id")

        coVerify(exactly = 1) { interruptSource(ServerSource.AUTOMATIC1111) }
        coVerify(exactly = 0) { gateway.cancelActive(any()) }
    }

    @Test
    fun `given iOS CoreML, cancellation keeps the existing runtime path`() = runTest {
        useCase(ServerSource.LOCAL_APPLE_CORE_ML, "worker-id")

        coVerify(exactly = 1) { interruptSource(ServerSource.LOCAL_APPLE_CORE_ML) }
        coVerify(exactly = 0) { gateway.cancelActive(any()) }
    }

    @Test
    fun `given cancellation failure, original failure propagates without fallback`() = runTest {
        every { buildInfo.platform } returns Platform.ANDROID
        val failure = IllegalStateException("connection unavailable")
        coEvery { gateway.cancelActive("worker-id") } throws failure

        assertSame(
            failure,
            runCatching { useCase(ServerSource.LOCAL_APPLE_BONSAI, "worker-id") }.exceptionOrNull()
        )
        coVerify(exactly = 0) { interruptSource(any()) }
    }
}
