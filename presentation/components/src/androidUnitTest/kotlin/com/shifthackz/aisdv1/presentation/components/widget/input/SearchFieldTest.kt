package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Protects search query ownership and empty-state decoration.
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SearchFieldTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `given search field, query is typed, expected exact edit delivered without filtering`() {
        // Given
        var query by mutableStateOf("")
        compose.setContent {
            MaterialTheme {
                SearchField(
                    query,
                    { query = it },
                    "Search providers",
                    Modifier.testTag("search")
                )
            }
        }
        // When
        compose.onNode(hasSetTextAction()).performTextReplacement("  MNN / SDXL  ")
        // Then
        assertEquals("  MNN / SDXL  ", query)
        compose.onNodeWithTag("search").assertExists()
        compose.onNodeWithText("Search providers").assertDoesNotExist()
    }

    @Test
    fun `given search field with query, owner clears it, expected empty editor and placeholder restored`() {
        // Given
        var query by mutableStateOf("MNN")
        compose.setContent {
            MaterialTheme {
                SearchField(
                    query,
                    { query = it },
                    "Search providers"
                )
            }
        }
        // When
        compose.runOnIdle { query = "" }
        // Then
        assertEquals(
            AnnotatedString(""),
            compose.onNode(hasSetTextAction())
                .fetchSemanticsNode().config[SemanticsProperties.EditableText]
        )
        compose.onNodeWithText("Search providers", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `given search field, owner replaces query externally, expected editor follows owner without stale draft`() {
        // Given
        var query by mutableStateOf("MNN")
        compose.setContent {
            MaterialTheme {
                SearchField(
                    query,
                    { query = it },
                    "Search providers"
                )
            }
        }
        // When
        compose.runOnIdle { query = "Bonsai" }
        // Then
        compose.onNode(hasSetTextAction()).assertTextEquals("Bonsai")
    }
}
