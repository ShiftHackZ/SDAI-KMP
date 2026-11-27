package com.shifthackz.aisdv1.presentation.configuration.upscaler

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.UpscalerFeatureTag
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.presentation.components.model.description
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises provider discovery, setup availability and confirmation actions using real Compose controls.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class UpscalerSetupUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var previousLanguage: String
    private val model = LocalAiModel(
        "upscaler_mnn_anime",
        LocalAiModel.Type.Upscaler,
        "MNN Anime",
        "17 MB",
        emptyList(),
        downloaded = true
    )
    private val state = UpscalerSetupState(
        platform = Platform.ANDROID, source = UpscalerSource.LOCAL_MNN,
        isLoading = false, catalogStatus = LocalCatalogStatus.READY,
        selectedId = model.id, models = listOf(model),
    )

    @Before
    fun useEnglish() {
        previousLanguage = Localization.currentLanguageCode(); Localization.setLanguageCode("en")
    }

    @After
    fun restoreLanguage() {
        Localization.setLanguageCode(previousLanguage)
    }

    @Test
    fun `given upscaler provider UI, MNN is searched, expected only matching provider card`() {
        // Given
        var draft by mutableStateOf(state)
        render {
            UpscalerSetupContent(
                draft,
                {
                    if (it is UpscalerSetupIntent.Discover) draft =
                        draft.copy(discovery = it.discovery)
                })
        }
        // When
        compose.onNode(hasSetTextAction()).performTextReplacement("MNN")
        // Then
        compose.onAllNodesWithText("MNN")[0].assertIsDisplayed()
        compose.onNodeWithText("ESRGAN").assertDoesNotExist()
    }

    @Test
    fun `given upscaler provider UI, search has no matches, expected empty discovery message`() {
        // Given
        val draft = state.copy(discovery = UpscalerDiscoveryState(query = "unmatched"))
        render { UpscalerSetupContent(draft, {}) }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText(ServerSetupStrings().sourceNoResults).assertIsDisplayed()
    }

    @Test
    fun `given iOS upscaler provider UI, providers are displayed, expected only platform allowed MNN`() {
        // Given
        render {
            UpscalerSourceSelectionStep(
                state.copy(platform = Platform.IOS),
                ServerSetupStrings(),
                {})
        }
        // When
        compose.waitForIdle()
        // Then
        compose.onAllNodesWithText("MNN")[0].assertIsDisplayed()
        compose.onNodeWithText("ESRGAN").assertDoesNotExist()
    }

    @Test
    fun `given upscaler setup UI, second page is displayed, expected provider title description and dedicated model`() {
        // Given
        render { UpscalerSetupContent(state.copy(step = ServerSetupState.Step.CONFIGURE), {}) }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText("MNN").assertIsDisplayed()
        compose.onNodeWithText(UpscalerSource.LOCAL_MNN.description).assertIsDisplayed()
        compose.onNodeWithText(model.name).assertIsDisplayed()
    }

    @Test
    fun `given configured upscaler wizard, toolbar Back is pressed, expected provider step with draft preserved`() {
        // Given
        var draft by mutableStateOf(
            state.copy(
                step = ServerSetupState.Step.CONFIGURE,
                discovery = UpscalerDiscoveryState(query = "MNN"),
            )
        )
        render {
            UpscalerSetupContent(draft, { intent ->
                if (intent == UpscalerSetupIntent.Back) draft =
                    draft.copy(step = ServerSetupState.Step.SOURCE)
            })
        }
        // When
        compose.onNodeWithContentDescription(ServerSetupStrings().backContentDescription)
            .performClick()
        compose.waitForIdle()
        // Then
        assertEquals(ServerSetupState.Step.SOURCE, draft.step)
        assertEquals(UpscalerSource.LOCAL_MNN, draft.source)
        assertEquals(model.id, draft.selectedId)
        assertEquals("MNN", draft.discovery.query)
        compose.onAllNodesWithText("MNN")[0].assertIsDisplayed()
    }

    @Test
    fun `given upscaler setup UI, no downloaded selection exists, expected Setup action disabled`() {
        // Given
        render {
            UpscalerSetupContent(
                state.copy(
                    step = ServerSetupState.Step.CONFIGURE,
                    models = emptyList(),
                    selectedId = ""
                ), {})
        }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText(ServerSetupStrings().setup).assertIsNotEnabled()
    }

    @Test
    fun `given upscaler capability filters, multiple models is selected, expected independent capability filter event`() {
        // Given
        var draft by mutableStateOf(state)
        render {
            UpscalerFiltersBottomSheet(
                draft,
                ServerSetupStrings(),
                { draft = draft.copy(discovery = it) })
        }
        // When
        compose.onNodeWithText(UpscalerFeatureTag.MultipleModels.mapToUi()).performClick()
        // Then
        assertEquals(setOf(UpscalerFeatureTag.MultipleModels), draft.discovery.tags)
    }

    @Test
    fun `given upscaler deletion dialog, confirmation is pressed, expected explicit deletion intent`() {
        // Given
        val intents = mutableListOf<UpscalerSetupIntent>()
        render { UpscalerSetupContent(state.copy(pendingDeleteId = model.id), intents::add) }
        // When
        compose.onNodeWithText(ServerSetupStrings().ok).performClick()
        // Then
        assertEquals(listOf(UpscalerSetupIntent.ConfirmDelete), intents)
    }

    @Test
    fun `given upscaler setup error dialog, Retry is pressed, expected retry intent`() {
        // Given
        val intents = mutableListOf<UpscalerSetupIntent>()
        render { UpscalerSetupContent(state.copy(error = "Download failed"), intents::add) }
        // When
        compose.onNodeWithText(Localization.string("upscaler_retry")).performClick()
        // Then
        assertEquals(listOf(UpscalerSetupIntent.Retry), intents)
    }

    /**
     * Composes the actual wizard or sheet in an Android activity with the normal Material theme.
     *
     * @param content Production configuration surface with controlled state and captured events.
     * @author Dmitriy Moroz
     */
    private fun render(content: @Composable () -> Unit) {
        compose.setContent { MaterialTheme(content = content) }
    }
}
