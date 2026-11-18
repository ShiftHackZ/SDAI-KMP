package com.shifthackz.aisdv1.presentation.components.widget.image

/**
 * Admission limits for explicit editing, pixel-coordinate transforms and normalized picker conversion.
 * Values are configured in DI and require real-device calibration.
 *
 * @param encodedChars Maximum Base64 source characters before decoding and normalized picker output characters.
 * @param maxPixels Maximum original editor, sampled picker or mask pixel allocation.
 *
 * @author Dmitriy Moroz
 */
data class OriginalImageBudget(val encodedChars: Int, val maxPixels: Long) {
    init {
        require(encodedChars > 0 && maxPixels > 0)
    }
}
