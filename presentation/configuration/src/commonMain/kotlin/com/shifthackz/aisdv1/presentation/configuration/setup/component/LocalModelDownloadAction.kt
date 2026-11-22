package com.shifthackz.aisdv1.presentation.configuration.setup.component

import com.shifthackz.aisdv1.presentation.components.widget.action.SecondaryActionButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.localCatalogType

/**
 * Animates the batch action and its footer gap together, keeping Setup at a fixed bottom position.
 * Hides the start action during single-model work of this provider and when no catalog models are missing.
 * The current batch always retains its cancellation action until work terminates.
 *
 * @param state Current provider catalog and application-owned batch status.
 * @param processIntent Dispatches start, replacement confirmation or cancellation through MVI.
 * @param modifier Layout modifiers for the secondary action.
 * @author Dmitriy Moroz
 */
@Composable
internal fun LocalModelDownloadAction(
    state: ServerSetupState,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val action = state.toLocalModelDownloadActionState()
    val isCurrentBatch = action.isCancelling
    AnimatedVisibility(
        visible = action.isVisible,
        modifier = modifier,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            SecondaryActionButton(
                isEnabled = action.isEnabled,
                onClick = { processIntent(if (isCurrentBatch) ServerSetupIntent.CancelModelDownloads else ServerSetupIntent.DownloadAllModels) },
            ) {
                Icon(
                    imageVector = if (isCurrentBatch) Icons.Outlined.StopCircle else Icons.Outlined.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(Localization.string(if (isCurrentBatch) "download_cancel_all" else "download_all"))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Maps current catalogs and worker ownership to the footer's visibility and click behavior.
 * Keeps another provider's action enabled so it can open the switch confirmation.
 *
 * @author Dmitriy Moroz
 */
internal fun ServerSetupState.toLocalModelDownloadActionState(): LocalModelDownloadActionState {
    val type = mode.localCatalogType ?: return LocalModelDownloadActionState(false, false, false)
    val active = modelDownloadBatches.filter { it.isRunning }
    val isWorkRunning = active.isNotEmpty()
    val isCurrentBatch = active.any { it.isBatch && it.type == type }
    val hasMissingModels =
        localModels.any { !it.isCustom && !it.downloaded && it.sources.isNotEmpty() }
    val canStart =
        localCatalogStatuses[type] == LocalCatalogStatus.READY && !isWorkRunning && hasMissingModels
    val hasSingleDownload = active.any { !it.isBatch && it.type == type }
    return LocalModelDownloadActionState(
        isVisible = isCurrentBatch || (hasMissingModels && !hasSingleDownload),
        isEnabled = !loadingConfiguration && (isWorkRunning || canStart),
        isCancelling = isCurrentBatch,
    )
}
