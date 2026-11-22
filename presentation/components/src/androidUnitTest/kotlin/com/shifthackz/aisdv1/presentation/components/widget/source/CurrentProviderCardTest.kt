package com.shifthackz.aisdv1.presentation.components.widget.source

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.compactDisplayName
import com.shifthackz.aisdv1.presentation.components.model.description
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Checks configuration navigation and provider-specific help from the reusable generation card.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class CurrentProviderCardTest {
    @get:Rule
    val compose = createComposeRule()
    private var requests = 0

    /**
     * Renders a selected provider under its declared platform.
     *
     * @param source Current provider.
     * @param platform Platform selecting local descriptions and artwork.
     *
     * @author Dmitriy Moroz
     */
    private fun render(source: ServerSource, platform: Platform) {
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                Column(Modifier.padding(top = 160.dp)) {
                    CurrentProviderCard(
                        source,
                        platform,
                        { requests++ },
                        Modifier.testTag("provider")
                    )
                }
            }
        }
    }

    @Test
    fun `given current provider card, card is tapped, expected one configuration request`() {
        // Given
        render(ServerSource.AUTOMATIC1111, Platform.ANDROID)
        // When
        compose.onNodeWithText(ServerSource.AUTOMATIC1111.compactDisplayName(Platform.ANDROID))
            .performClick()
        // Then
        assertEquals(1, requests)
        compose.onNodeWithTag("provider").assertExists()
    }


    @Test
    fun `given local_mnn provider on ios, card is held, expected platform help without navigation`() {
        // Given
        val source = ServerSource.LOCAL_MNN
        val platform = Platform.IOS
        render(source, platform)
        // When
        compose.onNodeWithTag("provider").performTouchInput { longClick() }
        // Then
        compose.onNodeWithText(source.description(platform) + "\n\n" + Localization.string("help_provider_configuration"))
            .assertIsDisplayed()
        assertEquals(0, requests)
    }

    @Test
    fun `given local_apple_bonsai provider on android, card is held, expected platform help without navigation`() {
        // Given
        val source = ServerSource.LOCAL_APPLE_BONSAI
        val platform = Platform.ANDROID
        render(source, platform)
        // When
        compose.onNodeWithTag("provider").performTouchInput { longClick() }
        // Then
        compose.onNodeWithText(source.description(platform) + "\n\n" + Localization.string("help_provider_configuration"))
            .assertIsDisplayed()
        assertEquals(0, requests)
    }

}
