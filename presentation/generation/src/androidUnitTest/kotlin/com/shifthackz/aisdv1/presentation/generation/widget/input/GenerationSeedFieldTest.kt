package com.shifthackz.aisdv1.presentation.generation.widget.input

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.validation.di.nativeSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.easyDiffusionSeedValidatorQualifier
import com.shifthackz.aisdv1.core.validation.di.validatorsModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.koinApplication
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.annotation.Config


/**
 * Repeats seed-field interactions for both generation screens and both seed controls.
 * Uses the production composable, screen state, event processors and generation payloads.
 *
 * @param isImg2Img whether the form belongs to image-to-image generation.
 * @param isVariationSeed whether the variation-seed control is under test.
 * @author Dmitriy Moroz
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w480dp-h1000dp")
class GenerationSeedFieldTest(
    private val isImg2Img: Boolean,
    private val isVariationSeed: Boolean,
) {
    @get:Rule
    val compose = createComposeRule()

    private val dependencyGraph = koinApplication { modules(validatorsModule) }
    private lateinit var fixture: GenerationSeedFormFixture
    private lateinit var previousLanguage: String

    @Before
    fun renderForm() {
        previousLanguage = Localization.currentLanguageCode()
        Localization.setLanguageCode("en")
        fixture = GenerationSeedFormFixture(
            isImg2Img,
            dependencyGraph.koin.get(),
            dependencyGraph.koin.get(nativeSeedValidatorQualifier),
            dependencyGraph.koin.get(easyDiffusionSeedValidatorQualifier),
        )
        compose.setContent {
            MaterialTheme {
                LazyColumn {
                    generationInputAdvancedItems(
                        state = fixture.state.toAdvancedOptionsState(),
                        isImg2Img = isImg2Img,
                        onEvent = fixture::process,
                        afterSlidersSection = {},
                    )
                }
            }
        }
    }

    @After
    fun restoreLanguage() {
        Localization.setLanguageCode(previousLanguage)
        dependencyGraph.close()
    }

    @Test
    fun `given generation seed field, the seed dice is clicked repeatedly, expected web compatible seeds replace only the selected field`() {
        // Given
        // renderForm() renders the parameterized production form and selects fieldIndex for seed or
        // variation seed.

        repeat(3) {
            // When
            compose.onAllNodesWithContentDescription(
                Localization.string("action_image_picker_random"),
            )[fieldIndex].performClick()

            // Then
            compose.runOnIdle {
                val value = currentSeed()
                val seed = value.toLongOrNull()
                assertNotNull("Dice must emit an integer: $value", seed)
                assertTrue(
                    "Seed must match A1111 random range: $value",
                    seed!! in 0L..4_294_967_293L
                )
                assertEquals(seed, value.toDouble().toLong())
                assertPayloadAndOtherSeed(value)
                assertFalse(fixture.hasValidationErrors)
            }
            seedField().assertTextContains(currentSeed())
        }

    }

    @Test
    fun `given generation seed field, a seed field is cleared, expected only that field uses server selected randomness`() {
        // Given

        seedField().performTextReplacement("6687881544318117707")
        compose.runOnIdle { assertTrue(fixture.hasValidationErrors) }
        // When
        compose.onAllNodesWithContentDescription(Localization.string("action_clear"))[fieldIndex]
            .performClick()

        // Then
        compose.runOnIdle {
            assertEquals("", currentSeed())
            assertPayloadAndOtherSeed("")
            assertFalse(fixture.hasValidationErrors)
        }

    }

    @Test
    fun `given generation seed field, valid boundary and server randomness seeds are entered, expected form state and payload retain exact values`() {
        // Given
        // renderForm() supplies the parameterized form; the loop lists valid zero, random-range maximum,
        // web maximum and server-randomness values.

        listOf("0", "4294967293", "9007199254740991", "-1").forEach { value ->
            // When
            seedField().performTextReplacement(value)
            // Then
            seedField().assertTextContains(value)
            compose.runOnIdle {
                assertPayloadAndOtherSeed(value)
                assertFalse(fixture.hasValidationErrors)
            }
        }

    }

    @Test
    fun `given generation seed field, an oversized seed is entered, expected its value remains visible and generation is blocked`() {
        // Given

        val value = "6687881544318117707"
        // When
        seedField().performTextReplacement(value)
        // Then
        seedField().assertTextContains(value)
        compose.runOnIdle {
            assertPayloadAndOtherSeed(value)
            assertTrue(fixture.hasValidationErrors)
        }

    }

    @Test
    fun `given generation seed field, the dice replaces an invalid manual seed, expected a valid seed appears and its error clears`() {
        // Given

        seedField().performTextReplacement("9007199254740992")
        compose.runOnIdle { assertTrue(fixture.hasValidationErrors) }
        // When
        compose.onAllNodesWithContentDescription(
            Localization.string("action_image_picker_random"),
        )[fieldIndex].performClick()
        // Then
        compose.runOnIdle {
            assertTrue(currentSeed().toLong() in 0L..4_294_967_293L)
            assertFalse(fixture.hasValidationErrors)
            assertPayloadAndOtherSeed(currentSeed())
        }

    }

    @Test
    fun `given generation seed field, malformed seed text is pasted, expected text is retained and generation is blocked`() {
        // Given
        // renderForm() supplies the parameterized production form; the loop enumerates malformed decimal
        // inputs.

        listOf("12x", "1.5", "-2").forEach { value ->
            // When
            seedField().performTextReplacement(value)
            // Then
            seedField().assertTextContains(value)
            compose.runOnIdle {
                assertPayloadAndOtherSeed(value)
                assertTrue(fixture.hasValidationErrors)
            }
        }

    }

    private val fieldIndex: Int
        get() = if (isVariationSeed) 1 else 0

    private fun currentSeed(): String =
        if (isVariationSeed) fixture.state.subSeed else fixture.state.seed

    private fun seedField(): SemanticsNodeInteraction = compose.onNode(
        hasSetTextAction() and hasText(Localization.string(if (isVariationSeed) "hint_sub_seed" else "hint_seed")),
    )

    /**
     * Verifies state and payload preserve the edited seed and leave its sibling untouched.
     *
     * @param value exact decimal string or empty value emitted by the selected seed control.
     * @author Dmitriy Moroz
     */
    private fun assertPayloadAndOtherSeed(value: String) {
        assertEquals(value, currentSeed())
        val expected = if (isVariationSeed) "123" to value else value to "456"
        assertEquals(expected, fixture.state.seed to fixture.state.subSeed)
        assertEquals(expected, fixture.payloadSeeds())
    }

    companion object {
        @JvmStatic
        @Parameters(name = "img2img={0}, variation={1}")
        fun screensAndFields(): List<Array<Any>> = listOf(
            arrayOf(false, false),
            arrayOf(false, true),
            arrayOf(true, false),
            arrayOf(true, true),
        )
    }
}
