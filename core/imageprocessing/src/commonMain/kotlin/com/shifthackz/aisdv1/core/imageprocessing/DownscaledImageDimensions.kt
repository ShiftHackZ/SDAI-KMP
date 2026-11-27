package com.shifthackz.aisdv1.core.imageprocessing

/**
 * Fits both pixel edges inside the bounds without enlarging smaller images.
 * Integer ratios keep the limiting edge exact and round the other edge down by less than one pixel.
 *
 * @param width Positive source width.
 * @param height Positive source height.
 * @param maxWidth Positive maximum output width.
 * @param maxHeight Positive maximum output height.
 * @return Proportional output width and height, each at least one pixel.
 * @author Dmitriy Moroz
 */
internal fun downscaledImageDimensions(
    width: Int,
    height: Int,
    maxWidth: Int,
    maxHeight: Int
): Pair<Int, Int> {
    require(width > 0 && height > 0 && maxWidth > 0 && maxHeight > 0)
    if (width <= maxWidth && height <= maxHeight) return width to height
    return if (width.toLong() * maxHeight >= height.toLong() * maxWidth) {
        maxWidth to (height.toLong() * maxWidth / width).toInt().coerceAtLeast(1)
    } else {
        (width.toLong() * maxHeight / height).toInt().coerceAtLeast(1) to maxHeight
    }
}
