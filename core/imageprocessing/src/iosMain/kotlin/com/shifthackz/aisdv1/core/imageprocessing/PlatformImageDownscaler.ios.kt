@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.core.imageprocessing

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
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFNumberGetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.CGImageRef
import platform.Foundation.getBytes
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.kCGImagePropertyPixelHeight
import platform.ImageIO.kCGImagePropertyPixelWidth
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceShouldCache
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.UIKit.UIImage
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImagePNGRepresentation

/**
 * Uses ImageIO's bounded thumbnail decoder to preserve the whole source without a full-size raster.
 *
 * @author Dmitriy Moroz
 */
internal actual class PlatformImageDownscaler actual constructor() : ImageDownscaler {
    /**
     * Proportionally reduces oversized images and releases all owned Core Foundation resources.
     *
     * @param image Original encoded source in its stored pixel orientation.
     * @param maxWidth Positive maximum output width.
     * @param maxHeight Positive maximum output height.
     * @author Dmitriy Moroz
     */
    actual override fun downscale(image: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
        require(image.isNotEmpty() && maxWidth > 0 && maxHeight > 0)
        val data = requireNotNull(image.usePinned {
            CFDataCreate(kCFAllocatorDefault, it.addressOf(0).reinterpret(), image.size.toLong())
        })
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
                val source = requireNotNull(
                    CGImageSourceCreateWithData(
                        data,
                        options
                    )
                ) { "Invalid input image" }
                try {
                    val properties =
                        requireNotNull(CGImageSourceCopyPropertiesAtIndex(source, 0u, null))
                    val (sourceWidth, sourceHeight) = try {
                        dimension(properties, kCGImagePropertyPixelWidth) to dimension(
                            properties,
                            kCGImagePropertyPixelHeight
                        )
                    } finally {
                        CFRelease(properties)
                    }
                    if (sourceWidth <= maxWidth && sourceHeight <= maxHeight) return image
                    val (width, height) = downscaledImageDimensions(
                        sourceWidth,
                        sourceHeight,
                        maxWidth,
                        maxHeight
                    )
                    CFDictionarySetValue(
                        options,
                        kCGImageSourceCreateThumbnailFromImageAlways,
                        kCFBooleanTrue
                    )
                    memScoped {
                        val edge = alloc<IntVar>()
                        edge.value = maxOf(width, height)
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
                    val scaled =
                        requireNotNull(CGImageSourceCreateThumbnailAtIndex(source, 0u, options))
                    try {
                        require(
                            CGImageGetWidth(scaled) in 1uL..maxWidth.toULong() &&
                                    CGImageGetHeight(scaled) in 1uL..maxHeight.toULong()
                        )
                        return encodeAtSize(scaled, width, height)
                    } finally {
                        CGImageRelease(scaled)
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
    }

    /**
     * Corrects thumbnail rounding to exact proportional integer dimensions before PNG encoding.
     *
     * @param image Bounded native thumbnail owned by the caller.
     * @param width Exact output width.
     * @param height Exact output height.
     * @author Dmitriy Moroz
     */
    private fun encodeAtSize(image: CGImageRef, width: Int, height: Int): ByteArray {
        UIGraphicsBeginImageContextWithOptions(
            CGSizeMake(width.toDouble(), height.toDouble()),
            false,
            1.0
        )
        try {
            UIImage(cGImage = image).drawInRect(
                CGRectMake(
                    0.0,
                    0.0,
                    width.toDouble(),
                    height.toDouble()
                )
            )
            val output = requireNotNull(UIGraphicsGetImageFromCurrentImageContext())
            val png = requireNotNull(UIImagePNGRepresentation(output))
            require(png.length in 1uL..Int.MAX_VALUE.toULong())
            return ByteArray(png.length.toInt()).also { bytes ->
                bytes.usePinned { png.getBytes(it.addressOf(0), png.length) }
            }
        } finally {
            UIGraphicsEndImageContext()
        }
    }

    /**
     * Reads a positive pixel dimension from ImageIO metadata without decoding image pixels.
     *
     * @param properties Source properties owned by the caller.
     * @param key ImageIO width or height property key.
     * @author Dmitriy Moroz
     */
    private fun dimension(properties: CFDictionaryRef, key: CFStringRef?): Int = memScoped {
        val number = requireNotNull(CFDictionaryGetValue(properties, key))
        val result = alloc<IntVar>()
        require(
            CFNumberGetValue(
                number.reinterpret(),
                kCFNumberIntType,
                result.ptr
            ) && result.value > 0
        )
        result.value
    }
}
