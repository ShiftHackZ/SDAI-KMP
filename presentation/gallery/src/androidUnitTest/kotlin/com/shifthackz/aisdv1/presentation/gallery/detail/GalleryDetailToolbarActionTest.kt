package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.shifthackz.aisdv1.core.localization.Localization
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Checks tab-dependent sharing payloads through the production toolbar action.
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GalleryDetailToolbarActionTest {
    @get:Rule
    val compose = createComposeRule()


    @Test
    fun `given image detail tab, toolbar action is pressed, expected image share intent`() {
        // Given
        Localization.setLanguageCode("en")
        val intents = mutableListOf<GalleryDetailIntent>()
        compose.setContent {
            MaterialTheme {
                GalleryDetailToolbarAction(
                    GalleryDetailTab.IMAGE,
                    true,
                    { intents.add(it) })
            }
        }
        // When
        compose.onNodeWithContentDescription(Localization.string("action_export")).performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.Share.Image), intents)
    }

    @Test
    fun `given original detail tab, toolbar action is pressed, expected image share intent`() {
        // Given
        Localization.setLanguageCode("en")
        val intents = mutableListOf<GalleryDetailIntent>()
        compose.setContent {
            MaterialTheme {
                GalleryDetailToolbarAction(
                    GalleryDetailTab.ORIGINAL,
                    true,
                    { intents.add(it) })
            }
        }
        // When
        compose.onNodeWithContentDescription(Localization.string("action_export")).performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.Share.Image), intents)
    }

    @Test
    fun `given info detail tab, toolbar action is pressed, expected params share intent`() {
        // Given
        Localization.setLanguageCode("en")
        val intents = mutableListOf<GalleryDetailIntent>()
        compose.setContent {
            MaterialTheme {
                GalleryDetailToolbarAction(
                    GalleryDetailTab.INFO,
                    true,
                    { intents.add(it) })
            }
        }
        // When
        compose.onNodeWithContentDescription(Localization.string("action_share_prompt"))
            .performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.Share.Params), intents)
    }

    @Test
    fun `given unloaded detail, toolbar action is pressed, expected no share request`() {
        // Given
        Localization.setLanguageCode("en")
        val intents = mutableListOf<GalleryDetailIntent>()
        compose.setContent {
            MaterialTheme {
                GalleryDetailToolbarAction(
                    GalleryDetailTab.INFO,
                    false,
                    { intents.add(it) })
            }
        }
        // When
        compose.onNodeWithContentDescription(Localization.string("action_share_prompt"))
            .assertIsNotEnabled().performClick()
        // Then
        assertEquals(emptyList<GalleryDetailIntent>(), intents)
    }
}
