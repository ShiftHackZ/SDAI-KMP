package com.shifthackz.aisdv1.presentation.components.widget.work

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Renders the production card to verify text alignment, available width and measured progress semantics.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class BackgroundGenerationProgressContentTest {
    @get:Rule
    val compose = createComposeRule()

    /**
     * Composes the production progress surface at a fixed phone-card width.
     *
     * @param status Provider text including absent and whitespace-only cases.
     * @param percentage Measured percentage used by both text and accessibility.
     * @author Dmitriy Moroz
     */
    private fun render(status: String?, percentage: Int = 40) {
        compose.setContent {
            MaterialTheme {
                BackgroundGenerationProgressContent(
                    status,
                    percentage,
                    Modifier.width(300.dp).testTag("card")
                )
            }
        }
    }

    @Test
    fun `given background progress card, stage text exists, expected status left and percentage right above a full-width bar`() {
        // Given
        render("Denoising")
        // When
        val status = compose.onNodeWithText("Denoising").fetchSemanticsNode().boundsInRoot
        val percentage = compose.onNodeWithText("40%").fetchSemanticsNode().boundsInRoot
        val card = compose.onNodeWithTag("card").fetchSemanticsNode().boundsInRoot
        val bar =
            compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
                .fetchSemanticsNode().boundsInRoot
        // Then
        assertEquals(card.left, status.left, 0.5f)
        assertEquals(card.right, percentage.right, 0.5f)
        assertEquals(status.top, percentage.top, 0.5f)
        assertEquals(card.width, bar.width, 0.5f)
        assertTrue(bar.center.y > percentage.bottom)
    }

    @Test
    fun `given background progress card, status is absent, expected percentage starts on the left`() {
        // Given
        render(null)
        // When
        val percentage = compose.onNodeWithText("40%").fetchSemanticsNode().boundsInRoot
        val card = compose.onNodeWithTag("card").fetchSemanticsNode().boundsInRoot
        // Then
        assertEquals(card.left, percentage.left, 0.5f)
    }

    @Test
    fun `given background progress card, status is empty, expected percentage starts on the left`() {
        // Given
        render("")
        // When
        val percentage = compose.onNodeWithText("40%").fetchSemanticsNode().boundsInRoot
        val card = compose.onNodeWithTag("card").fetchSemanticsNode().boundsInRoot
        // Then
        assertEquals(card.left, percentage.left, 0.5f)
    }

    @Test
    fun `given background progress card, status is whitespace, expected percentage starts on the left`() {
        // Given
        render(" \n ")
        // When
        val percentage = compose.onNodeWithText("40%").fetchSemanticsNode().boundsInRoot
        val card = compose.onNodeWithTag("card").fetchSemanticsNode().boundsInRoot
        // Then
        assertEquals(card.left, percentage.left, 0.5f)
    }

    @Test
    fun `given background progress card, stage text is long, expected percentage remains visible within the card`() {
        // Given
        render("Loading and preparing a very long provider-specific generation stage description")
        // When
        val percentage = compose.onNodeWithText("40%")
        val card = compose.onNodeWithTag("card").fetchSemanticsNode().boundsInRoot
        // Then
        percentage.assertIsDisplayed()
        assertEquals(card.right, percentage.fetchSemanticsNode().boundsInRoot.right, 0.5f)
    }

    @Test
    fun `given background progress card, negative measurement is rendered, expected bounded text and accessibility fraction`() {
        // Given
        render(null, -5)
        // When
        val percentage = compose.onNodeWithText("0%")
        val bar =
            compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
                .fetchSemanticsNode()
        // Then
        percentage.assertIsDisplayed()
        assertEquals(
            ProgressBarRangeInfo(0.0f, 0f..1f),
            bar.config[SemanticsProperties.ProgressBarRangeInfo]
        )
    }

    @Test
    fun `given background progress card, overshoot measurement is rendered, expected bounded text and accessibility fraction`() {
        // Given
        render(null, 150)
        // When
        val percentage = compose.onNodeWithText("100%")
        val bar =
            compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
                .fetchSemanticsNode()
        // Then
        percentage.assertIsDisplayed()
        assertEquals(
            ProgressBarRangeInfo(1.0f, 0f..1f),
            bar.config[SemanticsProperties.ProgressBarRangeInfo]
        )
    }

}
