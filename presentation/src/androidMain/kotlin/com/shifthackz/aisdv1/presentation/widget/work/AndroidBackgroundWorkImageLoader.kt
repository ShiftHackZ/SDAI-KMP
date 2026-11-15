@file:OptIn(ExperimentalUuidApi::class)

package com.shifthackz.aisdv1.presentation.widget.work

import androidx.compose.ui.graphics.ImageBitmap
import com.shifthackz.aisdv1.presentation.widget.image.GalleryImageLoader
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Provides a bounded compatibility preview for consumers outside the measured Compose widget.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidBackgroundWorkImageLoader(
    /**
     * Shared sampled image owner; normal widget rendering measures its own visible slot.
     *
     * @author Dmitriy Moroz
     */
    private val images: GalleryImageLoader,
) : BackgroundWorkImageLoader {

    /**
     * Loads SDAI data through `load`.
     *
     * @param base64 Base64 image payload used by the operation.
     * @return Result produced by `load`.
     * @author Dmitriy Moroz
     */
    override suspend fun load(base64: String): ImageBitmap? =
        images.loadTransient(Uuid.random().toString(), base64, false, 128)
}
