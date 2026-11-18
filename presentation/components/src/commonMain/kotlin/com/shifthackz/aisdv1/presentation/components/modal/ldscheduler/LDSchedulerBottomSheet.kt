package com.shifthackz.aisdv1.presentation.components.modal.ldscheduler

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.presentation.components.service.debug.mapToUi
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem

/**
 * Lists local ONNX dispatcher choices using the shared scheduler labels.
 *
 * @param modifier Layout modifiers for the scheduler list.
 * @param currentScheduler Current local ONNX dispatcher selection.
 * @param onSelected Reports the chosen scheduler; persistence and sheet dismissal remain caller-owned.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun LDSchedulerBottomSheet(
    modifier: Modifier = Modifier,
    currentScheduler: SchedulersToken = SchedulersToken.COMPUTATION,
    onSelected: (SchedulersToken) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        SchedulersToken.entries.forEach { scheduler ->
            SettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                selected = scheduler == currentScheduler,
                text = scheduler.mapToUi(),
                showChevron = false,
                onClick = { onSelected(scheduler) },
                startIcon = Icons.Default.Construction,
            )
        }
    }
}
