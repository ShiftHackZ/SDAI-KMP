package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.ui.graphics.ImageBitmap
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticContext
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticOperation
import com.shifthackz.aisdv1.presentation.screen.txt2img.decodeBase64ImageBitmap
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Serializes explicit editing decodes separately from sampled display work.
 *
 * @param budget Encoded source and full-resolution pixel admission.
 * @param dispatchers Off-main decode context.
 * @author Dmitriy Moroz
 */
internal class BoundedOriginalImageLoader(
    private val budget: OriginalImageBudget,
    private val dispatchers: DispatchersProvider
) : OriginalImageLoader {
    private val permit = Semaphore(1)

    /**
     * Decodes one full-resolution editing image off main under encoded and pixel admission limits.
     * Decodes are serialized; rejected or undecodable input returns null.
     *
     * @param base64 Full original retained by the edit owner.
     * @param diagnosticContext Optional fixed-label timing context, never retained.
     * @author Dmitriy Moroz
     */
    override suspend fun load(
        base64: String,
        diagnosticContext: UiDiagnosticContext?
    ): ImageBitmap? = permit.withPermit {
        if (base64.length > budget.encodedChars) return@withPermit null
        withContext(dispatchers.io) {
            val span = diagnosticContext?.let {
                it.diagnostics.beginTrace(
                    it.owner,
                    it.subtree,
                    UiDiagnosticOperation.IMAGE_DECODE
                )
            }
            try {
                base64.decodeBase64ImageBitmap(budget.maxPixels)
            } finally {
                diagnosticContext?.diagnostics?.finishTrace(span)
            }
        }
    }
}
