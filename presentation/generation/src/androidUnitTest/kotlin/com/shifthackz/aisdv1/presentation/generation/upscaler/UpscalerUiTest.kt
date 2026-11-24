package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises production upscaler controls through Compose semantics and real input actions.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class UpscalerUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var previousLanguage: String
    private val model = LocalAiModel(
        "upscaler_esrgan_anime",
        LocalAiModel.Type.Upscaler,
        "ESRGAN Anime",
        "17 MB",
        emptyList(),
        downloaded = true
    )
    private val selection = UpscalerConfiguration(model.id)
    private val form =
        UpscalerFormState(true, Platform.ANDROID, model, listOf(model), isLoading = false)

    @Before
    fun useEnglish() {
        previousLanguage = Localization.currentLanguageCode(); Localization.setLanguageCode("en")
    }

    @After
    fun restoreLanguage() {
        Localization.setLanguageCode(previousLanguage)
    }

    @Test
    fun `given upscaler dimensions UI, portrait output is shown, expected exact width and height without editable fields`() {
        // Given
        compose.setContent {
            MaterialTheme {
                UpscalerOutputSizeFields(
                    UpscalerImageSize(
                        3072,
                        4096
                    )
                )
            }
        }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText("3072").assertIsDisplayed()
        compose.onNodeWithText("4096").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
    }

    @Test
    fun `given upscaler dimensions UI, input is absent, expected dimension placeholders`() {
        // Given
        compose.setContent { MaterialTheme { UpscalerOutputSizeFields(null) } }
        // When
        compose.waitForIdle()
        // Then
        assertEquals(2, compose.onAllNodes(hasText("—")).fetchSemanticsNodes().size)
    }

    @Test
    fun `given upscaler parameter UI, disclosure is collapsed then expanded, expected fields removed then restored`() {
        // Given
        compose.setContent {
            MaterialTheme {
                UpscalerParametersForm(
                    selection,
                    listOf(model),
                    {},
                    isStandalone = true
                )
            }
        }
        compose.onNodeWithText(model.name).assertIsDisplayed()
        // When
        compose.onNodeWithText(Localization.string("upscaler_options")).performClick()
        compose.waitForIdle()
        // Then
        compose.onNodeWithText(model.name).assertDoesNotExist()
        compose.onNodeWithText(Localization.string("upscaler_options")).performClick()
        compose.onNodeWithText(model.name).assertIsDisplayed()
    }

    @Test
    fun `given upscaler parameter UI, x2 is selected, expected same checkpoint with new captured scale`() {
        // Given
        var configuration by mutableStateOf<UpscalerConfiguration?>(selection)
        compose.setContent {
            MaterialTheme {
                UpscalerParametersForm(
                    configuration,
                    listOf(model),
                    { configuration = it })
            }
        }
        // When
        compose.onNodeWithText("×4").performClick()
        compose.onNodeWithText("×2").performClick()
        // Then
        assertEquals(selection.copy(scaleFactor = 2), configuration)
        compose.onNodeWithText("×2").assertIsDisplayed()
    }

    @Test
    fun `given upscaler parameter UI, operation disables controls, expected model and scale fields disabled`() {
        // Given
        compose.setContent {
            MaterialTheme {
                UpscalerParametersForm(
                    selection,
                    listOf(model),
                    {},
                    isEnabled = false
                )
            }
        }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText(model.name).assertIsNotEnabled()
        compose.onNodeWithText("×4").assertIsNotEnabled()
    }

    @Test
    fun `given upscaler parameter UI, no model is downloaded, expected setup guidance and disabled model field`() {
        // Given
        compose.setContent {
            MaterialTheme {
                UpscalerParametersForm(
                    null,
                    listOf(model.copy(downloaded = false)),
                    {})
            }
        }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText(Localization.string("upscaler_setup_required")).assertIsDisplayed()
        compose.onNodeWithText(model.name).assertDoesNotExist()
    }

    @Test
    fun `given optional upscaler form, pipeline is toggled on then off, expected model controls follow captured selection`() {
        // Given
        var configuration by mutableStateOf<UpscalerConfiguration?>(null)
        compose.setContent {
            MaterialTheme {
                UpscalerForm(
                    form,
                    configuration,
                    { configuration = it },
                    {})
            }
        }
        compose.onNodeWithText(model.name).assertDoesNotExist()
        // When
        compose.onNode(isToggleable()).performClick()
        // Then
        compose.onNodeWithText(model.name).assertIsDisplayed()
        assertEquals(model.id, configuration?.modelId)
        compose.onNode(isToggleable()).performClick()
        compose.onNodeWithText(model.name).assertDoesNotExist()
        assertNull(configuration)
    }

    @Test
    fun `given standalone upscaler screen, no input image is selected, expected disabled Upscale action`() {
        // Given
        val state = UpscalerState(Platform.ANDROID, true, configuration = selection)
        compose.setContent {
            MaterialTheme {
                UpscalerScreenContent(
                    state,
                    form,
                    {},
                    inputImageContent = {})
            }
        }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText(Localization.string("upscaler_action")).assertIsNotEnabled()
    }

    @Test
    fun `given standalone downscale warning, Continue is pressed, expected confirmation intent without replacing source`() {
        // Given
        val intents = mutableListOf<UpscalerIntent>()
        val state = UpscalerState(
            Platform.ANDROID,
            true,
            image = "original",
            width = 2048,
            height = 1536,
            configuration = selection,
            hasDownscaleWarning = true
        )
        compose.setContent {
            MaterialTheme {
                UpscalerScreenContent(
                    state,
                    form,
                    intents::add,
                    inputImageContent = {})
            }
        }
        compose.runOnIdle { intents.clear() }
        // When
        compose.onNodeWithText(Localization.string("upscaler_continue")).performClick()
        // Then
        assertEquals(listOf(UpscalerIntent.ConfirmDownscale), intents)
        assertEquals("original", state.image)
    }

    @Test
    fun `given standalone downscale warning, Cancel is pressed, expected dismissal intent`() {
        // Given
        val intents = mutableListOf<UpscalerIntent>()
        val state = UpscalerState(Platform.ANDROID, true, hasDownscaleWarning = true)
        compose.setContent {
            MaterialTheme {
                UpscalerScreenContent(
                    state,
                    form,
                    intents::add,
                    inputImageContent = {})
            }
        }
        compose.runOnIdle { intents.clear() }
        // When
        compose.onNodeWithText(Localization.string("cancel")).performClick()
        // Then
        assertEquals(listOf(UpscalerIntent.DismissModal), intents)
    }
}
