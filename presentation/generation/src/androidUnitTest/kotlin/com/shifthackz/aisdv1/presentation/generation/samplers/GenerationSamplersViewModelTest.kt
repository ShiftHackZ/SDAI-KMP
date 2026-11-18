package com.shifthackz.aisdv1.presentation.generation.samplers

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


/**
 * Exercises sampler recovery through both production generation view models and their public intents.
 *
 * @param type Generation screen to exercise with the same failure and lifecycle recovery scenarios.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class GenerationSamplersViewModelTest(private val type: AiGenerationResult.Type) {

    private val dispatcher = StandardTestDispatcher()
    private val getSamplers = mockk<GetStableDiffusionSamplersUseCase>()
    private val fixtures = mutableListOf<SamplerRecoveryViewModelFixture>()
    private val samplers = listOf("Euler a", "DPM++ 2M").map {
        StableDiffusionSampler(name = it, aliases = emptyList(), options = emptyMap())
    }

    @Before
    fun initialize() {
        Dispatchers.setMain(dispatcher)
        coEvery { getSamplers() } returns samplers
    }

    @After
    fun cleanUp() {
        fixtures.forEach(SamplerRecoveryViewModelFixture::close)
        Dispatchers.resetMain()
    }

    @Test
    fun `given generation samplers, the initial sampler refresh succeeds, expected choices are populated and a usable sampler is selected`() =
        runTest {
            // Given
            // initialize() stubs the existing sampler catalog and createFixture supplies the production
            // generation ViewModel.

            // When
            val fixture = createFixture()
            advanceUntilIdle()

            // Then
            assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
            assertTrue(fixture.state.selectedSampler in fixture.state.availableSamplers)
            assertFalse(fixture.state.isLoadingSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            assertTrue(fixture.errors.isEmpty())

        }

    @Test
    fun `given generation samplers, recovery is requested while loading, expected only one sampler request remains active`() =
        runTest {
            // Given

            val response = CompletableDeferred<List<StableDiffusionSampler>>()
            coEvery { getSamplers() } coAnswers { response.await() }
            val fixture = createFixture()
            runCurrent()

            assertTrue(fixture.state.isLoadingSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            // When
            fixture.retry()
            fixture.retry()
            runCurrent()
            // Then
            coVerify(exactly = 1) { getSamplers() }

            // When
            response.complete(samplers)
            advanceUntilIdle()

            // Then
            assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
            assertFalse(fixture.state.isLoadingSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)

        }

    @Test
    fun `given generation samplers, a failed initial refresh is retried on resume, expected sampler choices load successfully`() =
        runTest {
            // Given

            val failure = IllegalStateException("HTTP 503")
            coEvery { getSamplers() } throws failure
            val fixture = createFixture()
            advanceUntilIdle()

            assertTrue(fixture.state.availableSamplers.isEmpty())
            assertTrue(fixture.state.hasSamplerLoadFailed)
            assertFalse(fixture.state.isLoadingSamplers)
            assertEquals(1, fixture.errors.size)
            assertEquals(failure.message, fixture.errors.single().message)
            assertTrue(fixture.errors.single() is IllegalStateException)

            val response = CompletableDeferred<List<StableDiffusionSampler>>()
            coEvery { getSamplers() } coAnswers { response.await() }
            // When
            fixture.retry()
            runCurrent()

            // Then
            assertTrue(fixture.state.isLoadingSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            // When
            fixture.retry()
            runCurrent()
            // Then
            coVerify(exactly = 2) { getSamplers() }

            // When
            response.complete(samplers)
            advanceUntilIdle()

            // Then
            assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            assertFalse(fixture.state.isLoadingSamplers)
            coVerify(exactly = 2) { getSamplers() }

        }

    @Test
    fun `given generation samplers, empty responses and failed retries are retried, expected a later success populates sampler choices`() =
        runTest {
            // Given

            coEvery { getSamplers() } returns emptyList()
            val fixture = createFixture()
            advanceUntilIdle()
            assertTrue(fixture.state.hasSamplerLoadFailed)

            coEvery { getSamplers() } throws IllegalStateException("HTTP 503")
            // When
            fixture.retry()
            advanceUntilIdle()
            // Then
            assertTrue(fixture.state.hasSamplerLoadFailed)
            assertFalse(fixture.state.isLoadingSamplers)

            // Given
            val response = CompletableDeferred<List<StableDiffusionSampler>>()
            coEvery { getSamplers() } coAnswers { response.await() }
            // When
            fixture.retry()
            runCurrent()

            // Then
            assertTrue(fixture.state.isLoadingSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            // When
            fixture.retry()
            runCurrent()
            // Then
            coVerify(exactly = 3) { getSamplers() }

            // When
            response.complete(samplers)
            advanceUntilIdle()

            // Then
            assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            assertFalse(fixture.state.isLoadingSamplers)
            coVerify(exactly = 3) { getSamplers() }

        }

    @Test
    fun `given generation samplers, settings emit during construction, expected the sampler retry key remains valid`() =
        runTest {
            // Given

            coEvery { getSamplers() } returns emptyList()
            // When
            val fixture = createFixture(UnconfinedTestDispatcher(testScheduler))
            advanceUntilIdle()
            // Then
            assertTrue(fixture.state.hasSamplerLoadFailed)

            // Given
            coEvery { getSamplers() } returns samplers
            // When
            fixture.retry()
            advanceUntilIdle()

            // Then
            assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
            assertFalse(fixture.state.isLoadingSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            coVerify(exactly = 2) { getSamplers() }

        }

    @Test
    fun `given generation samplers, the view model is recreated, expected a new sampler request fetches fresh choices`() =
        runTest {
            // Given

            val original = createFixture()
            advanceUntilIdle()
            assertEquals(samplers.map { it.name }, original.state.availableSamplers)
            original.close()

            val replacement = samplers.map { it.copy(name = "new-${it.name}") }
            coEvery { getSamplers() } returns replacement
            // When
            val recreated = createFixture()
            advanceUntilIdle()

            // Then
            assertEquals(replacement.map { it.name }, recreated.state.availableSamplers)
            assertFalse(recreated.state.hasSamplerLoadFailed)
            coVerify(exactly = 2) { getSamplers() }

        }

    @Test
    fun `given generation samplers, recovery and unrelated settings arrive after loading, expected populated sampler choices are not refetched`() =
        runTest {
            // Given

            val fixture = createFixture()
            advanceUntilIdle()

            // When
            fixture.retry()
            fixture.settings.value = fixture.settings.value.copy(sdModel = "another-checkpoint")
            advanceUntilIdle()

            // Then
            assertEquals(samplers.map { it.name }, fixture.state.availableSamplers)
            coVerify(exactly = 1) { getSamplers() }

        }

    @Test
    fun `given generation samplers, the provider changes while loading, expected the pending request is cancelled without a visible failure`() =
        runTest {
            // Given

            val response = CompletableDeferred<List<StableDiffusionSampler>>()
            coEvery { getSamplers() } coAnswers { response.await() }
            val fixture = createFixture()
            runCurrent()

            // When
            fixture.settings.value = fixture.settings.value.copy(source = ServerSource.STABILITY_AI)
            runCurrent()
            response.complete(samplers)
            fixture.retry()
            advanceUntilIdle()

            // Then
            assertEquals(ServerSource.STABILITY_AI, fixture.state.mode)
            assertFalse(fixture.state.isLoadingSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            assertTrue(fixture.errors.isEmpty())
            coVerify(exactly = 1) { getSamplers() }

        }

    @Test
    fun `given generation samplers, a previous server responds after a server switch, expected only the new server sampler choices remain`() =
        runTest {
            // Given

            val oldResponse = CompletableDeferred<List<StableDiffusionSampler>>()
            val replacement = samplers.map { it.copy(name = "new-${it.name}") }
            var attempts = 0
            coEvery { getSamplers() } coAnswers {
                if (++attempts == 1) withContext(NonCancellable) { oldResponse.await() }
                else replacement
            }
            val fixture = createFixture()
            runCurrent()

            // When
            fixture.settings.value = fixture.settings.value.copy(serverUrl = "http://server-b:7860")
            runCurrent()
            // Then
            assertEquals(replacement.map { it.name }, fixture.state.availableSamplers)

            // When
            oldResponse.complete(samplers)
            advanceUntilIdle()

            // Then
            assertEquals(replacement.map { it.name }, fixture.state.availableSamplers)
            assertFalse(fixture.state.isLoadingSamplers)
            assertFalse(fixture.state.hasSamplerLoadFailed)
            assertTrue(fixture.errors.isEmpty())
            coVerify(exactly = 2) { getSamplers() }

        }

    /**
     * Registers the production view model for scope cleanup after each parameterized test case.
     *
     * @param immediateDispatcher Entry dispatcher; IO continues to use the controlled test dispatcher.
     * @author Dmitriy Moroz
     */
    private fun createFixture(immediateDispatcher: CoroutineDispatcher = dispatcher) =
        SamplerRecoveryViewModelFixture(type, dispatcher, getSamplers, immediateDispatcher)
            .also(fixtures::add)

    companion object {
        /**
         * Runs every regression against both generation screens sharing the sampler field contract.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun generationTypes() = listOf(
            AiGenerationResult.Type.TEXT_TO_IMAGE,
            AiGenerationResult.Type.IMAGE_TO_IMAGE,
        )
    }
}
