package com.shifthackz.aisdv1.presentation.components.widget.imageinput

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import com.shifthackz.aisdv1.core.localization.Localization
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Exercises the reusable image acquisition routes and caller-owned selected image content.
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class ImageInputTest {
    @get:Rule
    val compose = createComposeRule()
    private val requests = mutableListOf<String>()

    /** Renders the production acquisition grid.
     * @param isEnabled Whether acquisition is available.
     * @author Dmitriy Moroz
     */
    private fun renderActions(isEnabled: Boolean = true) {
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                ImageInputActions(
                    isEnabled,
                    { requests.add("gallery") },
                    { requests.add("camera") },
                    { requests.add("random") },
                    { requests.add("history") })
            }
        }
    }

    @Test
    fun `given image input, gallery action is pressed, expected only gallery callback`() {
        // Given
        renderActions()
        // When
        compose.onNodeWithText(Localization.string("action_image_picker_gallery")).performClick()
        // Then
        assertEquals(listOf("gallery"), requests)
    }

    @Test
    fun `given image input, camera action is pressed, expected only camera callback`() {
        // Given
        renderActions()
        // When
        compose.onNodeWithText(Localization.string("action_image_picker_camera")).performClick()
        // Then
        assertEquals(listOf("camera"), requests)
    }

    @Test
    fun `given image input, random action is pressed, expected only random callback`() {
        // Given
        renderActions()
        // When
        compose.onNodeWithText(Localization.string("action_image_picker_random")).performClick()
        // Then
        assertEquals(listOf("random"), requests)
    }

    @Test
    fun `given image input, history action is pressed, expected only history callback`() {
        // Given
        renderActions()
        // When
        compose.onNodeWithText(Localization.string("action_choose_generation")).performClick()
        // Then
        assertEquals(listOf("history"), requests)
    }

    @Test
    fun `given disabled image input, acquisition actions are pressed, expected no picker callback`() {
        // Given
        renderActions(false)
        // When
        listOf(
            "action_image_picker_gallery",
            "action_image_picker_camera",
            "action_image_picker_random",
            "action_choose_generation"
        ).forEach {
            compose.onNodeWithText(Localization.string(it)).assertIsNotEnabled().performClick()
        }
        // Then
        assertEquals(emptyList<String>(), requests)
    }

    @Test
    fun `given image input, gallery action is held, expected explanatory help without opening picker`() {
        // Given
        renderActions()
        // When
        compose.onNodeWithText(Localization.string("action_image_picker_gallery"))
            .performTouchInput { longClick() }
        // Then
        assertEquals(emptyList<String>(), requests)
        compose.onNodeWithText(Localization.string("help_input_photo")).assertIsDisplayed()
    }

    @Test
    fun `given selected image card, owner supplies a preview, expected preview replaces acquisition grid`() {
        // Given
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                ImageInputCard(
                    false,
                    true,
                    true,
                    {},
                    {},
                    {},
                    {},
                    Modifier.testTag("card")
                ) { Text("Selected pixels") }
            }
        }
        // When
        compose.waitForIdle()
        // Then
        compose.onNodeWithText("Selected pixels").assertIsDisplayed()
        compose.onNodeWithText(Localization.string("action_image_picker_gallery"))
            .assertDoesNotExist()
    }

    @Test
    fun `given expanded image card, header is toggled twice, expected preview collapses then returns`() {
        // Given
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                ImageInputCard(
                    false,
                    true,
                    true,
                    {},
                    {},
                    {},
                    {}) { Text("Selected pixels") }
            }
        }
        // When
        compose.onNodeWithText(Localization.string("title_input_image")).performClick()
        // Then
        compose.onNodeWithText("Selected pixels").assertDoesNotExist()
        compose.onNodeWithText(Localization.string("title_input_image")).performClick()
        compose.onNodeWithText("Selected pixels").assertIsDisplayed()
    }
}
