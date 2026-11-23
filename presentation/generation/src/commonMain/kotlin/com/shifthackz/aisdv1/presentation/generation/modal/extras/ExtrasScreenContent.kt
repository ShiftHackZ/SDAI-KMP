package com.shifthackz.aisdv1.presentation.generation.modal.extras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.presentation.components.model.ExtraType
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.error.ErrorComposable
import com.shifthackz.aisdv1.presentation.components.widget.source.getName
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ModalDialogToolbar

/**
 * Displays a captured LoRA or hypernetwork catalog without resolving its ViewModel.
 *
 * @param modifier Placement of the dialog surface.
 * @param state Captured editor data and selection/loading/error state.
 * @param processIntent Receives edits, confirmation and dismissal requests.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ExtrasScreenContent(
    modifier: Modifier = Modifier,
    state: ExtrasState,
    processIntent: (ExtrasIntent) -> Unit = {},
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        ),
    ) {
        Surface(
            modifier = modifier.fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                bottomBar = {
                    if (!state.loading) {
                        PrimaryButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            onClick = { processIntent(ExtrasIntent.ApplyPrompts) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = Localization.string(
                                    if (state.error != ErrorState.None) "close" else "apply"
                                ),
                                color = LocalContentColor.current,
                            )
                        }
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize(),
                ) {
                    ModalDialogToolbar(
                        text = Localization.string(
                            when (state.type) {
                                ExtraType.Lora -> "title_lora"
                                ExtraType.HyperNet -> "title_hyper_net"
                            }
                        ),
                        onClose = { processIntent(ExtrasIntent.Close) },
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (state.error != ErrorState.None) {
                            item(key = "error_state") {
                                ErrorComposable(state = state.error)
                            }
                        } else if (state.loading) {
                            items(
                                count = 3,
                                key = { index -> "shimmer_$index" },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .shimmer(),
                                )
                            }
                        } else {
                            if (state.loras.isEmpty()) {
                                item(key = "empty_state") {
                                    ExtrasEmptyState(state.type, state.source)
                                }
                            } else {
                                items(
                                    count = state.loras.size,
                                    key = { index -> state.loras[index].key },
                                ) { index ->
                                    ExtrasItemComposable(
                                        item = state.loras[index],
                                        onLoraSelected = { item ->
                                            processIntent(ExtrasIntent.ToggleItem(item))
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Explains the selected provider's empty LoRA or hypernetwork catalog.
 *
 * @param type Extra category selecting its expected directory and explanation.
 * @param source Provider whose catalog is empty.
 * @param modifier Placement of the empty-catalog explanation.
 * @author Dmitriy Moroz
 */
@Composable
private fun ExtrasEmptyState(
    type: ExtraType,
    source: ServerSource,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = Localization.string("extras_empty_title"),
            fontSize = 20.sp,
        )
        val path = when (type) {
            ExtraType.Lora -> when (source) {
                ServerSource.AUTOMATIC1111 -> "../models/Lora"
                ServerSource.SWARM_UI -> "../Models/Lora"
                else -> ""
            }

            ExtraType.HyperNet -> when (source) {
                ServerSource.AUTOMATIC1111 -> "../models/hypernetworks"
                ServerSource.SWARM_UI -> ""
                else -> ""
            }
        }
        Text(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = Localization.string(
                when (type) {
                    ExtraType.Lora -> "extras_empty_sub_title_lora"
                    ExtraType.HyperNet -> "extras_empty_sub_title_hypernet"
                },
                source.getName(),
                path,
            ),
            textAlign = TextAlign.Center,
        )
    }
}
