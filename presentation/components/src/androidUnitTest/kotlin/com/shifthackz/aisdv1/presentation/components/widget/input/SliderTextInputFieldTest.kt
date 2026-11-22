package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import com.shifthackz.aisdv1.core.localization.Localization
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards numeric drafts against accidental application and stale slider feedback.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class SliderTextInputFieldTest {
    @get:Rule
    val compose = createComposeRule()
    private var value by mutableFloatStateOf(2f)
    private val applied = mutableListOf<Float>()

    /**
     * Renders the production editor with an owner that accepts applied values.
     *
     * @author Dmitriy Moroz
     */
    private fun render() {
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                val focus = LocalFocusManager.current; Column {
                SliderTextInputField(
                    value,
                    { value = it; applied.add(it) },
                    valueRange = 1f..10f,
                    fractionDigits = 2
                )
                TextButton(onClick = { focus.clearFocus() }) { Text("Other control") }
            }
            }
        }
    }

    @Test
    fun `given numeric editor, valid draft is typed, expected Apply without immediate value mutation`() {
        // Given
        render()
        // When
        compose.onNode(hasSetTextAction()).performTextReplacement("4.25")
        // Then
        assertEquals(emptyList<Float>(), applied)
        compose.onNodeWithText(Localization.string("apply")).assertExists()
        assertEquals(2f, value)
    }

    @Test
    fun `given numeric editor, valid draft is applied, expected rounded owner value and Apply removed`() {
        // Given
        render()
        compose.onNode(hasSetTextAction()).performTextReplacement("4.257")
        // When
        compose.onNodeWithText(Localization.string("apply")).performClick()
        // Then
        assertEquals(listOf(4.26f), applied)
        compose.onNodeWithText(Localization.string("apply")).assertDoesNotExist()
    }


    @Test
    fun `given numeric editor, blank draft is entered, expected disabled Apply and no mutation`() {
        // Given
        render()
        // When
        compose.onNode(hasSetTextAction()).performTextReplacement("")
        // Then
        compose.onNodeWithText(Localization.string("apply")).assertIsNotEnabled().performClick()
        assertEquals(emptyList<Float>(), applied)
    }

    @Test
    fun `given numeric editor, malformed draft is entered, expected disabled Apply and no mutation`() {
        // Given
        render()
        // When
        compose.onNode(hasSetTextAction()).performTextReplacement("abc")
        // Then
        compose.onNodeWithText(Localization.string("apply")).assertIsNotEnabled().performClick()
        assertEquals(emptyList<Float>(), applied)
    }

    @Test
    fun `given numeric editor, below range draft is entered, expected disabled Apply and no mutation`() {
        // Given
        render()
        // When
        compose.onNode(hasSetTextAction()).performTextReplacement("0")
        // Then
        compose.onNodeWithText(Localization.string("apply")).assertIsNotEnabled().performClick()
        assertEquals(emptyList<Float>(), applied)
    }

    @Test
    fun `given numeric editor, above range draft is entered, expected disabled Apply and no mutation`() {
        // Given
        render()
        // When
        compose.onNode(hasSetTextAction()).performTextReplacement("11")
        // Then
        compose.onNodeWithText(Localization.string("apply")).assertIsNotEnabled().performClick()
        assertEquals(emptyList<Float>(), applied)
    }

    @Test
    fun `given unapplied numeric draft, focus moves elsewhere, expected original value restored`() {
        // Given
        render()
        compose.onNode(hasSetTextAction()).performTextReplacement("7")
        // When
        compose.onNodeWithText("Other control").performClick()
        // Then
        compose.onNode(hasSetTextAction()).assertTextContains("2.0")
        compose.onNodeWithText(Localization.string("apply")).assertDoesNotExist()
        assertEquals(emptyList<Float>(), applied)
    }

    @Test
    fun `given numeric editor without focus, owner value changes, expected displayed value synchronized`() {
        // Given
        render()
        // When
        compose.runOnIdle { value = 6.5f }
        // Then
        compose.onNode(hasSetTextAction()).assertTextContains("6.5")
        compose.onNodeWithText(Localization.string("apply")).assertDoesNotExist()
    }

    @Test
    fun `given numeric editor, slider changes value, expected immediate application without Apply flash`() {
        // Given
        render()
        // When
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
        // Then
        assertEquals(listOf(5f), applied)
        compose.onNode(hasSetTextAction()).assertTextContains("5.0")
        compose.onNodeWithText(Localization.string("apply")).assertDoesNotExist()
    }

    @Test
    fun `given numeric editor, equivalent rounded draft is typed, expected no redundant Apply`() {
        // Given
        render()
        // When
        compose.onNode(hasSetTextAction()).performTextReplacement("2.000")
        // Then
        compose.onNodeWithText(Localization.string("apply")).assertDoesNotExist()
        assertEquals(emptyList<Float>(), applied)
    }
}
