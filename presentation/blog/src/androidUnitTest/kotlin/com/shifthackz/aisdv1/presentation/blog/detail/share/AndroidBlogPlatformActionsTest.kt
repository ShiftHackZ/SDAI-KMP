package com.shifthackz.aisdv1.presentation.blog.detail.share

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Checks the system chooser payload emitted by the Android blog sharing adapter.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AndroidBlogPlatformActionsTest {

    @Test
    fun `given Android blog share adapter and article text, share is requested, expected chooser contains both lines as plain text`() {
        // Given
        val application = RuntimeEnvironment.getApplication()
        val actions = AndroidBlogPlatformActions(application)
        val text = "SDAI 1.3.0\nhttps://sdai.moroz.cc/blog/1.3.0-release/"

        // When
        val opened = actions.sharePost(text)
        val chooser = shadowOf(application).nextStartedActivity

        // Then
        assertTrue(opened)
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        val send = requireNotNull(chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT))
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertEquals(text, send.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun `given Android blog share adapter and unavailable chooser, share is requested, expected failure reported to reader`() {
        // Given
        val context = object : ContextWrapper(RuntimeEnvironment.getApplication()) {
            override fun startActivity(intent: Intent) = throw ActivityNotFoundException()
        }
        val actions = AndroidBlogPlatformActions(context)

        // When
        val opened = actions.sharePost("SDAI 1.3.0\nhttps://sdai.moroz.cc/blog/1.3.0-release/")

        // Then
        assertFalse(opened)
    }
}
