package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.graphics.Color
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.ExtraType

/**
 * Wraps generation actions with provider-supported LoRA, embedding and hypernetwork shortcuts.
 * The caller supplies the main generation controls; shortcuts receive the current prompt pair.
 *
 * @param modifier Layout modifiers for the complete toolbar.
 * @param mode Selected provider controlling which extra-prompt shortcuts are available.
 * @param prompt Positive prompt captured when opening an extra-prompt editor.
 * @param negativePrompt Negative prompt captured when opening an extra-prompt editor.
 * @param strokeAccentState Whether extra-prompt shortcuts use the highlighted border color.
 * @param onExtraSelected Opens the selected extra type with the current prompt pair.
 * @param onEmbeddingsSelected Opens textual-inversion selection with the current prompt pair.
 * @param content Main generation controls rendered below the shortcut row.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun GenerationBottomToolbar(
    modifier: Modifier = Modifier,
    mode: ServerSource,
    prompt: String,
    negativePrompt: String,
    strokeAccentState: Boolean = false,
    onExtraSelected: (prompt: String, negativePrompt: String, type: ExtraType) -> Unit = { _, _, _ -> },
    onEmbeddingsSelected: (prompt: String, negativePrompt: String) -> Unit = { _, _ -> },
    content: @Composable () -> Unit,
) {
    val hasExtraShortcuts = mode == ServerSource.AUTOMATIC1111 || mode == ServerSource.SWARM_UI
    GenerationActionBar(modifier) {
        if (hasExtraShortcuts) {
            GenerationBottomToolbarShortcuts(
                modifier = Modifier.padding(bottom = 4.dp),
                strokeAccentState = strokeAccentState,
                mode = mode,
                prompt = prompt,
                negativePrompt = negativePrompt,
                onExtraSelected = onExtraSelected,
                onEmbeddingsSelected = onEmbeddingsSelected,
            )
        }
        content()
    }
}

/**
 * Renders the extension rail with an outline around the visible shortcut row.
 *
 * @param modifier Placement relative to the generation action.
 * @param mode Provider determining whether hypernetworks are supported.
 * @param prompt Positive prompt passed to an extension selector.
 * @param negativePrompt Negative prompt passed to an extension selector.
 * @param strokeAccentState Whether the rail uses an accent outline.
 * @param onExtraSelected Opens LoRA or hypernetwork selection.
 * @param onEmbeddingsSelected Opens inversion selection.
 * @author Dmitriy Moroz
 */
@Composable
private fun GenerationBottomToolbarShortcuts(
    modifier: Modifier = Modifier,
    mode: ServerSource,
    prompt: String,
    negativePrompt: String,
    strokeAccentState: Boolean = false,
    onExtraSelected: (prompt: String, negativePrompt: String, type: ExtraType) -> Unit = { _, _, _ -> },
    onEmbeddingsSelected: (prompt: String, negativePrompt: String) -> Unit = { _, _ -> },
) {
    val shape = RoundedCornerShape(16.dp)
    val accentColor = if (strokeAccentState) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }
    var dividerHeight by remember {
        mutableIntStateOf(0)
    }
    Row(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = accentColor,
                shape = shape,
            )
            .onSizeChanged {
                dividerHeight = it.height
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val localColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)

        GenerationExtraShortcut(
            title = Localization.string("title_lora"),
            message = Localization.string("help_lora"),
            color = localColor,
            onClick = { onExtraSelected(prompt, negativePrompt, ExtraType.Lora) },
        )
        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(with(LocalDensity.current) { dividerHeight.toDp() })
                .background(color = accentColor),
        )
        GenerationExtraShortcut(
            title = Localization.string("title_txt_inversion_short"),
            message = Localization.string("help_inversion"),
            color = localColor,
            onClick = { onEmbeddingsSelected(prompt, negativePrompt) },
        )
        if (mode == ServerSource.AUTOMATIC1111) {
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(with(LocalDensity.current) { dividerHeight.toDp() })
                    .background(color = accentColor),
            )
            GenerationExtraShortcut(
                title = Localization.string("title_hyper_net_short"),
                message = Localization.string("help_hypernetwork"),
                color = localColor,
                onClick = { onExtraSelected(prompt, negativePrompt, ExtraType.HyperNet) },
            )
        }
    }
}

/**
 * Gives each prompt-extension shortcut an equal share of the row and explanatory long-press help.
 *
 * @param title Localized compact name of the extension.
 * @param message Friendly explanation of the extension and selector.
 * @param color Existing toolbar label color.
 * @param onClick Opens the corresponding extension selector.
 * @param modifier Placement of the weighted shortcut.
 * @author Dmitriy Moroz
 */
@Composable
private fun RowScope.GenerationExtraShortcut(
    title: String,
    message: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.weight(1f)) {
        ContextualHelp(title = title, message = message, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                color = color,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
