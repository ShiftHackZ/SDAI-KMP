package com.shifthackz.aisdv1.presentation.components.widget.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch

/**
 * Keeps handle activation and accessibility actions when blur owns the whole sheet surface.
 * State methods retain the caller's value-change confirmation before moving to another anchor.
 *
 * @param sheetState Visibility and available expansion anchors of the Material host.
 * @param onDismissRequest Removes the sheet only after its state reaches hidden.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SheetDragHandle(sheetState: SheetState, onDismissRequest: () -> Unit) {
    val scope = rememberCoroutineScope()
    val dismissSheet: () -> Unit = {
        scope.launch {
            sheetState.hide()
            if (!sheetState.isVisible) onDismissRequest()
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) {
            when (sheetState.currentValue) {
                SheetValue.Expanded -> dismissSheet()
                SheetValue.PartiallyExpanded -> scope.launch { sheetState.expand() }
                SheetValue.Hidden -> scope.launch { sheetState.show() }
            }
        }.semantics(mergeDescendants = true) {
            dismiss { dismissSheet(); true }
            if (sheetState.currentValue == SheetValue.PartiallyExpanded) {
                expand { scope.launch { sheetState.expand() }; true }
            } else if (sheetState.hasPartiallyExpandedState) {
                collapse { scope.launch { sheetState.partialExpand() }; true }
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        BottomSheetDefaults.DragHandle()
    }
}
