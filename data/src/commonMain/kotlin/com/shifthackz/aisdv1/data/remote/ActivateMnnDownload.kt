package com.shifthackz.aisdv1.data.remote

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Owns a received MNN archive through activation, archive removal and session shutdown.
 * A terminal path is returned only after both cleanup operations succeed.
 *
 * @param activate Validates and atomically installs the archive, returning its owned model path.
 * @param removeArchive Removes the download attempt directory and propagates storage failures.
 * @param closeSession Finalizes the exact NSURLSession after its archive is no longer needed.
 *
 * @author Dmitriy Moroz
 */
internal suspend fun activateMnnDownload(
    activate: suspend () -> String,
    removeArchive: () -> Unit,
    closeSession: () -> Unit,
): String {
    var modelPath: String? = null
    var primaryFailure: Throwable? = null
    try {
        currentCoroutineContext().ensureActive()
        modelPath = activate()
        currentCoroutineContext().ensureActive()
    } catch (failure: Throwable) {
        primaryFailure = failure
    }
    for (cleanup in listOf(removeArchive, closeSession)) {
        try {
            cleanup()
        } catch (failure: Throwable) {
            if (primaryFailure == null) primaryFailure = failure else primaryFailure.addSuppressed(
                failure
            )
        }
    }
    primaryFailure?.let { throw it }
    return checkNotNull(modelPath)
}
