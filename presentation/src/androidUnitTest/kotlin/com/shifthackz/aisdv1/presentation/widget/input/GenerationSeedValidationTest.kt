package com.shifthackz.aisdv1.presentation.widget.input

import com.shifthackz.aisdv1.core.validation.di.nativeSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.validatorsModule
import com.shifthackz.aisdv1.core.validation.seed.SeedValidator
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageState
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageState
import com.shifthackz.aisdv1.presentation.screen.txt2img.canGenerate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.After
import org.koin.dsl.koinApplication

/**
 * Checks submission gates and provider changes using the actual generation screen models.
 *
 * @author Dmitriy Moroz
 */
class GenerationSeedValidationTest {
    private val dependencyGraph = koinApplication { modules(validatorsModule) }
    private val validator = dependencyGraph.koin.get<SeedValidator>()
    private val nativeValidator = dependencyGraph.koin.get<SeedValidator>(nativeSeedValidatorQualifier)

    @After
    fun closeDependencies() {
        dependencyGraph.close()
    }

    @Test
    fun `either unsafe web seed blocks both generation screen states`() {
        listOf(ServerSource.AUTOMATIC1111, ServerSource.SWARM_UI).forEach { mode ->
            listOf("6687881544318117707" to "", "" to "9007199254740992").forEach { (seed, subSeed) ->
                val textState = TextToImageState(mode = mode, seed = seed, subSeed = subSeed)
                    .withSeedValidation(validator, nativeValidator)
                val imageState = ImageToImageState(mode = mode, seed = seed, subSeed = subSeed)
                    .withSeedValidation(validator, nativeValidator)
                assertTrue(textState.hasValidationErrors)
                assertFalse(textState.canGenerate())
                assertTrue(imageState.hasValidationErrors)
                assertFalse(imageState.canGenerate)
            }
        }
    }

    @Test
    fun `hidden seed fields cannot block providers without seed controls`() {
        listOf(ServerSource.OPEN_AI, ServerSource.OPEN_AI_COMPATIBLE, ServerSource.OPEN_AI_OAUTH).forEach { mode ->
            val textState = TextToImageState(mode = mode, seed = "invalid", subSeed = "invalid")
                .withSeedValidation(validator, nativeValidator)
            val imageState = ImageToImageState(mode = mode, seed = "invalid", subSeed = "invalid")
                .withSeedValidation(validator, nativeValidator)
            assertNull(textState.seedValidationError)
            assertNull(textState.subSeedValidationError)
            assertFalse(textState.hasValidationErrors)
            assertNull(imageState.seedValidationError)
            assertNull(imageState.subSeedValidationError)
            assertFalse(imageState.hasValidationErrors)
        }
    }

    @Test
    fun `switching between native and web providers recomputes seed constraints immediately`() {
        val textState = TextToImageState(
            mode = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            seed = Long.MAX_VALUE.toString(),
            subSeed = "invalid hidden variation",
        ).withSeedValidation(validator, nativeValidator)
        assertFalse(textState.hasValidationErrors)
        val webTextState = textState.copy(mode = ServerSource.AUTOMATIC1111)
            .withSeedValidation(validator, nativeValidator)
        assertNotNull(webTextState.seedValidationError)
        assertNotNull(webTextState.subSeedValidationError)
        assertFalse(webTextState.copy(mode = textState.mode).withSeedValidation(validator, nativeValidator).hasValidationErrors)

        val imageState = ImageToImageState(
            mode = ServerSource.LOCAL_APPLE_CORE_ML,
            seed = Long.MAX_VALUE.toString(),
            subSeed = "invalid hidden variation",
        ).withSeedValidation(validator, nativeValidator)
        assertFalse(imageState.hasValidationErrors)
        val webImageState = imageState.copy(mode = ServerSource.SWARM_UI)
            .withSeedValidation(validator, nativeValidator)
        assertNotNull(webImageState.seedValidationError)
        assertNotNull(webImageState.subSeedValidationError)
        assertFalse(webImageState.copy(mode = imageState.mode).withSeedValidation(validator, nativeValidator).hasValidationErrors)
    }

    @Test
    fun `server randomness and exact web maximum do not block submission validation`() {
        listOf("", "-1", "0", "9007199254740991").forEach { seed ->
            val textState = TextToImageState(seed = seed, subSeed = seed)
                .withSeedValidation(validator, nativeValidator)
            assertFalse(textState.hasValidationErrors)
            assertTrue(textState.canGenerate())
            assertFalse(ImageToImageState(seed = seed, subSeed = seed).withSeedValidation(validator, nativeValidator).hasValidationErrors)
        }
    }
}
