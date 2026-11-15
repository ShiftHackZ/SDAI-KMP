package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.ui.graphics.ImageBitmap
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticContext

/**
 * Admits an explicit full-resolution editing action without changing its pixel coordinates.
 *
 * @author Dmitriy Moroz
 */
interface OriginalImageLoader {
    /**
     * Returns the full original only within the configured encoded/pixel budgets.
     *
     * @param base64 Original source; never a display sample.
     * @param diagnosticContext Optional fixed-label correlation; no image data enters diagnostics.
     * @author Dmitriy Moroz
     */
    suspend fun load(base64: String, diagnosticContext: UiDiagnosticContext? = null): ImageBitmap?
}
