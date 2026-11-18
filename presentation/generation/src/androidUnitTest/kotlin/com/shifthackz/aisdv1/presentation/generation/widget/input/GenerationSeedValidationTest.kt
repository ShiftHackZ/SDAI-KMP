package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.core.validation.di.nativeSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.easyDiffusionSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.validatorsModule
import com.shifthackz.aisdv1.core.validation.seed.SeedValidator
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageState
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState
import com.shifthackz.aisdv1.presentation.generation.txt2img.canGenerate
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
    private val easyDiffusionValidator =
        dependencyGraph.koin.get<SeedValidator>(easyDiffusionSeedValidatorQualifier)
    private val nativeValidator =
        dependencyGraph.koin.get<SeedValidator>(nativeSeedValidatorQualifier)

    @After
    fun closeDependencies() {
        dependencyGraph.close()
    }

    @Test
    fun `given generation seed validation, Easy Diffusion seeds are validated, expected both forms enforce uint32 limits and ignore hidden variation`() {
        // Given
        // The existing validator, nativeValidator and easyDiffusionValidator fixtures define the provider
        // limits; both loops retain their existing seed values.

        listOf("", "-1", "0", "4294967295").forEach { seed ->
            // When
            val text = TextToImageState(
                mode = ServerSource.EASY_DIFFUSION,
                prompt = "lake",
                seed = seed,
                subSeed = "invalid hidden"
            )
                .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
            val image = ImageToImageState(
                mode = ServerSource.EASY_DIFFUSION,
                prompt = "lake",
                imageBase64 = "input",
                seed = seed,
                subSeed = "invalid hidden"
            )
                .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
            // Then
            assertNull(text.seedValidationError)
            assertNull(image.seedValidationError)
            assertNull(text.subSeedValidationError)
            assertNull(image.subSeedValidationError)
            assertTrue(text.canGenerate())
            assertTrue(image.canGenerate)
        }
        listOf("4294967296", "-2", "1.5", "bad").forEach { seed ->
            // When
            val text =
                TextToImageState(mode = ServerSource.EASY_DIFFUSION, prompt = "lake", seed = seed)
                    .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
            val image = ImageToImageState(
                mode = ServerSource.EASY_DIFFUSION,
                prompt = "lake",
                imageBase64 = "input",
                seed = seed
            )
                .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
            // Then
            assertNotNull(text.seedValidationError)
            assertNotNull(image.seedValidationError)
            assertFalse(text.canGenerate())
            assertFalse(image.canGenerate)
        }

    }

    @Test
    fun `given generation seed validation, either web seed exceeds the supported range, expected both generation forms block submission`() {
        // Given
        // Existing validators and nested mode/seed lists define both web providers and the invalid
        // primary/variation pairs.

        listOf(ServerSource.AUTOMATIC1111, ServerSource.SWARM_UI).forEach { mode ->
            listOf(
                "6687881544318117707" to "",
                "" to "9007199254740992"
            ).forEach { (seed, subSeed) ->
                // When
                val textState = TextToImageState(mode = mode, seed = seed, subSeed = subSeed)
                    .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
                val imageState = ImageToImageState(mode = mode, seed = seed, subSeed = subSeed)
                    .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
                // Then
                assertTrue(textState.hasValidationErrors)
                assertFalse(textState.canGenerate())
                assertTrue(imageState.hasValidationErrors)
                assertFalse(imageState.canGenerate)
            }
        }

    }

    @Test
    fun `given generation seed validation, providers hide stored invalid seed controls, expected both forms have no seed validation errors`() {
        // Given
        // Existing validators and provider list define OPEN_AI, OPEN_AI_COMPATIBLE and OPEN_AI_OAUTH with
        // retained invalid seeds.

        listOf(
            ServerSource.OPEN_AI,
            ServerSource.OPEN_AI_COMPATIBLE,
            ServerSource.OPEN_AI_OAUTH
        ).forEach { mode ->
            // When
            val textState = TextToImageState(mode = mode, seed = "invalid", subSeed = "invalid")
                .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
            val imageState = ImageToImageState(mode = mode, seed = "invalid", subSeed = "invalid")
                .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
            // Then
            assertNull(textState.seedValidationError)
            assertNull(textState.subSeedValidationError)
            assertFalse(textState.hasValidationErrors)
            assertNull(imageState.seedValidationError)
            assertNull(imageState.subSeedValidationError)
            assertFalse(imageState.hasValidationErrors)
        }

    }

    @Test
    fun `given generation seed validation, the provider switches between native and web, expected seed validation uses the current provider constraints`() {
        // Given
        // Existing validators and explicit Long.MAX_VALUE seed values define native and web constraints.

        // When
        val textState = TextToImageState(
            mode = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            seed = Long.MAX_VALUE.toString(),
            subSeed = "invalid hidden variation",
        ).withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
        // Then
        assertFalse(textState.hasValidationErrors)
        // When
        val webTextState = textState.copy(mode = ServerSource.AUTOMATIC1111)
            .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
        // Then
        assertNotNull(webTextState.seedValidationError)
        assertNotNull(webTextState.subSeedValidationError)
        // When
        val hasRestoredTextValidationErrors =
            webTextState.copy(mode = textState.mode).withSeedValidation(
                validator,
                nativeValidator,
                easyDiffusionValidator
            ).hasValidationErrors
        // Then
        assertFalse(hasRestoredTextValidationErrors)

        // When
        val imageState = ImageToImageState(
            mode = ServerSource.LOCAL_APPLE_CORE_ML,
            seed = Long.MAX_VALUE.toString(),
            subSeed = "invalid hidden variation",
        ).withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
        // Then
        assertFalse(imageState.hasValidationErrors)
        // When
        val webImageState = imageState.copy(mode = ServerSource.SWARM_UI)
            .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
        // Then
        assertNotNull(webImageState.seedValidationError)
        assertNotNull(webImageState.subSeedValidationError)
        // When
        val hasRestoredImageValidationErrors =
            webImageState.copy(mode = imageState.mode).withSeedValidation(
                validator,
                nativeValidator,
                easyDiffusionValidator
            ).hasValidationErrors
        // Then
        assertFalse(hasRestoredImageValidationErrors)

    }

    @Test
    fun `given generation seed validation, server randomness or the maximum web seed is selected, expected submission validation accepts both values`() {
        // Given
        // Existing validators and loop values cover empty/server-randomness, zero and web maximum seeds.

        listOf("", "-1", "0", "9007199254740991").forEach { seed ->
            // When
            val textState = TextToImageState(seed = seed, subSeed = seed)
                .withSeedValidation(validator, nativeValidator, easyDiffusionValidator)
            // Then
            assertFalse(textState.hasValidationErrors)
            assertTrue(textState.canGenerate())
            // When
            val hasImageValidationErrors =
                ImageToImageState(seed = seed, subSeed = seed).withSeedValidation(
                    validator,
                    nativeValidator,
                    easyDiffusionValidator
                ).hasValidationErrors
            // Then
            assertFalse(hasImageValidationErrors)
        }

    }
}
