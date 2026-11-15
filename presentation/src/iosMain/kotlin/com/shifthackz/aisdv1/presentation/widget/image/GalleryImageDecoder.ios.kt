package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.kCFAllocatorDefault
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.ImageIO.kCGImageSourceShouldCacheImmediately
import platform.CoreGraphics.CGImageRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSDictionary
import platform.Foundation.NSNumber
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import org.jetbrains.skia.Image as SkiaImage

/**
 * ImageIO creates a thumbnail without decoding an original-size UIKit bitmap.
 *
 * @param base64 Budgeted encoded source.
 * @param maxDimension Maximum decoded edge.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun decodeSampledGalleryImage(base64: String, maxDimension: Int): ImageBitmap? =
    try {
        val bytes = Base64.decode(base64.substringAfter("base64,", base64))
        if (bytes.isEmpty()) return null
        val data = bytes.usePinned {
            CFDataCreate(
                kCFAllocatorDefault,
                it.addressOf(0).reinterpret(),
                bytes.size.toLong()
            )
        } ?: return null
        try {
            val source = CGImageSourceCreateWithData(data, null) ?: return null
            try {
                val options = mapOf(
                    kCGImageSourceCreateThumbnailFromImageAlways to true,
                    kCGImageSourceThumbnailMaxPixelSize to maxDimension,
                    kCGImageSourceShouldCacheImmediately to true,
                )
                val dictionary = CFBridgingRetain(options)
                try {
                    val image =
                        CGImageSourceCreateThumbnailAtIndex(source, 0u, dictionary?.reinterpret())
                            ?: return null
                    try {
                        val png =
                            UIImagePNGRepresentation(UIImage.imageWithCGImage(image)) ?: return null
                        val output = ByteArray(png.length.toInt())
                        output.usePinned { memcpy(it.addressOf(0), png.bytes, png.length) }
                        SkiaImage.makeFromEncoded(output).toComposeImageBitmap()
                    } finally {
                        CGImageRelease(image)
                    }
                } finally {
                    if (dictionary != null) CFRelease(dictionary)
                }
            } finally {
                CFRelease(source)
            }
        } finally {
            CFRelease(data)
        }
    } catch (_: Exception) {
        null
    }
