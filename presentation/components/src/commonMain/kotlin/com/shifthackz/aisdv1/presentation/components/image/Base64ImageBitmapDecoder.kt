package com.shifthackz.aisdv1.presentation.components.image

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decodes the original pixel dimensions for an explicitly opened editor.
 * Dimension admission precedes bitmap rasterization; display previews use the sampled loader.
 * The caller owns background dispatch, encoded-byte admission and decode concurrency.
 *
 * @receiver Original Base64 image, optionally prefixed by a data URI.
 *
 * @param maxPixels Maximum admitted original pixel count.
 *
 * @return Full-resolution image, or null for invalid content or rejected dimensions.
 *
 * @author Dmitriy Moroz
 */
internal expect fun String.decodeBase64ImageBitmap(maxPixels: Long): ImageBitmap?
