@file:Suppress("DEPRECATION")

package com.shifthackz.aisdv1.data.gateway.mediastore

import android.content.Context
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.log.debugLog
import com.shifthackz.aisdv1.data.export.MediaStoreImageFormatResolver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.core.common.extensions.shouldUseNewMediaStore as defaultShouldUseNewMediaStore

/**
 * Selects the platform media writer while sharing the same encoded-image format contract.
 *
 * @param context Application context used by the modern MediaStore writer.
 * @param fileProviderDescriptor FileProvider authority for exported-folder navigation.
 * @param formatResolver Signature classifier injected into either platform writer.
 * @param shouldUseNewMediaStore SDK routing predicate; defaults to Android 12L and later.
 * @author Dmitriy Moroz
 */
internal class MediaStoreGatewayFactory(
    private val context: Context,
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val formatResolver: MediaStoreImageFormatResolver,
    private val shouldUseNewMediaStore: () -> Boolean = ::defaultShouldUseNewMediaStore,
) {

    /**
     * Creates the writer supported by the current SDK without opening storage or exporting data.
     *
     * @return A MediaStore writer on Android 12L and later, or the existing filesystem writer below it.
     * @author Dmitriy Moroz
     */
    operator fun invoke(): MediaStoreGateway {
        if (shouldUseNewMediaStore()) {
            debugLog("Using Android 12L and higher implementation for MediaStore")
            return MediaStoreGatewayImpl(context, fileProviderDescriptor, formatResolver)
        }
        debugLog("Using deprecated implementation for MediaStore")
        return MediaStoreGatewayOldImpl(formatResolver)
    }
}
