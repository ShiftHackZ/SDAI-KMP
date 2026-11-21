package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Checks that the actual demo modal presents two independent acknowledgements and a guarded CTA.
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w400dp-h1000dp")
class DemoModeExplanationDialogTest {
    @get:Rule
    val compose = createComposeRule()
    private val intents = mutableListOf<ServerSetupIntent>()

    /** Renders caller-owned checkbox state without replacing the production dialog.
     * @param hasUnderstood First acknowledgement value.
     * @param hasAgreed Feedback acknowledgement value.
     * @author Dmitriy Moroz
     */
    private fun render(hasUnderstood: Boolean = false, hasAgreed: Boolean = false) {
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                DemoModeExplanationDialog(
                    ServerSetupState.Modal.DemoExplanation(
                        hasUnderstood,
                        hasAgreed
                    ), { intents.add(it) })
            }
        }
    }

    @Test
    fun `given demo acknowledgements false and false, Continue is pressed, expected no submission`() {
        // Given
        render(false, false)
        // When
        compose.onNodeWithText(Localization.string("demo_explanation_continue"))
            .assertIsNotEnabled().performClick()
        // Then
        assertEquals(emptyList<ServerSetupIntent>(), intents)
    }

    @Test
    fun `given demo acknowledgements true and false, Continue is pressed, expected no submission`() {
        // Given
        render(true, false)
        // When
        compose.onNodeWithText(Localization.string("demo_explanation_continue"))
            .assertIsNotEnabled().performClick()
        // Then
        assertEquals(emptyList<ServerSetupIntent>(), intents)
    }

    @Test
    fun `given demo acknowledgements false and true, Continue is pressed, expected no submission`() {
        // Given
        render(false, true)
        // When
        compose.onNodeWithText(Localization.string("demo_explanation_continue"))
            .assertIsNotEnabled().performClick()
        // Then
        assertEquals(emptyList<ServerSetupIntent>(), intents)
    }

    @Test
    fun `given both demo acknowledgements, Continue is pressed, expected explicit confirmation intent`() {
        // Given
        render(true, true)
        // When
        compose.onNodeWithText(Localization.string("demo_explanation_continue")).assertIsEnabled()
            .performClick()
        // Then
        assertEquals(listOf(ServerSetupIntent.ConfirmDemoExplanation), intents)
    }

    @Test
    fun `given demo modal, behavior acknowledgement is pressed, expected only behavior toggle request`() {
        // Given
        render()
        // When
        compose.onNodeWithText(Localization.string("demo_explanation_understand")).performScrollTo()
            .performClick()
        // Then
        assertEquals(listOf(ServerSetupIntent.AcknowledgeDemoBehavior(true)), intents)
    }

    @Test
    fun `given demo modal, feedback acknowledgement is pressed, expected only feedback toggle request`() {
        // Given
        render()
        // When
        compose.onNodeWithText(Localization.string("demo_explanation_feedback")).performScrollTo()
            .performClick()
        // Then
        assertEquals(listOf(ServerSetupIntent.AgreeToDemoFeedback(true)), intents)
    }

    @Test
    fun `given demo modal, flat cross is pressed, expected close request instead of confirmation`() {
        // Given
        render()
        // When
        compose.onNodeWithContentDescription(Localization.string("demo_explanation_close"))
            .performClick()
        // Then
        assertEquals(listOf(ServerSetupIntent.CloseDemoExplanation), intents)
    }
}
