package com.shifthackz.aisdv1.presentation.components.widget.section

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Ensures disclosure events remain caller-owned and accessible.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ExpandableSectionHeaderTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `given section header, title is tapped, expected one disclosure request`() {
        // Given
        var calls = 0
        compose.setContent {
            MaterialTheme {
                ExpandableSectionHeader(
                    "Options",
                    true,
                    { calls++ },
                    Modifier.testTag("header")
                )
            }
        }
        // When
        compose.onNodeWithText("Options").performClick()
        // Then
        assertEquals(1, calls)
        compose.onNodeWithTag("header").assertExists()
    }


    @Test
    fun `given expanded section header, accessibility collapse is requested, expected one disclosure event`() {
        // Given
        var calls = 0
        compose.setContent {
            ExpandableSectionHeader(
                "Options",
                true,
                { calls++ },
                Modifier.testTag("header")
            )
        }
        // When
        compose.onNodeWithTag("header").performSemanticsAction(SemanticsActions.Collapse) { it() }
        // Then
        assertEquals(1, calls)
    }

    @Test
    fun `given collapsed section header, accessibility expand is requested, expected one disclosure event`() {
        // Given
        var calls = 0
        compose.setContent {
            ExpandableSectionHeader(
                "Options",
                false,
                { calls++ },
                Modifier.testTag("header")
            )
        }
        // When
        compose.onNodeWithTag("header").performSemanticsAction(SemanticsActions.Expand) { it() }
        // Then
        assertEquals(1, calls)
    }

    @Test
    fun `given section header, status content is supplied, expected status and heading retained`() {
        // Given
        compose.setContent {
            MaterialTheme {
                ExpandableSectionHeader(
                    "Options",
                    false,
                    {},
                    trailingContent = { Text("Loading") })
            }
        }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText("Loading", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("Options", useUnmergedTree = true).assertIsDisplayed()
    }
}
