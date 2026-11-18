package com.shifthackz.aisdv1.presentation.logger

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.shifthackz.aisdv1.core.sharing.shareText

/**
 * Copies log snapshots to the Android clipboard or launches the native text share flow.
 *
 * @param context Provides clipboard access and the activity-launch context for sharing.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidLoggerPlatformActions(

    private val context: Context,
) : LoggerPlatformActions {
    /**
     * Copies the supplied snapshot using the application log clipboard label.
     *
     * @param text Log snapshot to copy.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun copyLogs(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, text))
    }

    /**
     * Opens Android text sharing for the supplied snapshot.
     *
     * @param text Log snapshot to share.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun shareLogs(text: String) {
        context.shareText(text)
    }


    private companion object {

        const val CLIP_LABEL = "SDAI logs"
    }
}
