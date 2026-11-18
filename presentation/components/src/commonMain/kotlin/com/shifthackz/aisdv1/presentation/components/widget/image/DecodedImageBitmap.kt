package com.shifthackz.aisdv1.presentation.components.widget.image

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Separates pending, displayable and unavailable bounded image-load outcomes.
 *
 * @author Dmitriy Moroz
 */
@Immutable
sealed interface DecodedImageBitmap {
    /**
     * The requested image has not completed loading or decoding.
     *
     * @author Dmitriy Moroz
     */
    data object Loading : DecodedImageBitmap

    /**
     * A decoded image ready for display; cache eviction must not recycle this bitmap.
     *
     * @param image Decoded bitmap retained while the UI uses it, including full-resolution editing loads.
     *
     * @author Dmitriy Moroz
     */
    data class Ready(val image: ImageBitmap) : DecodedImageBitmap

    /**
     * No image can be displayed: it is missing, replaced, outside admission limits or undecodable.
     *
     * @author Dmitriy Moroz
     */
    data object Unavailable : DecodedImageBitmap
}
