package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.gallery.mocks.mockAiGenerationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Protects table column alignment for absent text and the provider/model reading order.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class GalleryMetadataLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    @Before
    fun initializeLocalization() {
        Localization.setLanguageCode("en")
    }

    @Test
    fun `given populated empty and whitespace metadata, detail rows render, expected aligned dashes and no empty copy actions`() {
        // Given
        compose.setContent {
            MaterialTheme {
                Column(Modifier.width(360.dp)) {
                    listOf("CPU", "", "  \t ").forEachIndexed { index, value ->
                        GalleryDetailRow(
                            name = "Field $index",
                            value = value,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        // When
        val populatedLeft = compose.onNodeWithText("CPU").fetchSemanticsNode().boundsInRoot.left
        val blanks = compose.onAllNodesWithText("-").fetchSemanticsNodes()
        // Then
        assertEquals(2, blanks.size)
        blanks.forEach { assertEquals(populatedLeft, it.boundsInRoot.left, 0.01f) }
        compose.onAllNodesWithContentDescription(Localization.string("action_copy"))
            .assertCountEquals(1)
    }

    @Test
    fun `given image details with provider and model, table is rendered, expected model directly between provider and acceleration`() {
        // Given
        val content = mockAiGenerationResult.copy(
            providerKey = "local_mnn",
            modelName = "SD 1.5",
            acceleration = "CPU"
        ).toGalleryDetailContent(false)
        compose.setContent { MaterialTheme { GalleryDetailsTable(content = content) } }
        // When
        val provider = compose.onNodeWithText("Provider").fetchSemanticsNode().boundsInRoot.top
        val model = compose.onNodeWithText("Model").fetchSemanticsNode().boundsInRoot.top
        val acceleration =
            compose.onNodeWithText("Acceleration").fetchSemanticsNode().boundsInRoot.top
        // Then
        assertTrue(provider < model)
        assertTrue(model < acceleration)
    }
}
