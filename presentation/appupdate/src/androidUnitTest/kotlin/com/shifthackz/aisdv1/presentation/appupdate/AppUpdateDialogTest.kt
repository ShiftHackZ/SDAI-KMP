package com.shifthackz.aisdv1.presentation.appupdate

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Renders the production dialog and host to protect copy, callbacks and launch-side-effect ownership.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class AppUpdateDialogTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private var originalLanguage = "en"

    @Before
    fun selectEnglish() {
        originalLanguage = Localization.currentLanguageCode()
        Localization.setLanguageCode("en")
    }

    @After
    fun restoreLanguage() {
        Localization.setLanguageCode(originalLanguage)
    }

    @Test
    fun `given Full update dialog, a published version is rendered, expected version text and optional actions`() {
        // Given
        val version = "1.3.0"

        // When
        compose.setContent { MaterialTheme { AppUpdateDialog(version, {}, {}) } }

        // Then
        compose.onNodeWithText("Update available").assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_message", version))
            .assertIsDisplayed()
        compose.onNodeWithText("Update").assertIsDisplayed()
        compose.onNodeWithText("Later").assertIsDisplayed()
    }

    @Test
    fun `given Nightly update dialog, a published version is rendered, expected version text and optional actions`() {
        // Given
        val version = "1.2.0-dev.20261122.abcdef12"

        // When
        compose.setContent { MaterialTheme { AppUpdateDialog(version, {}, {}) } }

        // Then
        compose.onNodeWithText("Update available").assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_message", version))
            .assertIsDisplayed()
        compose.onNodeWithText("Update").assertIsDisplayed()
        compose.onNodeWithText("Later").assertIsDisplayed()
    }

    @Test
    fun `given App Store update dialog, a published version is rendered, expected version text and optional actions`() {
        // Given
        val version = "1.3.0"

        // When
        compose.setContent { MaterialTheme { AppUpdateDialog(version, {}, {}) } }

        // Then
        compose.onNodeWithText("Update available").assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_message", version))
            .assertIsDisplayed()
        compose.onNodeWithText("Update").assertIsDisplayed()
        compose.onNodeWithText("Later").assertIsDisplayed()
    }

    @Test
    fun `given localized update dialog, the uk catalog is selected, expected translated copy without changing global locale`() {
        // Given
        val language = "uk"

        // When
        compose.setContent {
            MaterialTheme {
                AppUpdateDialog(
                    "1.3.0",
                    {},
                    {},
                    languageCode = language
                )
            }
        }

        // Then
        compose.onNodeWithText(Localization.string("app_update_title", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(
            Localization.string(
                "app_update_message",
                "1.3.0",
                languageCode = language
            )
        ).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_action", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_later", languageCode = language))
            .assertIsDisplayed()
        assertEquals("en", Localization.currentLanguageCode())
    }

    @Test
    fun `given localized update dialog, the de catalog is selected, expected translated copy without changing global locale`() {
        // Given
        val language = "de"

        // When
        compose.setContent {
            MaterialTheme {
                AppUpdateDialog(
                    "1.3.0",
                    {},
                    {},
                    languageCode = language
                )
            }
        }

        // Then
        compose.onNodeWithText(Localization.string("app_update_title", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(
            Localization.string(
                "app_update_message",
                "1.3.0",
                languageCode = language
            )
        ).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_action", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_later", languageCode = language))
            .assertIsDisplayed()
        assertEquals("en", Localization.currentLanguageCode())
    }

    @Test
    fun `given localized update dialog, the tr catalog is selected, expected translated copy without changing global locale`() {
        // Given
        val language = "tr"

        // When
        compose.setContent {
            MaterialTheme {
                AppUpdateDialog(
                    "1.3.0",
                    {},
                    {},
                    languageCode = language
                )
            }
        }

        // Then
        compose.onNodeWithText(Localization.string("app_update_title", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(
            Localization.string(
                "app_update_message",
                "1.3.0",
                languageCode = language
            )
        ).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_action", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_later", languageCode = language))
            .assertIsDisplayed()
        assertEquals("en", Localization.currentLanguageCode())
    }

    @Test
    fun `given localized update dialog, the ru catalog is selected, expected translated copy without changing global locale`() {
        // Given
        val language = "ru"

        // When
        compose.setContent {
            MaterialTheme {
                AppUpdateDialog(
                    "1.3.0",
                    {},
                    {},
                    languageCode = language
                )
            }
        }

        // Then
        compose.onNodeWithText(Localization.string("app_update_title", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(
            Localization.string(
                "app_update_message",
                "1.3.0",
                languageCode = language
            )
        ).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_action", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_later", languageCode = language))
            .assertIsDisplayed()
        assertEquals("en", Localization.currentLanguageCode())
    }

    @Test
    fun `given localized update dialog, the zh catalog is selected, expected translated copy without changing global locale`() {
        // Given
        val language = "zh"

        // When
        compose.setContent {
            MaterialTheme {
                AppUpdateDialog(
                    "1.3.0",
                    {},
                    {},
                    languageCode = language
                )
            }
        }

        // Then
        compose.onNodeWithText(Localization.string("app_update_title", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(
            Localization.string(
                "app_update_message",
                "1.3.0",
                languageCode = language
            )
        ).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_action", languageCode = language))
            .assertIsDisplayed()
        compose.onNodeWithText(Localization.string("app_update_later", languageCode = language))
            .assertIsDisplayed()
        assertEquals("en", Localization.currentLanguageCode())
    }

    @Test
    fun `given update dialog actions, Update is tapped, expected only the update callback`() {
        // Given
        var updates = 0
        var dismissals = 0
        compose.setContent {
            MaterialTheme {
                AppUpdateDialog(
                    "1.3.0",
                    { updates++ },
                    { dismissals++ })
            }
        }

        // When
        compose.onNodeWithText("Update").performClick()

        // Then
        assertEquals(1, updates)
        assertEquals(0, dismissals)
    }

    @Test
    fun `given update dialog actions, Later is tapped, expected only the dismiss callback`() {
        // Given
        var updates = 0
        var dismissals = 0
        compose.setContent {
            MaterialTheme {
                AppUpdateDialog(
                    "1.3.0",
                    { updates++ },
                    { dismissals++ })
            }
        }

        // When
        compose.onNodeWithText("Later").performClick()

        // Then
        assertEquals(0, updates)
        assertEquals(1, dismissals)
    }

    @Test
    fun `given launch update host, discovery finds no update, expected no dialog and one discovery`() {
        // Given
        var calls = 0
        val state = AppUpdateLaunchStateImpl { calls++; null }

        // When
        compose.setContent { MaterialTheme { AppUpdateHost(state) } }
        compose.waitForIdle()

        // Then
        compose.onNodeWithText("Update available").assertDoesNotExist()
        assertEquals(1, calls)
    }

    @Test
    fun `given launch update host, Later is tapped after an offer, expected the pending offer is consumed`() {
        // Given
        val state = AppUpdateLaunchStateImpl {
            AppUpdate(
                "1.3.0",
                "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.3.0"
            )
        }
        compose.setContent { MaterialTheme { AppUpdateHost(state) } }

        // When
        compose.onNodeWithText("Later").performClick()
        compose.waitForIdle()

        // Then
        assertNull(state.update.value)
        compose.onNodeWithText("Update available").assertDoesNotExist()
    }

    @Test
    fun `given launch update host, a parent recomposes after dismissal, expected no renewed discovery or dialog`() {
        // Given
        var calls = 0
        val revision = mutableIntStateOf(0)
        val state =
            AppUpdateLaunchStateImpl { calls++; AppUpdate("1.3.0", "https://example.com/release") }
        compose.setContent {
            revision.intValue
            MaterialTheme { AppUpdateHost(state) }
        }
        compose.onNodeWithText("Later").performClick()

        // When
        compose.runOnIdle { revision.intValue++ }
        compose.waitForIdle()

        // Then
        assertEquals(1, calls)
        compose.onNodeWithText("Update available").assertDoesNotExist()
    }

    @Test
    fun `given launch update host, Update is tapped, expected the trusted destination opens and the offer is consumed`() {
        // Given
        val url = "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.3.0"
        val state = AppUpdateLaunchStateImpl { AppUpdate("1.3.0", url) }
        compose.setContent { MaterialTheme { AppUpdateHost(state) } }

        // When
        compose.onNodeWithText("Update").performClick()
        compose.waitForIdle()

        // Then
        assertEquals(url, shadowOf(compose.activity).nextStartedActivity.dataString)
        assertNull(state.update.value)
        compose.onNodeWithText("Update available").assertDoesNotExist()
    }
}
