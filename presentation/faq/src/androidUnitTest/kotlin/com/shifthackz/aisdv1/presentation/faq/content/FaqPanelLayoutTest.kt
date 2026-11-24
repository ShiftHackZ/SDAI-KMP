package com.shifthackz.aisdv1.presentation.faq.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the compact empty panel and the expanded question's visual reading order.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class FaqPanelLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var previousLanguage: String

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
    fun `given FAQ no-results panel in a short viewport, layout is measured, expected whole panel takes at most half the height`() {
        // Given
        var viewportHeight = 0
        var panelHeight = 0
        compose.setContent {
            MaterialTheme {
                Box(
                    Modifier.size(width = 320.dp, height = 240.dp)
                        .onSizeChanged { viewportHeight = it.height }) {
                    FaqNoResults(
                        isCached = false,
                        onRetry = {},
                        modifier = Modifier.fillMaxWidth()
                            .onSizeChanged { panelHeight = it.height },
                    )
                }
            }
        }

        // When
        compose.waitForIdle()

        // Then
        compose.onNodeWithText(Localization.string("faq_no_results")).assertIsDisplayed()
        assertTrue(
            "Panel height $panelHeight for viewport $viewportHeight",
            panelHeight in 1..viewportHeight / 2
        )
    }

    @Test
    fun `given FAQ search on a saved snapshot with no matches, panel scrolls to Retry, expected cached notice and refresh action remain reachable`() {
        // Given
        var retries = 0
        compose.setContent {
            MaterialTheme {
                Box(Modifier.size(width = 320.dp, height = 360.dp)) {
                    FaqNoResults(
                        isCached = true,
                        onRetry = { retries++ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        compose.onNodeWithText(Localization.string("faq_no_results")).assertIsDisplayed()

        // When
        compose.onNodeWithText(Localization.string("retry")).performScrollTo().performClick()

        // Then
        compose.onNodeWithText(Localization.string("faq_cached")).assertIsDisplayed()
        assertEquals(1, retries)
    }

    @Test
    fun `given FAQ question card with two paragraphs, question is expanded and collapsed, expected vertical order and hidden collapsed answers`() {
        // Given
        val title = "How do I connect?"
        val first = "Open Configuration."
        val second = "Enter your server address."
        val question = FaqQuestion("setup", title, listOf(first, second), emptyList())
        var isExpanded by mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                FaqQuestionCard(question, isExpanded, onToggle = { isExpanded = !isExpanded })
            }
        }
        compose.onNodeWithText(first).assertDoesNotExist()

        // When
        compose.onNodeWithText(title).performClick()

        // Then
        val headingBounds = compose.onNodeWithText(title).fetchSemanticsNode().boundsInRoot
        val firstBounds = compose.onNodeWithText(first).fetchSemanticsNode().boundsInRoot
        val secondBounds = compose.onNodeWithText(second).fetchSemanticsNode().boundsInRoot
        assertTrue(headingBounds.bottom < firstBounds.top)
        assertTrue(firstBounds.bottom < secondBounds.top)

        // When
        compose.onNodeWithText(title).performClick()

        // Then
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.onNodeWithText(first).assertDoesNotExist()
        compose.onNodeWithText(second).assertDoesNotExist()
    }
}
