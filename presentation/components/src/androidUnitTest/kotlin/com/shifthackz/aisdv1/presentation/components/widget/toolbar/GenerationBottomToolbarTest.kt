package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Checks the generation action slot receives its painted parent color for native button cutouts.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GenerationBottomToolbarTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `given generation toolbar, ancestor uses another backing, expected action slot receives toolbar surface`() {
        // Given
        var backing: Color? = null
        val surface = Color(0xFF304050)
        compose.setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    surface = surface,
                    background = Color.White
                )
            ) {
                CompositionLocalProvider(LocalPlatformControlBackgroundColor provides Color.Red) {
                    GenerationBottomToolbar(
                        mode = ServerSource.HORDE,
                        prompt = "",
                        negativePrompt = ""
                    ) {
                        val color = LocalPlatformControlBackgroundColor.current
                        SideEffect { backing = color }
                        Text("Imagine")
                    }
                }
            }
        }
        // When
        compose.waitForIdle()
        // Then
        assertEquals(surface, backing)
    }

    @Test
    fun `given generation toolbar, theme surface changes, expected action backing follows new surface`() {
        // Given
        val surface = mutableStateOf(Color(0xFF304050))
        var backing: Color? = null
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(surface = surface.value)) {
                GenerationBottomToolbar(
                    mode = ServerSource.HORDE,
                    prompt = "",
                    negativePrompt = ""
                ) {
                    val color = LocalPlatformControlBackgroundColor.current
                    SideEffect { backing = color }
                    Text("Imagine")
                }
            }
        }
        // When
        compose.runOnIdle { surface.value = Color(0xFF708090) }
        // Then
        compose.runOnIdle { assertEquals(surface.value, backing) }
    }
}
