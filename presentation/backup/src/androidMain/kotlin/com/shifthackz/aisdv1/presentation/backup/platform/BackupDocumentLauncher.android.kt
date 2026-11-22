package com.shifthackz.aisdv1.presentation.backup.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport

/**
 * Uses Android Storage Access Framework without broad storage permissions or retained external ownership.
 *
 * @param onPicked Receives a read-granted document URI or neutral cancellation.
 * @param onExported Receives a destination URI; file writing remains a domain operation.
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberBackupDocumentLauncher(
    onPicked: (String?) -> Unit,
    onExported: (String?, Boolean, String) -> Unit
): BackupDocumentLauncher {
    val picked = rememberUpdatedState(onPicked)
    val exported = rememberUpdatedState(onExported)
    var exportHandle by rememberSaveable { mutableStateOf("") }
    val open =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { picked.value(it?.toString()) }
    val save =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
            exported.value(
                it?.toString(),
                false,
                exportHandle
            )
        }
    return remember(open, save) {
        object : BackupDocumentLauncher {
            override fun open() {
                open.launch(arrayOf("application/octet-stream", "*/*"))
            }

            override fun export(backup: BackupExport) {
                exportHandle = backup.handle
                save.launch(backup.filename)
            }
        }
    }
}
