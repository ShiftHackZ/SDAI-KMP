package com.shifthackz.aisdv1.presentation.configuration.setup.platform

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.darwin.NSObject


@Composable
/**
 * Presents the native iOS folder picker and returns the selected source path to setup.
 * The remembered delegate retains acquired security-scoped URLs for subsequent model import.
 *
 * @param text Localized folder-selection button label.
 * @param onPathSelected Receives the chosen folder's filesystem path.
 * @param modifier Size and placement of the folder-selection button.
 *
 * @author Dmitriy Moroz
 */
internal actual fun ServerSetupLocalPathPickerButton(
    text: String,
    onPathSelected: (String) -> Unit,
    modifier: Modifier,
) {
    val picker = remember { IosLocalPathPicker() }
    OutlinedButton(
        modifier = modifier,
        onClick = { picker.present(onPathSelected) },
    ) {
        Text(text = text)
    }
}

/**
 * Enables local model-folder setup through the native iOS document picker.
 *
 * @author Dmitriy Moroz
 */
internal actual fun isLocalGenerationSetupAvailable(): Boolean = true

/**
 * Retains the document-picker delegate, pending selection callback and acquired security-scoped URLs.
 *
 * @author Dmitriy Moroz
 */
private class IosLocalPathPicker : NSObject(), UIDocumentPickerDelegateProtocol {

    private var onPathSelected: ((String) -> Unit)? = null
    private val retainedSecurityScopedUrls = mutableListOf<NSURL>()

    fun present(onPathSelected: (String) -> Unit) {
        val presenter = UIApplication.sharedApplication
            .rootViewController()
            ?.topMostPresentedViewController()
            ?: return
        this.onPathSelected = onPathSelected
        val controller = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeFolder),
            asCopy = false,
        )
        controller.delegate = this
        controller.allowsMultipleSelection = false
        controller.modalPresentationStyle = UIModalPresentationFullScreen
        presenter.presentViewController(
            viewControllerToPresent = controller,
            animated = true,
            completion = null,
        )
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        if (url.startAccessingSecurityScopedResource()) {
            retainedSecurityScopedUrls.add(url)
        }
        url.path?.let { path -> onPathSelected?.invoke(path) }
        onPathSelected = null
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPathSelected = null
    }
}

private fun UIApplication.rootViewController(): UIViewController? =
    keyWindow?.rootViewController
        ?: windows
            .filterIsInstance<UIWindow>()
            .firstOrNull(UIWindow::isKeyWindow)
            ?.rootViewController
        ?: windows
            .filterIsInstance<UIWindow>()
            .firstOrNull()
            ?.rootViewController

private fun UIViewController.topMostPresentedViewController(): UIViewController =
    presentedViewController?.topMostPresentedViewController() ?: this
