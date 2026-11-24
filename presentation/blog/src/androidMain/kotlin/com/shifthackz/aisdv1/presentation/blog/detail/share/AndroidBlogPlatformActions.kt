package com.shifthackz.aisdv1.presentation.blog.detail.share

import android.content.ActivityNotFoundException
import android.content.Context
import com.shifthackz.aisdv1.core.sharing.shareText

/**
 * Sends the article title and link through Android's text share chooser.
 *
 * @param context Application context used to launch the chooser.
 * @author Dmitriy Moroz
 */
internal class AndroidBlogPlatformActions(private val context: Context) : BlogPlatformActions {
    override fun sharePost(text: String): Boolean = try {
        context.shareText(text)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
