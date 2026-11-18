package com.shifthackz.aisdv1.presentation.components.utils

import android.content.Context
import java.io.File

/**
 * Hands an Android problem report to the email chooser without sending it automatically.
 *
 * @author Dmitriy Moroz
 */
interface ReportProblemEmailComposer {
    /**
     * Attaches the current log and an explicitly selected diagnostic capture when those files exist.
     *
     * @param context Context used to launch the chooser.
     * @param selectedCapture Optional capture selected by the diagnostic share action; never discovered implicitly.
     * @author Dmitriy Moroz
     */
    fun invoke(context: Context, selectedCapture: File? = null)
}
