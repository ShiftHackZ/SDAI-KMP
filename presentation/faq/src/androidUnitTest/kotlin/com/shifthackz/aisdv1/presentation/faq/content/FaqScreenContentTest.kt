package com.shifthackz.aisdv1.presentation.faq.content

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqContent
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import com.shifthackz.aisdv1.presentation.faq.FaqState
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
 * Checks the production FAQ renderer with prepared state and real Compose input actions.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class FaqScreenContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val basics =
        FaqQuestion("basics", "What is SDAI?", listOf("An image generation client."), emptyList())
    private val local = FaqQuestion(
        "speed",
        "How can I speed up generation?",
        listOf("Try a smaller model.", "Quantization can help."),
        listOf("nnapi"),
    )
    private val content = FaqContent(
        categories = listOf(
            FaqCategory("basics", "Basics", listOf(basics)),
            FaqCategory("local", "Local Generation", listOf(local)),
        ),
        isCached = false,
    )
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
    fun `given FAQ screen with initial load, screen is composed, expected accessible loading state without search controls`() {
        // Given
        renderState(FaqState.Loading)

        // When
        compose.waitForIdle()

        // Then
        compose.onNodeWithContentDescription(Localization.string("loading")).assertIsDisplayed()
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.onNodeWithText(Localization.string("retry")).assertDoesNotExist()
    }

    @Test
    fun `given FAQ screen after uncached failure, Retry is clicked, expected retry event without displaying search controls`() {
        // Given
        var retries = 0
        renderState(FaqState.Error, onRetry = { retries++ })

        // When
        compose.onNodeWithText(Localization.string("retry")).performClick()

        // Then
        compose.onNodeWithText(Localization.string("faq_load_error")).assertIsDisplayed()
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        assertEquals(1, retries)
    }

    @Test
    fun `given FAQ screen showing saved answers, cached Retry is clicked, expected saved notice and questions stay accessible`() {
        // Given
        var retries = 0
        renderState(FaqState.Ready(content.copy(isCached = true)), onRetry = { retries++ })

        // When
        compose.onNodeWithText(Localization.string("retry")).performClick()

        // Then
        compose.onNodeWithText(Localization.string("faq_cached")).assertIsDisplayed()
        compose.onNodeWithText(basics.question).assertIsDisplayed()
        assertEquals(1, retries)
    }

    @Test
    fun `given FAQ screen with published answers, search edits and clear are sent, expected tag and answer matches then empty and restored results`() {
        // Given
        var query by mutableStateOf("")
        compose.setContent {
            MaterialTheme {
                FaqScreenContent(
                    state = FaqState.Ready(content),
                    query = query,
                    categoryId = null,
                    onQueryChange = { query = it },
                    onCategoryChange = {},
                    onRetry = {},
                    onBack = {},
                )
            }
        }

        // When
        val search = compose.onNode(hasSetTextAction())
        search.performTextReplacement("  QUANTIZATION nnapi  ")

        // Then
        compose.onNodeWithText(local.question).assertIsDisplayed()
        compose.onNodeWithText(basics.question).assertDoesNotExist()

        // When
        search.performTextReplacement("unmatched phrase")

        // Then
        compose.onNodeWithText(Localization.string("faq_no_results")).assertIsDisplayed()
        compose.onNodeWithText(local.question).assertDoesNotExist()

        // When
        compose.onNodeWithContentDescription(Localization.string("action_clear")).performClick()

        // Then
        compose.onNodeWithText(basics.question).assertIsDisplayed()
        compose.onNodeWithText(local.question).assertIsDisplayed()
        assertTrue(query.isEmpty())
    }

    @Test
    fun `given FAQ screen with search results, category is selected and reset, expected sheet applies filter and clears selection`() {
        // Given
        var categoryId by mutableStateOf<String?>(null)
        val selected = mutableListOf<String?>()
        compose.setContent {
            MaterialTheme {
                FaqScreenContent(
                    state = FaqState.Ready(content),
                    query = "",
                    categoryId = categoryId,
                    onQueryChange = {},
                    onCategoryChange = {
                        categoryId = it
                        selected += it
                    },
                    onRetry = {},
                    onBack = {},
                )
            }
        }
        compose.onNodeWithContentDescription(Localization.string("provider_action_filters"))
            .performClick()
        val reset = compose.onNodeWithText(Localization.string("action_reset"))
        reset.assertIsNotEnabled()

        // When
        compose.onNode(hasText("Local Generation") and hasClickAction()).performClick()
        reset.performClick()

        // Then
        assertEquals(listOf("local", null), selected)
        reset.assertIsNotEnabled()
    }

    @Test
    fun `given FAQ screen with a retired category selection, refreshed categories are rendered, expected current questions remain visible`() {
        // Given
        renderState(FaqState.Ready(content), categoryId = "removed")

        // When
        compose.waitForIdle()

        // Then
        compose.onNodeWithText(basics.question).assertIsDisplayed()
        compose.onNodeWithText(local.question).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("faq_no_results")).assertDoesNotExist()
    }

    private fun renderState(state: FaqState, categoryId: String? = null, onRetry: () -> Unit = {}) {
        compose.setContent {
            MaterialTheme {
                FaqScreenContent(
                    state = state,
                    query = "",
                    categoryId = categoryId,
                    onQueryChange = {},
                    onCategoryChange = {},
                    onRetry = onRetry,
                    onBack = {},
                )
            }
        }
    }
}
