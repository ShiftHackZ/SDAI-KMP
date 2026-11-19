package com.shifthackz.aisdv1.presentation.backup.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject

/**
 * Retains UIKit delegate callbacks for import/export and treats picker cancellation as neutral.
 * @param onPicked Receives a sandbox-copy URL; no persistent security-scoped grant is retained.
 * @param onExported Receives true only after UIKit confirms completed export.
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberBackupDocumentLauncher(
    onPicked: (String?) -> Unit,
    onExported: (String?, Boolean, String) -> Unit
): BackupDocumentLauncher {
    val picked = rememberUpdatedState(onPicked)
    val exported = rememberUpdatedState(onExported)
    val launcher =
        remember {
            IosBackupDocumentLauncher(
                { picked.value(it) },
                { hasExported, handle -> exported.value(null, hasExported, handle) })
        }
    DisposableEffect(launcher) { onDispose { launcher.release() } }
    return launcher
}

/**
 * Owns the native modal and delegate until its result is delivered.
 * @param onPicked Import result callback receiving only a sandbox-copy document reference.
 * @param onExported Export success callback, false for cancellation.
 * @author Dmitriy Moroz
 */
private class IosBackupDocumentLauncher(
    private val onPicked: (String?) -> Unit,
    private val onExported: (Boolean, String) -> Unit,
) : BackupDocumentLauncher {
    private val delegate = BackupDocumentDelegate(::onDocumentsPicked, ::onPickerCanceled)
    private var picker: UIDocumentPickerViewController? = null
    private var exportHandle: String = ""
    private var isExporting = false

    override fun open() {
        isExporting = false
        present(
            UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeData),
                asCopy = true
            )
        )
    }

    override fun export(backup: BackupExport) {
        exportHandle = backup.handle
        isExporting = true
        val url = NSURL.URLWithString(backup.document)
        if (url == null) {
            onExported(false, exportHandle); return
        }
        present(UIDocumentPickerViewController(forExportingURLs = listOf(url), asCopy = true))
    }

    private fun present(controller: UIDocumentPickerViewController) {
        val application = UIApplication.sharedApplication
        val root = application.keyWindow?.rootViewController
            ?: application.windows.filterIsInstance<UIWindow>()
                .firstOrNull { it.isKeyWindow() }?.rootViewController
        val presenter = root?.topmost()
        if (presenter == null) {
            if (isExporting) onExported(false, exportHandle) else onPicked(null); return
        }
        picker = controller
        controller.delegate = delegate
        controller.allowsMultipleSelection = false
        presenter.presentViewController(controller, true, null)
    }

    private fun onDocumentsPicked(didPickDocumentsAtURLs: List<*>) {
        if (isExporting) onExported(true, exportHandle)
        else onPicked((didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.absoluteString)
        picker?.delegate = null
        picker = null
    }

    private fun onPickerCanceled() {
        if (isExporting) onExported(false, exportHandle) else onPicked(null)
        picker?.delegate = null
        picker = null
    }

    fun release() {
        picker?.delegate = null
        picker?.dismissViewControllerAnimated(false, null)
        picker = null
    }

    private fun UIViewController.topmost(): UIViewController =
        presentedViewController?.topmost() ?: this
}

/**
 * Bridges Objective-C picker events without mixing Objective-C and Kotlin interface inheritance.
 * @param onPicked Selected sandbox document URLs or completed export destinations.
 * @param onCanceled Neutral picker cancellation.
 * @author Dmitriy Moroz
 */
private class BackupDocumentDelegate(
    private val onPicked: (List<*>) -> Unit,
    private val onCanceled: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) = onPicked(didPickDocumentsAtURLs)

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) =
        onCanceled()
}
