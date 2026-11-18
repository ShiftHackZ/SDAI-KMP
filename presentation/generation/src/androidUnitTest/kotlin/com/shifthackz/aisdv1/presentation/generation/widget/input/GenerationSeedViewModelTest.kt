package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.core.validation.di.nativeSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.easyDiffusionSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.validatorsModule
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import com.shifthackz.aisdv1.presentation.generation.samplers.SamplerRecoveryViewModelFixture
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.dsl.koinApplication


/**
 * Verifies both production ViewModels publish fresh seed feedback using Koin-configured validators.
 *
 * @param type generation screen whose edit and provider-change paths are exercised.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class GenerationSeedViewModelTest(private val type: AiGenerationResult.Type) {
    private val dispatcher = StandardTestDispatcher()
    private val dependencyGraph = koinApplication { modules(validatorsModule) }
    private lateinit var fixture: SamplerRecoveryViewModelFixture

    @Before
    fun initialize() {
        Dispatchers.setMain(dispatcher)
        val getSamplers = mockk<GetStableDiffusionSamplersUseCase> {
            coEvery { this@mockk.invoke() } returns listOf(
                StableDiffusionSampler("Euler a", emptyList(), emptyMap()),
            )
        }
        fixture = SamplerRecoveryViewModelFixture(
            type = type,
            dispatcher = dispatcher,
            getSamplers = getSamplers,
            seedValidator = dependencyGraph.koin.get(),
            nativeSeedValidator = dependencyGraph.koin.get(nativeSeedValidatorQualifier),
            easyDiffusionSeedValidator = dependencyGraph.koin.get(
                easyDiffusionSeedValidatorQualifier
            ),
        )
    }

    @After
    fun cleanUp() {
        fixture.close()
        dependencyGraph.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `given generation seed, manual seeds are edited and corrected, expected independent field errors appear and then clear`() =
        runTest {
            // Given
            // The parameterized fixture owns the production generation form and injected seed validators;
            // the initial scheduler is settled before editing.

            advanceUntilIdle()
            // When
            fixture.process(GenerationInputFormEvent.UpdateSeed("6687881544318117707"))
            fixture.process(GenerationInputFormEvent.UpdateSubSeed("9007199254740992"))
            // Then
            assertNotNull(fixture.state.seedValidationError)
            assertNotNull(fixture.state.subSeedValidationError)
            assertEquals("6687881544318117707", fixture.state.seed)

            // When
            fixture.process(GenerationInputFormEvent.UpdateSeed(""))
            // Then
            assertNull(fixture.state.seedValidationError)
            assertNotNull(fixture.state.subSeedValidationError)
            // When
            fixture.process(GenerationInputFormEvent.UpdateSubSeed("4294967293"))
            // Then
            assertNull(fixture.state.seedValidationError)
            assertNull(fixture.state.subSeedValidationError)

        }

    @Test
    fun `given generation seed, the selected provider changes, expected seed errors use the injected provider range`() =
        runTest {
            // Given

            advanceUntilIdle()
            fixture.process(GenerationInputFormEvent.UpdateSeed(Long.MAX_VALUE.toString()))
            fixture.process(GenerationInputFormEvent.UpdateSubSeed("invalid"))
            assertNotNull(fixture.state.seedValidationError)
            assertNotNull(fixture.state.subSeedValidationError)

            // When
            fixture.settings.value =
                fixture.settings.value.copy(source = ServerSource.LOCAL_MICROSOFT_ONNX)
            advanceUntilIdle()
            // Then
            assertNull(fixture.state.seedValidationError)
            assertNull(fixture.state.subSeedValidationError)

            // When
            fixture.settings.value = fixture.settings.value.copy(source = ServerSource.SWARM_UI)
            advanceUntilIdle()
            // Then
            assertNotNull(fixture.state.seedValidationError)
            assertNotNull(fixture.state.subSeedValidationError)

            // When
            fixture.settings.value =
                fixture.settings.value.copy(source = ServerSource.OPEN_AI_COMPATIBLE)
            advanceUntilIdle()
            // Then
            assertNull(fixture.state.seedValidationError)
            assertNull(fixture.state.subSeedValidationError)

        }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun generationTypes() = listOf(
            AiGenerationResult.Type.TEXT_TO_IMAGE,
            AiGenerationResult.Type.IMAGE_TO_IMAGE,
        )
    }
}
