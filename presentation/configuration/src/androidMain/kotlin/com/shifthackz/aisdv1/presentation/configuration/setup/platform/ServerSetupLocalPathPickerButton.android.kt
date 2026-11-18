package com.shifthackz.aisdv1.presentation.configuration.setup.platform

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.shifthackz.aisdv1.core.extensions.getRealPath

/**
 * Opens the existing Android directory picker and returns a resolvable custom-model path.
 *
 * @param modifier Bounds of the picker button.
 * @param text Localized button label.
 * @param onPathSelected Receives the granted directory when its filesystem path is available.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun ServerSetupLocalPathPickerButton(
    text: String,
    onPathSelected: (String) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val uriFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        result.data?.data?.let { uri ->
            context.contentResolver.takePersistableUriPermission(uri, uriFlags)
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri),
            )
            getRealPath(context, documentUri)?.let(onPathSelected)
        }
    }
    OutlinedButton(
        modifier = modifier,
        onClick = {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(uriFlags)
            }
            launcher.launch(intent)
        },
    ) {
        Text(text = text)
    }
}

/**
 * Reports whether the standard custom-model flow can read external directories on this Android version.
 *
 * @author Dmitriy Moroz
 */
internal actual fun isLocalGenerationSetupAvailable(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
