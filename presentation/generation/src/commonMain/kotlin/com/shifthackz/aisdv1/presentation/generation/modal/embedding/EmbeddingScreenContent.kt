package com.shifthackz.aisdv1.presentation.generation.modal.embedding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.error.ErrorComposable
import com.shifthackz.aisdv1.presentation.components.widget.source.getName
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ModalDialogToolbar

/**
 * Displays a captured embedding catalog and prompt-selection tabs without fetching provider data.
 *
 * @param modifier Placement of the dialog surface.
 * @param state Captured editor data and selection/loading/error state.
 * @param processIntent Receives edits, confirmation and dismissal requests.
 * @author Dmitriy Moroz
 */
@Composable
internal fun EmbeddingScreenContent(
    modifier: Modifier = Modifier,
    state: EmbeddingState,
    processIntent: (EmbeddingIntent) -> Unit,
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
                            onClick = { processIntent(EmbeddingIntent.ApplyNewPrompts) }
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
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                ) {
                    ModalDialogToolbar(
                        text = Localization.string("title_txt_inversion"),
                        onClose = { processIntent(EmbeddingIntent.Close) },
                    )
                    val bgColor = MaterialTheme.colorScheme.surface
                    val selectedTabColor = MaterialTheme.colorScheme.primary
                    val unselectedTabColor = Color.Transparent
                    var dividerHeight by remember {
                        mutableIntStateOf(0)
                    }
                    if (!state.loading && state.embeddings.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(intrinsicSize = IntrinsicSize.Max)
                                .onSizeChanged {
                                    if (it.height > dividerHeight) dividerHeight = it.height
                                }
                                .drawBehind {
                                    drawRoundRect(
                                        color = bgColor,
                                        cornerRadius = CornerRadius(16.dp.toPx())
                                    )
                                }
                                .padding(horizontal = 12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(with(LocalDensity.current) { dividerHeight.toDp() })
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .drawBehind {
                                        drawRoundRect(
                                            color = if (state.selector) selectedTabColor else unselectedTabColor,
                                            cornerRadius = CornerRadius(16.dp.toPx())
                                        )
                                    }
                                    .clickable { processIntent(EmbeddingIntent.ChangeSelector(true)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    text = Localization.string("hint_prompt"),
                                    color = if (state.selector) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .drawBehind {
                                        drawRoundRect(
                                            color = if (!state.selector) selectedTabColor else unselectedTabColor,
                                            cornerRadius = CornerRadius(16.dp.toPx())
                                        )
                                    }
                                    .clickable { processIntent(EmbeddingIntent.ChangeSelector(false)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    text = Localization.string("hint_prompt_negative"),
                                    color = if (!state.selector) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn {
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
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .shimmer(),
                                )
                            }
                        } else {
                            if (state.embeddings.isEmpty()) {
                                item(key = "empty_state") {
                                    EmbeddingEmptyState(state.source)
                                }
                            } else {
                                items(
                                    count = state.embeddings.size,
                                    key = { index -> state.embeddings[index].keyword },
                                ) { index ->
                                    EmbeddingItemComposable(
                                        item = state.embeddings[index],
                                        isPositivePrompt = state.selector,
                                        onItemToggle = {
                                            processIntent(EmbeddingIntent.ToggleItem(it))
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
 * Explains the provider's empty embedding catalog and its expected server directory.
 *
 * @param source Provider whose embedding directory is described.
 * @param modifier Placement of the empty-catalog explanation.
 * @author Dmitriy Moroz
 */
@Composable
private fun EmbeddingEmptyState(
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
        val path = when (source) {
            ServerSource.AUTOMATIC1111 -> "./embeddings"
            ServerSource.SWARM_UI -> "./Models/Embeddings"
            else -> ""
        }
        Text(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = Localization.string(
                "extras_empty_sub_title_embedding",
                source.getName(),
                path,
            ),
            textAlign = TextAlign.Center,
        )
    }
}
