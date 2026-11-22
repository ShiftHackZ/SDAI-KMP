package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.shifthackz.aisdv1.core.localization.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

/**
 * Protects shared modal dismissal and balanced title geometry, including loading state.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class ModalDialogToolbarTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `given modal toolbar, close is pressed, expected one dismissal`() {
        // Given
        Localization.setLanguageCode("en")
        var calls = 0
        compose.setContent { MaterialTheme { ModalDialogToolbar("LoRA", { calls++ }) } }
        // When
        compose.onNodeWithContentDescription(Localization.string("close")).performClick()
        // Then
        assertEquals(1, calls)
    }

    @Test
    fun `given loading modal toolbar, close is pressed, expected dismissal remains available`() {
        // Given
        Localization.setLanguageCode("en")
        var calls = 0
        compose.setContent {
            MaterialTheme {
                ModalDialogToolbar(
                    "H-Net",
                    { calls++ },
                    loading = true
                )
            }
        }
        // When
        compose.onNodeWithContentDescription(Localization.string("close")).performClick()
        // Then
        assertEquals(1, calls)
        compose.onNodeWithText("H-Net").assertDoesNotExist()
    }

    @Test
    fun `given modal toolbar, title is rendered, expected centered title and forwarded bounds modifier`() {
        // Given
        compose.setContent {
            MaterialTheme {
                ModalDialogToolbar(
                    "Inversion",
                    {},
                    Modifier.testTag("toolbar")
                )
            }
        }
        // When
        val toolbar = compose.onNodeWithTag("toolbar").fetchSemanticsNode().boundsInRoot
        val title = compose.onNodeWithText("Inversion").fetchSemanticsNode().boundsInRoot
        // Then
        assertTrue(abs(toolbar.center.x - title.center.x) < 1f)
        compose.onNodeWithText("Inversion").assertIsDisplayed()
    }
}
