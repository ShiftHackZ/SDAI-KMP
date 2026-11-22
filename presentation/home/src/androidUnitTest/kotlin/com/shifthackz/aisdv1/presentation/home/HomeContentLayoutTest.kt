package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityState
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityWidget
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarTopContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Protects status ownership: native routes host the badge in their chrome, Android keeps it outside.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class HomeContentLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `given native home monitoring, route has not consumed status, expected shell reserves no separate status strip`() {
        // Given
        var hasStatusSlot = false
        compose.setContent {
            HomeContentLayout(true, status = { Text("Connection") }) {
                val hasSlot = LocalToolbarTopContent.current != null
                SideEffect { hasStatusSlot = hasSlot }
                Text("Route", Modifier.testTag("route"))
            }
        }
        // When
        val route = compose.onNodeWithTag("route").fetchSemanticsNode().boundsInRoot
        // Then
        assertTrue(hasStatusSlot)
        assertEquals(0f, route.top)
        compose.onNodeWithText("Connection").assertDoesNotExist()
    }

    @Test
    fun `given native home monitoring, route consumes status, expected one badge inside route chrome`() {
        // Given
        render(true, ConnectivityState.Connected())
        // When
        val badge = compose.onNodeWithText(Localization.string("status_connected"))
            .fetchSemanticsNode().boundsInRoot
        val route = compose.onNodeWithTag("route").fetchSemanticsNode().boundsInRoot
        val title = compose.onNodeWithText("Title").fetchSemanticsNode().boundsInRoot
        // Then
        assertEquals(0f, route.top)
        assertTrue(badge.top >= route.top)
        assertTrue(title.top >= badge.bottom)
        compose.onAllNodesWithText(Localization.string("status_connected")).assertCountEquals(1)
    }

    @Test
    fun `given native home monitoring, status toggles off then on, expected route origin stays fixed and badge is restored once`() {
        // Given
        val isEnabled = mutableStateOf(true)
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                HomeContentLayout(
                    true,
                    status = { ConnectivityWidget(ConnectivityState.Connected(isEnabled.value)) }) {
                    Column(Modifier.fillMaxWidth().testTag("route")) {
                        LocalToolbarTopContent.current?.invoke()
                        Text("Title")
                    }
                }
            }
        }
        // When
        compose.runOnIdle { isEnabled.value = false }
        // Then
        compose.onNodeWithText(Localization.string("status_connected")).assertDoesNotExist()
        assertEquals(0f, compose.onNodeWithTag("route").fetchSemanticsNode().boundsInRoot.top)
        compose.runOnIdle { isEnabled.value = true }
        compose.onAllNodesWithText(Localization.string("status_connected")).assertCountEquals(1)
        assertEquals(0f, compose.onNodeWithTag("route").fetchSemanticsNode().boundsInRoot.top)
    }

    @Test
    fun `given Android home monitoring, shell renders status, expected badge above route without native slot`() {
        // Given
        var hasNativeSlot = true
        render(false, ConnectivityState.Disconnected()) { hasNativeSlot = it }
        // When
        val badge = compose.onNodeWithText(Localization.string("status_disconnected"))
            .fetchSemanticsNode().boundsInRoot
        val route = compose.onNodeWithTag("route").fetchSemanticsNode().boundsInRoot
        // Then
        assertFalse(hasNativeSlot)
        assertTrue(route.top >= badge.bottom)
        compose.onNodeWithText(Localization.string("status_disconnected")).assertIsDisplayed()
    }

    @Test
    fun `given standalone route layout, status is disabled, expected no status slot or extra top space`() {
        // Given
        var hasNativeSlot = true
        render(false, ConnectivityState.Uninitialized(false)) { hasNativeSlot = it }
        // When
        val route = compose.onNodeWithTag("route").fetchSemanticsNode().boundsInRoot
        // Then
        assertFalse(hasNativeSlot)
        assertEquals(0f, route.top)
        compose.onNodeWithText(Localization.string("status_communicating")).assertDoesNotExist()
    }

    /**
     * Renders the real badge in the shell's status slot and lets the route consume native chrome.
     *
     * @param hasNativeToolbar Whether the route owns the status slot.
     * @param state Provider status and monitoring visibility.
     * @param onSlotObserved Receives whether the route can consume a native status slot.
     *
     * @author Dmitriy Moroz
     */
    private fun render(
        hasNativeToolbar: Boolean,
        state: ConnectivityState,
        onSlotObserved: (Boolean) -> Unit = {}
    ) {
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                HomeContentLayout(hasNativeToolbar, status = { ConnectivityWidget(state) }) {
                    val status = LocalToolbarTopContent.current
                    SideEffect { onSlotObserved(status != null) }
                    Column(Modifier.fillMaxWidth().testTag("route")) {
                        status?.invoke()
                        Text("Title")
                    }
                }
            }
        }
    }
}
