package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformSwitch
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformTabBar
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies shared action contracts against their real Android implementations.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PlatformControlsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `given PrimaryButton, enabled action is pressed, expected one caller callback`() {
        // Given
        var calls = 0
        compose.setContent {
            MaterialTheme {
                PrimaryButton(
                    { calls++ },
                    Modifier.testTag("action"),
                    isEnabled = true
                ) { Text("Action") }
            }
        }
        // When
        compose.onNodeWithTag("action").performClick()
        // Then
        assertEquals(1, calls)
        compose.onNodeWithText("Action").assertExists()
    }

    @Test
    fun `given PrimaryButton, disabled action is pressed, expected no caller callback`() {
        // Given
        var calls = 0
        compose.setContent {
            MaterialTheme {
                PrimaryButton(
                    { calls++ },
                    Modifier.testTag("action"),
                    isEnabled = false
                ) { Text("Action") }
            }
        }
        // When
        compose.onNodeWithTag("action").performClick()
        // Then
        assertEquals(0, calls)
        compose.onNodeWithTag("action").assertIsNotEnabled()
    }

    @Test
    fun `given OutlinedButton, enabled action is pressed, expected one caller callback`() {
        // Given
        var calls = 0
        compose.setContent {
            MaterialTheme {
                OutlinedButton(
                    { calls++ },
                    Modifier.testTag("action"),
                    isEnabled = true
                ) { Text("Action") }
            }
        }
        // When
        compose.onNodeWithTag("action").performClick()
        // Then
        assertEquals(1, calls)
        compose.onNodeWithText("Action").assertExists()
    }

    @Test
    fun `given OutlinedButton, disabled action is pressed, expected no caller callback`() {
        // Given
        var calls = 0
        compose.setContent {
            MaterialTheme {
                OutlinedButton(
                    { calls++ },
                    Modifier.testTag("action"),
                    isEnabled = false
                ) { Text("Action") }
            }
        }
        // When
        compose.onNodeWithTag("action").performClick()
        // Then
        assertEquals(0, calls)
        compose.onNodeWithTag("action").assertIsNotEnabled()
    }

    @Test
    fun `given CircleIconButton, enabled action is pressed, expected one caller callback`() {
        // Given
        var calls = 0
        compose.setContent {
            MaterialTheme {
                CircleIconButton(
                    { calls++ },
                    Icons.Default.Close,
                    "Dismiss",
                    Modifier.testTag("action"),
                    isEnabled = true
                )
            }
        }
        // When
        compose.onNodeWithTag("action").performClick()
        // Then
        assertEquals(1, calls)
        compose.onNodeWithContentDescription("Dismiss").assertExists()
    }

    @Test
    fun `given CircleIconButton, disabled action is pressed, expected no caller callback`() {
        // Given
        var calls = 0
        compose.setContent {
            MaterialTheme {
                CircleIconButton(
                    { calls++ },
                    Icons.Default.Close,
                    "Dismiss",
                    Modifier.testTag("action"),
                    isEnabled = false
                )
            }
        }
        // When
        compose.onNodeWithTag("action").performClick()
        // Then
        assertEquals(0, calls)
        compose.onNodeWithTag("action").assertIsNotEnabled()
    }

    @Test
    fun `given platform switch, user toggles on, expected request without mutating caller state`() {
        // Given
        val requests = mutableListOf<Boolean>()
        compose.setContent {
            PlatformSwitch(
                false,
                { requests.add(it) },
                Modifier.testTag("switch")
            )
        }
        // When
        compose.onNodeWithTag("switch").performClick()
        // Then
        assertEquals(listOf(true), requests)
        compose.onNodeWithTag("switch").assertIsOff()
    }

    @Test
    fun `given checked platform switch, user toggles off, expected false request`() {
        // Given
        val requests = mutableListOf<Boolean>()
        compose.setContent {
            PlatformSwitch(
                true,
                { requests.add(it) },
                Modifier.testTag("switch")
            )
        }
        // When
        compose.onNodeWithTag("switch").performClick()
        // Then
        assertEquals(listOf(false), requests)
        compose.onNodeWithTag("switch").assertIsOn()
    }

    @Test
    fun `given disabled platform switch, user presses it, expected no change request`() {
        // Given
        val requests = mutableListOf<Boolean>()
        compose.setContent {
            PlatformSwitch(
                true,
                { requests.add(it) },
                Modifier.testTag("switch"),
                isEnabled = false
            )
        }
        // When
        compose.onNodeWithTag("switch").performClick()
        // Then
        assertEquals(emptyList<Boolean>(), requests)
        compose.onNodeWithTag("switch").assertIsNotEnabled()
    }

    @Test
    fun `given decorative platform switch, enclosing row is pressed, expected only parent activation`() {
        // Given
        var calls = 0
        compose.setContent {
            PrimaryButton({ calls++ }) {
                PlatformSwitch(
                    false,
                    null
                ); Text("Setting")
            }
        }
        // When
        compose.onNodeWithText("Setting").performClick()
        // Then
        assertEquals(1, calls)
    }

    @Test
    fun `given platform tabs, another tab is pressed, expected requested index with caller selection preserved`() {
        // Given
        val requests = mutableListOf<Int>()
        compose.setContent {
            PlatformTabBar(
                listOf("Image", "Info"),
                0,
                { requests.add(it) },
                Modifier.testTag("tabs")
            )
        }
        // When
        compose.onNodeWithText("Info").performClick()
        // Then
        assertEquals(listOf(1), requests)
        compose.onNodeWithText("Image").assertIsSelected()
        compose.onNodeWithText("Info").assertIsNotSelected()
        compose.onNodeWithTag("tabs").assertExists()
    }

    @Test
    fun `given disabled platform tabs, another tab is pressed, expected no selection request`() {
        // Given
        val requests = mutableListOf<Int>()
        compose.setContent {
            PlatformTabBar(
                listOf("Create", "Restore"),
                0,
                { requests.add(it) },
                isEnabled = false
            )
        }
        // When
        compose.onNodeWithText("Restore").performClick()
        // Then
        assertEquals(emptyList<Int>(), requests)
        compose.onNodeWithText("Restore").assertIsNotEnabled()
    }
}
