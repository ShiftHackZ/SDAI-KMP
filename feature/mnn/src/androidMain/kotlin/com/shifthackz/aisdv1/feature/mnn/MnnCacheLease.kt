package com.shifthackz.aisdv1.feature.mnn

import java.io.Closeable
import java.io.File

/**
 * Holds the cross-process cache lock until the native attempt and its files are released.
 *
 * @author Dmitriy Moroz
 */
internal interface MnnCacheLease : Closeable {
    /**
     * App-private UUID directory containing only this attempt's disposable files.
     *
     * @author Dmitriy Moroz
     */
    val directory: File
}
