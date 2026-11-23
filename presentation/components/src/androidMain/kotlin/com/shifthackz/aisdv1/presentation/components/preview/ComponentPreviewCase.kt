package com.shifthackz.aisdv1.presentation.components.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

/**
 * Names a fixed visual state without creating runtime view models or platform services.
 *
 * @param name Component and state shown in the IDE preview label.
 * @param content Actual component rendered with fixed input and inert callbacks.
 * @author Dmitriy Moroz
 */
internal class ComponentPreviewCase(val name: String, val content: @Composable () -> Unit)

/**
 * Supplies named component states to Android Studio without retaining application state.
 *
 * @param cases Ordered component snapshots owned by one visual family.
 * @author Dmitriy Moroz
 */
internal abstract class ComponentPreviewProvider(private val cases: List<ComponentPreviewCase>) :
    PreviewParameterProvider<ComponentPreviewCase> {
    /**
     * Fixed states exposed to the preview renderer.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ComponentPreviewCase> = cases.asSequence()

    /**
     * Labels the snapshot by component and observable state.
     *
     * @param index Position in this family's ordered snapshots.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
