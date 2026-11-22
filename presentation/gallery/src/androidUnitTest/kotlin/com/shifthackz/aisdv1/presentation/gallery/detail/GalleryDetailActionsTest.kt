package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.gallery.mocks.mockAiGenerationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Protects compact gallery actions from dispatching an adjacent or destructive operation.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class GalleryDetailActionsTest {
    @get:Rule
    val compose = createComposeRule()
    private val intents = mutableListOf<GalleryDetailIntent>()

    /**
     * Renders actual detail actions using the existing generation-result model fixture.
     *
     * @param canReport Whether this provider exposes reporting.
     * @param isLiked Whether the persisted item is already liked.
     *
     * @author Dmitriy Moroz
     */
    private fun render(canReport: Boolean = true, isLiked: Boolean = false) {
        Localization.setLanguageCode("en")
        val content = mockAiGenerationResult.copy(liked = isLiked).toGalleryDetailContent(canReport)
        compose.setContent {
            MaterialTheme {
                Column(Modifier.padding(top = 160.dp)) {
                    GalleryDetailActions(content, { intents.add(it) })
                }
            }
        }
    }

    @Test
    fun `given compact detail actions, like is pressed, expected only its existing intent`() {
        // Given
        render()
        // When
        compose.onNodeWithContentDescription(Localization.string("gallery_action_like"))
            .performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.ToggleLike), intents)
    }

    @Test
    fun `given compact detail actions, reuse prompt is pressed, expected only its existing intent`() {
        // Given
        render()
        // When
        compose.onNodeWithContentDescription(Localization.string("home_tab_txt_to_img"))
            .performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.SendTo.Txt2Img), intents)
    }

    @Test
    fun `given compact detail actions, reuse image is pressed, expected only its existing intent`() {
        // Given
        render()
        // When
        compose.onNodeWithContentDescription(Localization.string("home_tab_img_to_img"))
            .performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.SendTo.Img2Img), intents)
    }

    @Test
    fun `given compact detail actions, visibility is pressed, expected only its existing intent`() {
        // Given
        render()
        // When
        compose.onNodeWithContentDescription(Localization.string("gallery_action_blur"))
            .performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.ToggleVisibility), intents)
    }

    @Test
    fun `given compact detail actions, report is pressed, expected only its existing intent`() {
        // Given
        render()
        // When
        compose.onNodeWithContentDescription(Localization.string("gallery_action_report"))
            .performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.Report), intents)
    }

    @Test
    fun `given compact detail actions, delete is pressed, expected only its existing intent`() {
        // Given
        render()
        // When
        compose.onNodeWithContentDescription(Localization.string("gallery_action_delete"))
            .performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.Delete.Request), intents)
    }

    @Test
    fun `given liked detail, unlike is pressed, expected same toggle operation`() {
        // Given
        render(isLiked = true)
        // When
        compose.onNodeWithContentDescription(Localization.string("gallery_action_unlike"))
            .performClick()
        // Then
        assertEquals(listOf(GalleryDetailIntent.ToggleLike), intents)
    }

    @Test
    fun `given report-capable detail, action row is laid out, expected delete after report`() {
        // Given
        render()
        // When
        val report =
            compose.onNodeWithContentDescription(Localization.string("gallery_action_report"))
                .fetchSemanticsNode().boundsInRoot
        val delete =
            compose.onNodeWithContentDescription(Localization.string("gallery_action_delete"))
                .fetchSemanticsNode().boundsInRoot
        // Then
        assertTrue(delete.left >= report.right)
    }

    @Test
    fun `given detail without reporting, action row is laid out, expected delete last and no report`() {
        // Given
        render(canReport = false)
        // When
        val visibility =
            compose.onNodeWithContentDescription(Localization.string("gallery_action_blur"))
                .fetchSemanticsNode().boundsInRoot
        val delete =
            compose.onNodeWithContentDescription(Localization.string("gallery_action_delete"))
                .fetchSemanticsNode().boundsInRoot
        // Then
        assertTrue(delete.left >= visibility.right)
        compose.onNodeWithContentDescription(Localization.string("gallery_action_report"))
            .assertDoesNotExist()
    }

    @Test
    fun `given destructive detail action, delete is held, expected help without delete request`() {
        // Given
        render()
        // When
        compose.onNodeWithContentDescription(Localization.string("gallery_action_delete"))
            .performTouchInput { longClick() }
        // Then
        assertEquals(emptyList<GalleryDetailIntent>(), intents)
        compose.onNodeWithText(Localization.string("help_detail_delete")).assertIsDisplayed()
    }
}
