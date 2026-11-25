package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Checks whole-card activation and reveal timing in the production pipeline form inside a lazy scroller.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class UpscalerPipelineInteractionTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var previousLanguage: String
    private lateinit var listState: LazyListState
    private var configuration by mutableStateOf<UpscalerConfiguration?>(null)
    private val model = LocalAiModel(
        "upscaler_esrgan_anime", LocalAiModel.Type.Upscaler,
        "ESRGAN Anime", "17 MB", emptyList(), downloaded = true,
    )
    private val form = UpscalerFormState(
        true, Platform.ANDROID, model, listOf(model), isLoading = false,
    )
    private val toggleLabel get() = Localization.string("upscaler_pipeline_toggle")

    @Before
    fun useEnglish() {
        previousLanguage = Localization.currentLanguageCode()
        Localization.setLanguageCode("en")
    }

    @After
    fun restoreLanguage() {
        Localization.setLanguageCode(previousLanguage)
    }

    @Test
    fun `given pipeline enable card, label is tapped, expected exactly one toggle request`() {
        // Given
        val changes = mutableListOf<Boolean>()
        compose.setContent {
            MaterialTheme { UpscalerEnableCard(false, true, changes::add) }
        }
        // When
        compose.onNodeWithText(toggleLabel).performClick()
        // Then
        assertEquals(listOf(true), changes)
    }

    @Test
    fun `given pipeline enable card, switch area is tapped, expected exactly one toggle request`() {
        // Given
        val changes = mutableListOf<Boolean>()
        compose.setContent {
            MaterialTheme { UpscalerEnableCard(false, true, changes::add) }
        }
        // When
        compose.onNode(isToggleable()).performTouchInput {
            click(Offset(width - 30f, centerY))
        }
        // Then
        assertEquals(listOf(true), changes)
        assertEquals(1, compose.onAllNodes(isToggleable()).fetchSemanticsNodes().size)
    }

    @Test
    fun `given disabled pipeline card, label is tapped, expected unchanged selection`() {
        // Given
        val changes = mutableListOf<Boolean>()
        compose.setContent {
            MaterialTheme { UpscalerEnableCard(false, false, changes::add) }
        }
        // When
        compose.onNodeWithText(toggleLabel).performTouchInput { click() }
        // Then
        compose.onNode(isToggleable()).assertIsNotEnabled().assertIsOff()
        assertTrue(changes.isEmpty())
    }

    @Test
    fun `given scrolling pipeline form, card is enabled, expected scroll during expansion and final bottom alignment`() {
        // Given
        showScrollingForm()
        compose.onNodeWithTag("pipeline-list").performScrollToNode(hasText(toggleLabel))
        compose.mainClock.autoAdvance = false
        val initialPosition = -listState.layoutInfo.visibleItemsInfo.last().offset
        // When
        compose.onNodeWithText(toggleLabel).performClick()
        compose.mainClock.advanceTimeBy(160)
        compose.waitForIdle()
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        // Then
        val midwayPosition = -listState.layoutInfo.visibleItemsInfo.last().offset
        assertTrue(
            "Scroll must start while the controls are expanding",
            midwayPosition > initialPosition
        )
        compose.mainClock.advanceTimeBy(2000)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithText("×4").assertIsDisplayed()
        val finalPosition = -listState.layoutInfo.visibleItemsInfo.last().offset
        assertTrue(
            "Expansion must continue beyond the intermediate position",
            finalPosition > midwayPosition
        )
        assertFalse(listState.canScrollForward)
        assertEquals(model.id, configuration?.modelId)
    }

    @Test
    fun `given restored enabled pipeline, form is first displayed, expected retained scroll position`() {
        // Given
        configuration = UpscalerConfiguration(model.id)
        // When
        showScrollingForm()
        compose.waitForIdle()
        // Then
        assertEquals(0, listState.firstVisibleItemIndex)
        assertEquals(0, listState.firstVisibleItemScrollOffset)
        assertTrue(listState.canScrollForward)
    }

    @Test
    fun `given expanding pipeline form, captured stage is cleared, expected cancelled scroll`() {
        // Given
        showScrollingForm()
        compose.onNodeWithTag("pipeline-list").performScrollToNode(hasText(toggleLabel))
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText(toggleLabel).performClick()
        compose.mainClock.advanceTimeByFrame()
        // When
        compose.runOnIdle { configuration = null }
        compose.mainClock.advanceTimeBy(2000)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        // Then
        compose.onNode(isToggleable()).assertIsOff()
        compose.onNodeWithText(model.name).assertDoesNotExist()
        assertEquals(null, configuration)
        assertFalse(listState.isScrollInProgress)
    }

    /**
     * Places the real optional form after preceding generation content in a constrained viewport.
     *
     * @author Dmitriy Moroz
     */
    private fun showScrollingForm() {
        compose.setContent {
            listState = rememberLazyListState()
            MaterialTheme {
                LazyColumn(
                    modifier = Modifier.height(240.dp).testTag("pipeline-list"),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    item { Box(Modifier.fillMaxWidth().height(400.dp)) }
                    item {
                        UpscalerForm(
                            form, configuration, { configuration = it }, {},
                            onRevealControls = { isExpanding ->
                                listState.revealUpscalerControls(
                                    isExpanding
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
