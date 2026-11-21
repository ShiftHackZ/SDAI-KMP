package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityState
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityWidget
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarStatusBarInset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Protects home monitoring placement and single ownership of the native status-area inset.
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class HomeContentLayoutTest {
    @get:Rule
    val compose = createComposeRule()
    private var childInset: Dp? = null

    /** Renders the actual status widget within the production home layout.
     * @param shouldReserve Whether the native shell owns safe-area placement.
     * @param state Provider status and indicator visibility.
     * @author Dmitriy Moroz
     */
    private fun render(shouldReserve: Boolean, state: ConnectivityState) {
        Localization.setLanguageCode("en")
        compose.setContent {
            MaterialTheme {
                HomeContentLayout(shouldReserve, WindowInsets(top = 60), status = {
                    ConnectivityWidget(state, Modifier.testTag("status"))
                }) {
                    val inset = LocalToolbarStatusBarInset.current
                    SideEffect { childInset = inset }
                    Box(Modifier.height(64.dp).testTag("toolbar")) { Text("Route toolbar") }
                }
            }
        }
    }

    @Test
    fun `given native home monitoring, indicator is enabled, expected badge below system area and toolbar below badge`() {
        // Given
        render(true, ConnectivityState.Connected())
        // When
        val badge = compose.onNodeWithText(Localization.string("status_connected"))
            .fetchSemanticsNode().boundsInRoot
        val toolbar = compose.onNodeWithTag("toolbar").fetchSemanticsNode().boundsInRoot
        // Then
        assertTrue(badge.top >= 60f)
        assertTrue(toolbar.top >= badge.bottom)
        assertEquals(0.dp, childInset)
    }

    @Test
    fun `given native home monitoring, indicator is disabled, expected one safe area without empty status gap`() {
        // Given
        render(true, ConnectivityState.Connected(false))
        // When
        val toolbar = compose.onNodeWithTag("toolbar").fetchSemanticsNode().boundsInRoot
        // Then
        assertEquals(60f, toolbar.top)
        compose.onNodeWithText(Localization.string("status_connected")).assertDoesNotExist()
        assertEquals(0.dp, childInset)
    }

    @Test
    fun `given Android home monitoring, outer scaffold owns system inset, expected no duplicate safe-area padding`() {
        // Given
        render(false, ConnectivityState.Disconnected())
        // When
        val status = compose.onNodeWithTag("status").fetchSemanticsNode().boundsInRoot
        // Then
        assertEquals(0f, status.top)
        assertEquals(null, childInset)
        compose.onNodeWithText(Localization.string("status_disconnected")).assertIsDisplayed()
    }

    @Test
    fun `given standalone route layout, monitoring is absent, expected toolbar keeps its own native inset`() {
        // Given
        render(false, ConnectivityState.Uninitialized(false))
        // When
        val toolbar = compose.onNodeWithTag("toolbar").fetchSemanticsNode().boundsInRoot
        // Then
        assertEquals(0f, toolbar.top)
        assertEquals(null, childInset)
    }
}
