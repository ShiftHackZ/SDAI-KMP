package com.shifthackz.aisdv1.presentation.widget.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.localization.formatter.DurationFormatter
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.core.model.asUiText

/**
 * Shows generation or remote-queue progress with optional cancellation content.
 *
 * @param title Heading naming the provider or operation.
 * @param subTitle Stage or waiting explanation beneath the heading.
 * @param canDismiss Whether outside taps or Back may close the dialog.
 * @param onDismissRequest Handles permitted dismissal requests.
 * @param waitTimeSeconds Optional server estimate, measured in seconds.
 * @param positionInQueue Optional queue position, taking precedence over generation progress.
 * @param step Completed and total denoising iterations, absent for overall progress.
 * @param overallPercentage Overall completion from zero to one hundred, never a sampling count.
 * @param content Optional actions rendered below the progress status.
 *
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
    content: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = canDismiss,
            dismissOnBackPress = canDismiss,
        ),
    ) {
        GeneratingProgressDialogContent(
            title = title,
            subTitle = subTitle,
            waitTimeSeconds = waitTimeSeconds,
            positionInQueue = positionInQueue,
            step = step,
            overallPercentage = overallPercentage,
            content = content,
        )
    }
}

/**
 * Shows waiting time and the active queue, sampling or overall-percentage status.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @param waitTimeSeconds Optional server estimate, measured in seconds.
 * @param positionInQueue Optional queue position, taking precedence over generation progress.
 * @param step Completed and total denoising iterations, absent for overall progress.
 * @param overallPercentage Overall completion from zero to one hundred, never a sampling count.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ProgressDialogStatus(
    modifier: Modifier = Modifier,
    waitTimeSeconds: Int?,
    positionInQueue: Int?,
    step: Pair<Int, Int>?,
    overallPercentage: Int? = null,
) {
    ProgressDialogStatusContent(
        modifier = modifier,
        waitTimeText = waitTimeText(waitTimeSeconds),
        statusText = progressStatusText(positionInQueue, step, overallPercentage),
    )
}

/**
 * Offers the shared localized cancellation action beneath generation progress.
 *
 * @param onClick Requests cancellation of the captured generation.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ProgressDialogCancelButton(onClick: () -> Unit) {
    ProgressDialogCancelButtonContent(
        cancelText = Localization.string("cancel"),
        onClick = onClick,
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
 * @param overallPercentage Overall completion from zero to one hundred, never a sampling count.
 * @param content Optional actions rendered below the progress status.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun GeneratingProgressDialogContent(
    title: UiText = Localization.string("communicating_progress_title").asUiText(),
    subTitle: UiText = Localization.string("communicating_progress_sub_title").asUiText(),
    waitTimeSeconds: Int? = null,
    positionInQueue: Int? = null,
    step: Pair<Int, Int>? = null,
    overallPercentage: Int? = null,
    content: (@Composable () -> Unit)? = null,
) {
    ProgressDialogContent(
        title = title.asString(),
        subTitle = subTitle.asString(),
        waitTimeText = waitTimeText(waitTimeSeconds),
        statusText = progressStatusText(positionInQueue, step, overallPercentage),
        content = content,
    )
}

/**
 * Formats an available server wait estimate using the current locale.
 *
 * @param waitTimeSeconds Optional server estimate, measured in seconds.
 * @return Localized wait estimate, or null when the server provides none.
 *
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
 *
 * @author Dmitriy Moroz
 */
internal fun progressStatusText(
    positionInQueue: Int?,
    step: Pair<Int, Int>?,
    overallPercentage: Int?,
): String? {
    return positionInQueue?.let { position ->
        Localization.string("communicating_status_queue", position)
    } ?: overallPercentage?.let { "${it.coerceIn(0, 100)}%" } ?: step?.let { (current, total) ->
        Localization.string("communicating_status_steps", current, total)
    }
}
