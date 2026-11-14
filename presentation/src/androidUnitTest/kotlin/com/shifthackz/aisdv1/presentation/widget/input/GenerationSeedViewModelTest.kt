package com.shifthackz.aisdv1.presentation.widget.input

import com.shifthackz.aisdv1.core.validation.di.nativeSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.validatorsModule
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import com.shifthackz.aisdv1.presentation.screen.samplers.SamplerRecoveryViewModelFixture
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
        )
    }

    @After
    fun cleanUp() {
        fixture.close()
        dependencyGraph.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `manual edits publish independent errors and clear them when corrected`() = runTest {
        advanceUntilIdle()
        fixture.process(GenerationInputFormEvent.UpdateSeed("6687881544318117707"))
        fixture.process(GenerationInputFormEvent.UpdateSubSeed("9007199254740992"))
        assertNotNull(fixture.state.seedValidationError)
        assertNotNull(fixture.state.subSeedValidationError)
        assertEquals("6687881544318117707", fixture.state.seed)

        fixture.process(GenerationInputFormEvent.UpdateSeed(""))
        assertNull(fixture.state.seedValidationError)
        assertNotNull(fixture.state.subSeedValidationError)
        fixture.process(GenerationInputFormEvent.UpdateSubSeed("4294967293"))
        assertNull(fixture.state.seedValidationError)
        assertNull(fixture.state.subSeedValidationError)
    }

    @Test
    fun `provider changes republish errors using the corresponding injected range`() = runTest {
        advanceUntilIdle()
        fixture.process(GenerationInputFormEvent.UpdateSeed(Long.MAX_VALUE.toString()))
        fixture.process(GenerationInputFormEvent.UpdateSubSeed("invalid"))
        assertNotNull(fixture.state.seedValidationError)
        assertNotNull(fixture.state.subSeedValidationError)

        fixture.settings.value = fixture.settings.value.copy(source = ServerSource.LOCAL_MICROSOFT_ONNX)
        advanceUntilIdle()
        assertNull(fixture.state.seedValidationError)
        assertNull(fixture.state.subSeedValidationError)

        fixture.settings.value = fixture.settings.value.copy(source = ServerSource.SWARM_UI)
        advanceUntilIdle()
        assertNotNull(fixture.state.seedValidationError)
        assertNotNull(fixture.state.subSeedValidationError)

        fixture.settings.value = fixture.settings.value.copy(source = ServerSource.OPEN_AI_COMPATIBLE)
        advanceUntilIdle()
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
