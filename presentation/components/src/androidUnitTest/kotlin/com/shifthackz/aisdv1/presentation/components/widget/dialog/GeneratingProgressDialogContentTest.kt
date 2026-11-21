package com.shifthackz.aisdv1.presentation.components.widget.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Verifies the foreground dialog's actual indicator and text use the reported unit and queue precedence. @author Dmitriy Moroz */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class GeneratingProgressDialogContentTest {
    @get:Rule
    val compose = createComposeRule()

    /**
     * Renders production dialog content with provider counters rather than a copied progress formula.
     * @param step Sampling counters, or null while no counters are available.
     * @param overall Overall percentage reported by a provider.
     * @param queue Current queue position, taking precedence over generation measurements.
     * @author Dmitriy Moroz
     */
    private fun render(step: Pair<Int, Int>? = null, overall: Int? = null, queue: Int? = null) {
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                GeneratingProgressDialogContent(
                    title = "Generation".asUiText(), subTitle = "Working".asUiText(),
                    step = step, overallPercentage = overall, positionInQueue = queue,
                )
            }
        }
    }

    /** Returns the indicator's production accessibility contract. @author Dmitriy Moroz */
    private fun progress() = compose.onNode(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
    ).fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]

    @Test
    fun `given sampling dialog, valid step counts arrive, expected measured bar and original steps with percentage`() {
        // Given
        render(step = 8 to 20)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo(0.4f, 0f..1f), actual)
        compose.onNodeWithText(Localization.string("communicating_status_steps", 8, 20) + " · 40%")
            .assertIsDisplayed()
    }

    @Test
    fun `given overall dialog, percentage arrives, expected measured overall bar and percent text`() {
        // Given
        render(overall = 65)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo(0.65f, 0f..1f), actual)
        compose.onNodeWithText("65%").assertIsDisplayed()
    }

    @Test
    fun `given queued dialog, generation measurements also exist, expected indeterminate bar and queue status`() {
        // Given
        render(step = 8 to 20, overall = 65, queue = 4)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo.Indeterminate, actual)
        compose.onNodeWithText(Localization.string("communicating_status_queue", 4))
            .assertIsDisplayed()
    }

    @Test
    fun `given generation dialog, no counters exist, expected indeterminate bar`() {
        // Given
        render()
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo.Indeterminate, actual)

    }

    @Test
    fun `given sampling dialog, total is zero, expected indeterminate bar`() {
        // Given
        render(step = 1 to 0)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo.Indeterminate, actual)

    }

    @Test
    fun `given sampling dialog, count is negative, expected zero progress`() {
        // Given
        render(step = -4 to 20)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo(0f, 0f..1f), actual)

    }

    @Test
    fun `given sampling dialog, count exceeds total, expected full bounded bar`() {
        // Given
        render(step = 24 to 20)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo(1f, 0f..1f), actual)

    }

    @Test
    fun `given overall dialog, percentage is negative, expected zero progress`() {
        // Given
        render(overall = -5)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo(0f, 0f..1f), actual)
        compose.onNodeWithText("0%").assertIsDisplayed()
    }

    @Test
    fun `given overall dialog, percentage exceeds one hundred, expected full bounded bar`() {
        // Given
        render(overall = 150)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo(1f, 0f..1f), actual)
        compose.onNodeWithText("100%").assertIsDisplayed()
    }

    @Test
    fun `given overall dialog, sampling counts also exist, expected bar and text use overall measurement`() {
        // Given
        render(step = 2 to 20, overall = 75)
        // When
        val actual = progress()
        // Then
        assertEquals(ProgressBarRangeInfo(0.75f, 0f..1f), actual)
        compose.onNodeWithText("75%").assertIsDisplayed()
    }

}
