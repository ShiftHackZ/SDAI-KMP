package com.shifthackz.aisdv1.domain.entity.backup

/**
 * Native document outcomes tied to the staged source that launched their picker.
 * @author Dmitriy Moroz
 */
sealed interface BackupExportCompletion {
    /** App-owned staged identity captured before picker presentation. @author Dmitriy Moroz */
    val handle: String

    /**
     * Android supplied a write-granted output document for this source.
     * @param document Opaque destination associated with this export.
     * @param handle App-owned source identity retained by the native launcher.
     * @author Dmitriy Moroz
     */
    data class DestinationSelected(val document: String, override val handle: String) :
        BackupExportCompletion

    /**
     * UIKit confirmed this source was copied to its selected destination.
     * @param handle App-owned source identity retained by the native launcher.
     * @author Dmitriy Moroz
     */
    data class NativeCompleted(override val handle: String) : BackupExportCompletion

    /**
     * This source's native picker was canceled before destination copying.
     * @param handle App-owned source identity retained by the native launcher.
     * @author Dmitriy Moroz
     */
    data class Canceled(override val handle: String) : BackupExportCompletion
}
