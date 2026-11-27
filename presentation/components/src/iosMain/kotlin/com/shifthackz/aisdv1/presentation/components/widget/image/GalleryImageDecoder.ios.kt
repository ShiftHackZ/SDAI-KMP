@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceShouldCache
import platform.ImageIO.kCGImageSourceShouldCacheImmediately
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Image as SkiaImage
import kotlin.io.encoding.Base64

/**
 * Samples through ImageIO, then copies bounded RGBA pixels directly into Skia without PNG recompression.
 *
 * @param base64 Budgeted encoded source.
 * @param maxDimension Positive maximum decoded edge.
 * @author Dmitriy Moroz
 */
internal actual fun decodeSampledGalleryImage(base64: String, maxDimension: Int): ImageBitmap? =
    try {
        require(maxDimension in 1..2048)
        val bytes = Base64.decode(base64.substringAfter("base64,", base64))
        if (bytes.isEmpty()) return null
        val data = bytes.usePinned {
            CFDataCreate(kCFAllocatorDefault, it.addressOf(0).reinterpret(), bytes.size.toLong())
        } ?: return null
        try {
            val options = requireNotNull(
                CFDictionaryCreateMutable(
                    kCFAllocatorDefault,
                    0,
                    kCFTypeDictionaryKeyCallBacks.ptr,
                    kCFTypeDictionaryValueCallBacks.ptr
                )
            )
            try {
                CFDictionarySetValue(options, kCGImageSourceShouldCache, kCFBooleanFalse)
                val source = CGImageSourceCreateWithData(data, options) ?: return null
                try {
                    CFDictionarySetValue(
                        options,
                        kCGImageSourceCreateThumbnailFromImageAlways,
                        kCFBooleanTrue
                    )
                    CFDictionarySetValue(
                        options,
                        kCGImageSourceCreateThumbnailWithTransform,
                        kCFBooleanTrue
                    )
                    CFDictionarySetValue(
                        options,
                        kCGImageSourceShouldCacheImmediately,
                        kCFBooleanTrue
                    )
                    memScoped {
                        val edge = alloc<IntVar>()
                        edge.value = maxDimension
                        val number = requireNotNull(
                            CFNumberCreate(
                                kCFAllocatorDefault,
                                kCFNumberIntType,
                                edge.ptr
                            )
                        )
                        try {
                            CFDictionarySetValue(
                                options,
                                kCGImageSourceThumbnailMaxPixelSize,
                                number
                            )
                        } finally {
                            CFRelease(number)
                        }
                    }
                    val image =
                        CGImageSourceCreateThumbnailAtIndex(source, 0u, options) ?: return null
                    try {
                        val width = CGImageGetWidth(image).toInt()
                        val height = CGImageGetHeight(image).toInt()
                        require(width in 1..maxDimension && height in 1..maxDimension)
                        galleryBitmapFromPixels(image, width, height)
                    } finally {
                        CGImageRelease(image)
                    }
                } finally {
                    CFRelease(source)
                }
            } finally {
                CFRelease(options)
            }
        } finally {
            CFRelease(data)
        }
    } catch (_: Exception) {
        null
    }

/**
 * Converts the already bounded thumbnail to owned premultiplied RGBA pixels for the Compose renderer.
 *
 * @param image Native thumbnail owned by the caller.
 * @param width Admitted thumbnail width.
 * @param height Admitted thumbnail height.
 * @author Dmitriy Moroz
 */
private fun galleryBitmapFromPixels(image: CGImageRef, width: Int, height: Int): ImageBitmap {
    val rowBytes = width * 4
    val pixels = ByteArray(rowBytes * height)
    val colorSpace = requireNotNull(CGColorSpaceCreateDeviceRGB())
    try {
        pixels.usePinned {
            val context = requireNotNull(
                CGBitmapContextCreate(
                    it.addressOf(0),
                    width.toULong(),
                    height.toULong(),
                    8u,
                    rowBytes.toULong(),
                    colorSpace,
                    CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value or kCGBitmapByteOrder32Big
                )
            )
            try {
                CGContextDrawImage(
                    context,
                    CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()),
                    image
                )
            } finally {
                CGContextRelease(context)
            }
        }
    } finally {
        CGColorSpaceRelease(colorSpace)
    }
    return SkiaImage.makeRaster(
        ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL), pixels, rowBytes
    ).toComposeImageBitmap()
}
