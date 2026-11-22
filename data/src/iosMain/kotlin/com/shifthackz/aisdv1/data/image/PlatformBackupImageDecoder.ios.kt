@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.data.image

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
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
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFNumberGetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.CoreFoundation.kCFNumberSInt64Type
import platform.CoreGraphics.CGImageGetBytesPerRow
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRelease
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceGetStatusAtIndex
import platform.ImageIO.CGImageSourceRef
import platform.ImageIO.kCGImagePropertyPixelHeight
import platform.ImageIO.kCGImagePropertyPixelWidth
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceShouldCache
import platform.ImageIO.kCGImageSourceShouldCacheImmediately
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.ImageIO.kCGImageStatusComplete

/**
 * Reads uncached ImageIO properties before creating only a bounded thumbnail for content validation.
 *
 * @author Dmitriy Moroz
 */
internal actual class PlatformBackupImageDecoder actual constructor() : BackupImageDecoder {
    /**
     * Reads uncached ImageIO pixel dimensions and releases the metadata dictionary.
     *
     * @param bytes Encoded image admitted by the shared backup byte budget.
     * @author Dmitriy Moroz
     */
    actual override fun dimensions(bytes: ByteArray): Pair<Long, Long> =
        withSource(bytes) { source ->
            val properties = requireNotNull(
                CGImageSourceCopyPropertiesAtIndex(
                    source,
                    0u,
                    null
                )
            ) { "Invalid backup image properties" }
            try {
                dimension(properties, kCGImagePropertyPixelWidth) to dimension(
                    properties,
                    kCGImagePropertyPixelHeight
                )
            } finally {
                CFRelease(properties)
            }
        }

    /**
     * Validates a bounded ImageIO thumbnail and releases its native resources on every exit.
     *
     * @param bytes Encoded image whose original dimensions passed admission.
     * @param maxEdge Maximum sample width and height in pixels.
     * @author Dmitriy Moroz
     */
    actual override fun validateSample(bytes: ByteArray, maxEdge: Int) =
        withSource(bytes) { source ->
            val options = imageOptions(maxEdge)
            try {
                val image = requireNotNull(
                    CGImageSourceCreateThumbnailAtIndex(
                        source,
                        0u,
                        options
                    )
                ) { "Invalid backup image content" }
                try {
                    val width = CGImageGetWidth(image).toLong()
                    val height = CGImageGetHeight(image).toLong()
                    val rowBytes = CGImageGetBytesPerRow(image).toLong()
                    require(width in 1..maxEdge.toLong() && height in 1..maxEdge.toLong()) { "Backup sample exceeds bounds" }
                    require(rowBytes > 0 && rowBytes <= maxEdge.toLong() * maxEdge * 8 / height) { "Backup sample exceeds byte budget" }
                    require(
                        CGImageSourceGetStatusAtIndex(
                            source,
                            0u
                        ) == kCGImageStatusComplete
                    ) { "Incomplete backup image content" }
                } finally {
                    CGImageRelease(image)
                }
            } finally {
                CFRelease(options)
            }
        }

    /**
     * Creates an owned CF dictionary with native CFString keys and retained CFBoolean/CFNumber values.
     *
     * @param maxEdge Thumbnail edge, or null for metadata-only source options with pixel caching disabled.
     * @author Dmitriy Moroz
     */
    private fun imageOptions(maxEdge: Int?): CFDictionaryRef = memScoped {
        val options = requireNotNull(
            CFDictionaryCreateMutable(
                kCFAllocatorDefault,
                0,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr,
            )
        )
        try {
            if (maxEdge == null) {
                CFDictionarySetValue(options, kCGImageSourceShouldCache, kCFBooleanFalse)
            } else {
                CFDictionarySetValue(
                    options,
                    kCGImageSourceCreateThumbnailFromImageAlways,
                    kCFBooleanTrue
                )
                CFDictionarySetValue(options, kCGImageSourceShouldCacheImmediately, kCFBooleanTrue)
                val edge = alloc<IntVar>()
                edge.value = maxEdge
                val number =
                    requireNotNull(CFNumberCreate(kCFAllocatorDefault, kCFNumberIntType, edge.ptr))
                try {
                    CFDictionarySetValue(options, kCGImageSourceThumbnailMaxPixelSize, number)
                } finally {
                    CFRelease(number)
                }
            }
            options
        } catch (failure: Throwable) {
            CFRelease(options)
            throw failure
        }
    }

    /**
     * Extracts an integral native dimension without rasterizing the encoded image.
     *
     * @param properties ImageIO metadata dictionary retained by the caller.
     * @param key Native pixel width or height property identifier.
     * @author Dmitriy Moroz
     */
    private fun dimension(properties: CFDictionaryRef, key: CFStringRef?): Long = memScoped {
        val number = requireNotNull(
            CFDictionaryGetValue(
                properties,
                key
            )
        ) { "Missing backup image dimension" }
        val result = alloc<LongVar>()
        require(
            CFNumberGetValue(
                number.reinterpret(),
                kCFNumberSInt64Type,
                result.ptr
            )
        ) { "Invalid backup image dimension" }
        result.value
    }

    /**
     * Creates a metadata-only source with deferred pixel caching, releasing every native owner on failure.
     *
     * @param bytes Bounded encoded image retained by the caller.
     * @param block Metadata probing or sampled validation before native source release.
     * @author Dmitriy Moroz
     */
    private fun <T> withSource(bytes: ByteArray, block: (CGImageSourceRef) -> T): T {
        require(bytes.isNotEmpty()) { "Empty backup image" }
        val data = requireNotNull(bytes.usePinned {
            CFDataCreate(
                kCFAllocatorDefault,
                it.addressOf(0).reinterpret(),
                bytes.size.toLong()
            )
        })
        try {
            val options = imageOptions(null)
            val source = try {
                requireNotNull(
                    CGImageSourceCreateWithData(
                        data,
                        options
                    )
                ) { "Invalid backup image" }
            } finally {
                CFRelease(options)
            }
            try {
                return block(source)
            } finally {
                CFRelease(source)
            }
        } finally {
            CFRelease(data)
        }
    }
}
