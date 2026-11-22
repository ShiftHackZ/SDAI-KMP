package com.shifthackz.aisdv1.presentation.appupdate

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Ensures a native-only offer cannot open Play UI in a Full installation.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class UnsupportedNativeAppUpdateHostTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `given Full launch host, an unsupported native offer is received, expected NoOp consumption and no custom dialog`() {
        // Given
        val state = AppUpdateLaunchStateImpl { AppUpdate("221", null) }

        // When
        compose.setContent { MaterialTheme { AppUpdateHost(state) } }
        compose.waitForIdle()

        // Then
        assertNull(state.update.value)
        compose.onNodeWithText("Update available").assertDoesNotExist()
    }
}
