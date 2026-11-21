package com.shifthackz.aisdv1.presentation.components.widget.work

import androidx.compose.ui.graphics.ImageBitmap
import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Status card state for active work or its latest completed/error outcome.
 *
 * @param visible Whether the card should be rendered.
 * @param title Localized active-stage or outcome heading.
 * @param subTitle Queue/progress information or public failure details.
 * @param running Whether background generation is still active.
 * @param dismissible Whether a completed/error result may be acknowledged.
 * @param image Optional already decoded result preview.
 * @param imageBase64 One original result sampled only by the visible preview slot.
 * @param percentage Measured active completion, or null during queue/loading.
 * @param isError Whether failure styling replaces completion imagery.
 *
 * @author Dmitriy Moroz
 */
data class BackgroundWorkState(
    val visible: Boolean = false,
    val title: String = "",
    val subTitle: String = "",
    val running: Boolean = false,
    val dismissible: Boolean = false,
    val image: ImageBitmap? = null,
    val imageBase64: String? = null,
    val percentage: Int? = null,
    val isError: Boolean = false,
) : MviState
