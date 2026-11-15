package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest

/**
 * Validates complete image structure and bounded decoded size before host result publication.
 *
 * @author Dmitriy Moroz
 */
internal interface PngImageValidator {
    /**
     * Checks the complete PNG structure and decoded-size policy before result publication.
     * Invalid, truncated, unsupported or over-limit output throws instead of reaching gallery storage.
     *
     * @param bytes Complete PNG bytes, bounded by the file store.
     * @param request Expected engine dimensions and admission policy.
     * @author Dmitriy Moroz
     */
    fun validate(bytes: ByteArray, request: NativeGenerationRequest)
}
