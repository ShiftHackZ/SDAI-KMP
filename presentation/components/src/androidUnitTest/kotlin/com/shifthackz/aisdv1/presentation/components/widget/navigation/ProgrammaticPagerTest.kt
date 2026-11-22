package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Checks ownership, retained local state and gesture exclusion in the production pager.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h640dp")
class ProgrammaticPagerTest {
    @get:Rule
    val compose = createComposeRule()
    private var selected by mutableIntStateOf(0)
    private var progress by mutableStateOf<Float?>(null)
    private var isParentVisible by mutableStateOf(true)
    private var areControlsVisible by mutableStateOf(true)
    private var isInputEnabled by mutableStateOf(true)
    private val visible = mutableMapOf<Int, Boolean>()
    private val controls = mutableMapOf<Int, Boolean>()
    private val input = mutableMapOf<Int, Boolean>()

    /**
     * Renders actual retained pages under the same ownership locals used by the app shell.
     *
     * @author Dmitriy Moroz
     */
    private fun render() {
        compose.setContent {
            CompositionLocalProvider(
                LocalMviVisibility provides isParentVisible,
                LocalPlatformControlVisibility provides areControlsVisible,
                LocalNavigationInputEnabled provides isInputEnabled,
            ) {
                Box(Modifier.size(320.dp, 400.dp)) {
                    ProgrammaticPager(
                        selected,
                        3,
                        Modifier.testTag("pages"),
                        backProgress = progress
                    ) { page ->
                        var count by rememberSaveable { mutableIntStateOf(0) }
                        val isVisible = LocalMviVisibility.current
                        val hasControls = LocalPlatformControlVisibility.current
                        val canInteract = LocalNavigationInputEnabled.current
                        SideEffect {
                            visible[page] = isVisible; controls[page] = hasControls; input[page] =
                            canInteract
                        }
                        Button(onClick = { count++ }) { Text("Page $page: $count") }
                    }
                }
            }
        }
    }

    @Test
    fun `given peer pages, user swipes body, expected current selection unchanged`() {
        // Given
        render()
        // When
        compose.onNodeWithTag("pages").performTouchInput { swipeLeft() }
        // Then
        compose.onNodeWithText("Page 0: 0").assertIsDisplayed()
        assertEquals(0, selected)
    }

    @Test
    fun `given peer pages, owner selects another page, expected only destination owns visible controls and input`() {
        // Given
        render()
        // When
        compose.runOnIdle { selected = 2 }
        compose.waitForIdle()
        // Then
        compose.onNodeWithText("Page 2: 0").assertIsDisplayed()
        compose.onNodeWithText("Page 0: 0").assertDoesNotExist()
        compose.runOnIdle {
            assertTrue(input[2] == true); assertFalse(input[0] == true); assertFalse(
            visible[0] == true
        ); assertFalse(controls[0] == true)
        }
    }

    @Test
    fun `given retained pages, owner switches away and returns, expected local page state preserved`() {
        // Given
        render()
        compose.onNodeWithText("Page 0: 0").performClick()
        // When
        compose.runOnIdle { selected = 2 }
        compose.waitForIdle()
        compose.runOnIdle { selected = 0 }
        // Then
        compose.onNodeWithText("Page 0: 1").assertIsDisplayed()
    }

    @Test
    fun `given peer pages, parent is covered, expected child visibility controls and input remain disabled`() {
        // Given
        isParentVisible = false
        areControlsVisible = false
        isInputEnabled = false
        render()
        // When
        compose.runOnIdle { selected = 1 }
        compose.waitForIdle()
        // Then
        compose.runOnIdle {
            assertFalse(visible.values.any { it }); assertFalse(controls.values.any { it }); assertFalse(
            input.values.any { it })
        }
    }

    @Test
    fun `given local back preview, gesture is partially dragged, expected neither page accepts content actions`() {
        // Given
        selected = 1
        render()
        // When
        compose.runOnIdle { progress = 0.4f }
        compose.waitForIdle()
        // Then
        compose.runOnIdle { assertFalse(input.values.any { it }) }
        compose.onNodeWithText("Page 1: 0").assertDoesNotExist()
    }

    @Test
    fun `given local back preview, gesture is cancelled, expected selected page input restored`() {
        // Given
        selected = 1
        render()
        compose.runOnIdle { progress = 0.4f }
        compose.waitForIdle()
        // When
        compose.runOnIdle { progress = null }
        // Then
        compose.onNodeWithText("Page 1: 0").assertIsDisplayed()
        compose.runOnIdle { assertTrue(input[1] == true); assertFalse(input[0] == true) }
    }
}
