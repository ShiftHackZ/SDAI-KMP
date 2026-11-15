@file:OptIn(ExperimentalUuidApi::class)

package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Samples transient display content through the shared lifecycle-aware cache.
 *
 * @param base64 Original content; it is never replaced with sampled bytes.
 * @param edge Measured display pixels; zero defers work until layout.
 * @author Dmitriy Moroz
 */
@Composable
internal fun rememberPreviewImage(base64: String?, edge: Int): ImageBitmap? {
    val revision = remember(ImageSourceIdentity(base64)) { Uuid.random().toString() }
    val loader = LocalGalleryImageLoader.current ?: return null
    val image = rememberGalleryImage(
        loader,
        -1L,
        revision,
        edge = edge,
        transientBase64 = base64.orEmpty()
    ).value
    return (image as? DecodedImageBitmap.Ready)?.image
}
