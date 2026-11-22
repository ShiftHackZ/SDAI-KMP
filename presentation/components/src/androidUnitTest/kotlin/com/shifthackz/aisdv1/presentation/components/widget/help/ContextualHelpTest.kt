package com.shifthackz.aisdv1.presentation.components.widget.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.source.BetaBadge
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Protects help-only gestures from invoking destructive or navigational primary actions.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class ContextualHelpTest {
    @get:Rule
    val compose = createComposeRule()
    private var calls = 0

    /**
     * Renders the production help wrapper with a real shared control.
     *
     * @author Dmitriy Moroz
     */
    private fun render() {
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                Column(Modifier.padding(top = 160.dp)) {
                    ContextualHelp("Action help", "Explanation", Modifier.testTag("anchor")) {
                        PrimaryButton({ calls++ }) { Text("Execute") }
                    }
                }
            }
        }
    }

    @Test
    fun `given contextual help, control is tapped, expected primary action without popup`() {
        // Given
        render()
        // When
        compose.onNodeWithText("Execute").performClick()
        // Then
        assertEquals(1, calls)
        compose.onNodeWithText("Explanation").assertDoesNotExist()
    }

    @Test
    fun `given contextual help, control is held, expected popup without primary action`() {
        // Given
        render()
        // When
        compose.onNodeWithText("Execute").performTouchInput { longClick() }
        // Then
        assertEquals(0, calls)
        compose.onNodeWithText("Explanation").assertIsDisplayed()
    }

    @Test
    fun `given persistent contextual help, reading takes several seconds, expected popup remains visible`() {
        // Given
        render()
        compose.onNodeWithText("Execute").performTouchInput { longClick() }
        // When
        compose.mainClock.advanceTimeBy(6000)
        // Then
        compose.onNodeWithText("Explanation").assertIsDisplayed()
        assertEquals(0, calls)
    }

    @Test
    fun `given visible contextual help, flat cross is pressed, expected popup dismissed without primary action`() {
        // Given
        render()
        compose.onNodeWithText("Execute").performTouchInput { longClick() }
        // When
        compose.onNodeWithContentDescription(Localization.string("close")).performClick()
        // Then
        compose.onNodeWithText("Explanation").assertDoesNotExist()
        assertEquals(0, calls)
    }

    @Test
    fun `given contextual help, accessibility help action is requested, expected same explanation without activation`() {
        // Given
        render()
        // When
        val action = compose.onNodeWithTag("anchor")
            .fetchSemanticsNode().config[SemanticsActions.CustomActions].single()
        compose.runOnIdle { action.action() }
        // Then
        compose.onNodeWithText("Explanation").assertIsDisplayed()
        assertEquals(0, calls)
    }

    @Test
    fun `given Beta badge inside action, badge is tapped, expected parent action receives tap`() {
        // Given
        Localization.setLanguageCode("en")
        compose.setContent { MaterialTheme { PrimaryButton({ calls++ }) { Text("Setting"); BetaBadge() } } }
        // When
        compose.onNodeWithText(Localization.string("provider_readiness_beta"))
            .performTouchInput { down(center); up() }
        // Then
        assertEquals(1, calls)
    }

    @Test
    fun `given Beta badge inside action, badge is held, expected beta explanation without parent action`() {
        // Given
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                Column(Modifier.padding(top = 160.dp)) {
                    PrimaryButton({ calls++ }) { Text("Setting"); BetaBadge() }
                }
            }
        }
        // When
        compose.onNodeWithText(Localization.string("provider_readiness_beta"))
            .performTouchInput { longClick() }
        // Then
        compose.onNodeWithText(Localization.string("settings_help_beta")).assertIsDisplayed()
        assertEquals(0, calls)
    }
}
