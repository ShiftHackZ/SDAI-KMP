package com.shifthackz.aisdv1.presentation.components.widget.dialog

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shifthackz.aisdv1.presentation.components.extensions.LocalGenerationBrightnessInteraction
import com.shifthackz.aisdv1.presentation.components.extensions.generationBrightnessInteraction
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.localization.formatter.DurationFormatter
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.core.model.asUiText

/**
 * Shows generation or remote-queue progress with optional cancellation content.
 * Touches inside the dialog or its scrim notify the active dimming timer without dismissing
 * non-dismissible progress or intercepting child controls.
 *
 * @param title Heading naming the provider or operation.
 * @param subTitle Stage or waiting explanation beneath the heading.
 * @param canDismiss Whether outside taps or Back may close the dialog.
 * @param onDismissRequest Handles permitted dismissal requests.
 * @param waitTimeSeconds Optional server estimate, measured in seconds.
 * @param positionInQueue Optional queue position, taking precedence over generation progress.
 * @param step Completed and total denoising iterations, absent for overall progress.
 * @param elapsedText Captured generation clock text, shown at a fixed position directly below the title.
 * @param overallPercentage Overall completion from zero to one hundred, never a sampling count.
 * @param content Optional actions rendered below the progress status.
 * @author Dmitriy Moroz
 */
@Composable
fun ProgressDialog(
    title: UiText = Localization.string("communicating_progress_title").asUiText(),
    subTitle: UiText = Localization.string("communicating_progress_sub_title").asUiText(),
    canDismiss: Boolean = true,
    onDismissRequest: () -> Unit = {},
    waitTimeSeconds: Int? = null,
    positionInQueue: Int? = null,
    step: Pair<Int, Int>? = null,
    overallPercentage: Int? = null,
    elapsedText: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val onBrightnessInteraction = LocalGenerationBrightnessInteraction.current
    Dialog(
        onDismissRequest = {
            onBrightnessInteraction?.invoke()
            if (canDismiss) onDismissRequest()
        },
        properties = DialogProperties(
            dismissOnClickOutside = canDismiss || onBrightnessInteraction != null,
            dismissOnBackPress = canDismiss,
        ),
    ) {
        GeneratingProgressDialogContent(
            modifier = Modifier.generationBrightnessInteraction()
                .verticalScroll(rememberScrollState()),
            title = title,
            subTitle = subTitle,
            waitTimeSeconds = waitTimeSeconds,
            positionInQueue = positionInQueue,
            step = step,
            overallPercentage = overallPercentage,
            elapsedText = elapsedText,
            content = content,
        )
    }
}

/**
 * Offers the shared localized cancellation action beneath generation progress.
 *
 * @param onClick Requests cancellation of the captured generation.
 * @param modifier Layout modifiers for the cancellation-action container.
 * @author Dmitriy Moroz
 */
@Composable
fun ProgressDialogCancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProgressDialogCancelButtonContent(
        cancelText = Localization.string("cancel"),
        onClick = onClick,
        modifier = modifier,
    )
}

/**
 * Builds the shared generation dialog content with counters in their actual unit.
 *
 * @param title Heading naming the provider or operation.
 * @param subTitle Stage or waiting explanation beneath the heading.
 * @param waitTimeSeconds Optional server estimate, measured in seconds.
 * @param positionInQueue Optional queue position, taking precedence over generation progress.
 * @param step Completed and total denoising iterations, absent for overall progress.
 * @param elapsedText Captured generation clock text, shown at a fixed position directly below the title.
 * @param overallPercentage Overall completion from zero to one hundred, never a sampling count.
 * @param content Optional actions rendered below the progress status.
 * @param modifier Layout modifiers for the progress dialog surface.
 * @author Dmitriy Moroz
 */
@Composable
fun GeneratingProgressDialogContent(
    modifier: Modifier = Modifier,
    title: UiText = Localization.string("communicating_progress_title").asUiText(),
    subTitle: UiText = Localization.string("communicating_progress_sub_title").asUiText(),
    waitTimeSeconds: Int? = null,
    positionInQueue: Int? = null,
    step: Pair<Int, Int>? = null,
    overallPercentage: Int? = null,
    elapsedText: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    ProgressDialogContent(
        title = title.asString(),
        subTitle = subTitle.asString(),
        waitTimeText = waitTimeText(waitTimeSeconds),
        elapsedText = elapsedText,
        statusText = progressStatusText(positionInQueue, step, overallPercentage),
        progress = if (positionInQueue != null) null else overallPercentage?.coerceIn(0, 100)
            ?.div(100f)
            ?: step?.takeIf { it.second > 0 }?.let { (current, total) ->
                (current.toFloat() / total).coerceIn(0f, 1f)
            },
        content = content,
        modifier = modifier,
    )
}

/**
 * Formats an available server wait estimate using the current locale.
 *
 * @param waitTimeSeconds Optional server estimate, measured in seconds.
 * @return Localized wait estimate, or null when the server provides none.
 * @author Dmitriy Moroz
 */
private fun waitTimeText(waitTimeSeconds: Int?): String? {
    return waitTimeSeconds?.let { seconds ->
        Localization.string(
            "communicating_wait_time",
            DurationFormatter.formatDurationInSeconds(seconds),
        )
    }
}

/**
 * Formats queue position, overall completion or sampling counters without mixing their units.
 *
 * @param positionInQueue Optional queue position, taking precedence over generation progress.
 * @param step Completed and total denoising iterations, absent for overall progress.
 * @param overallPercentage Overall completion from zero to one hundred, never a sampling count.
 * @return Status text in the provider's unit, or null before counters arrive.
 * @author Dmitriy Moroz
 */
fun progressStatusText(
    positionInQueue: Int?,
    step: Pair<Int, Int>?,
    overallPercentage: Int?,
): String? {
    return positionInQueue?.let { position ->
        Localization.string("communicating_status_queue", position)
    } ?: overallPercentage?.let { "${it.coerceIn(0, 100)}%" } ?: step?.let { (current, total) ->
        Localization.string("communicating_status_steps", current, total) +
                if (total > 0) " · ${(current.toLong() * 100 / total).coerceIn(0, 100)}%" else ""
    }
}
