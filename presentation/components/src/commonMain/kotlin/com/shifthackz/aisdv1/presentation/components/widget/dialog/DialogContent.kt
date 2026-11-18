package com.shifthackz.aisdv1.presentation.components.widget.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Presents an informational alert using already localized strings.
 *
 * @param title Dialog heading.
 * @param subTitle Explanatory body text.
 * @param okText Acknowledgement button label.
 * @param onDismissRequest Requests removal after acknowledgement or outside dismissal.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun InfoDialogContent(
    title: String,
    subTitle: String,
    okText: String,
    onDismissRequest: () -> Unit = {},
) {
    AlertDialog(
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = okText)
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                color = AlertDialogDefaults.titleContentColor,
            )
        },
        text = {
            Text(
                text = subTitle,
                fontSize = 14.sp,
                color = AlertDialogDefaults.textContentColor,
            )
        },
    )
}

/**
 * Presents an error alert using already localized strings.
 *
 * @param title Error heading.
 * @param text Error details.
 * @param okText Acknowledgement button label.
 * @param onDismissRequest Requests removal after acknowledgement or outside dismissal.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ErrorDialogContent(
    title: String,
    text: String,
    okText: String,
    onDismissRequest: () -> Unit = {},
) {
    AlertDialog(
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = okText)
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                color = AlertDialogDefaults.titleContentColor,
            )
        },
        text = {
            Text(
                text = text,
                fontSize = 14.sp,
                color = AlertDialogDefaults.textContentColor,
            )
        },
    )
}

/**
 * Presents an affirmative/cancel decision with optional interactive body content.
 *
 * @param title Dialog heading.
 * @param text Explanation of the decision.
 * @param confirmActionText Affirmative button label.
 * @param dismissActionText Cancel button label.
 * @param onConfirmAction Requests the affirmative operation without dismissing state automatically.
 * @param onDismissRequest Requests cancellation or outside dismissal.
 * @param content Additional interactive controls inside the body.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun DecisionInteractiveDialogContent(
    title: String,
    text: String,
    confirmActionText: String,
    dismissActionText: String,
    onConfirmAction: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    AlertDialog(
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmAction) {
                Text(text = confirmActionText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = dismissActionText)
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                color = AlertDialogDefaults.titleContentColor,
            )
        },
        text = {
            Text(
                text = text,
                fontSize = 14.sp,
                color = AlertDialogDefaults.textContentColor,
            )
            content()
        },
    )
}

/**
 * Shows indeterminate operation progress with optional status and caller-owned actions.
 *
 * @param title Operation heading.
 * @param subTitle Explanatory progress text.
 * @param modifier Layout modifiers for the progress surface.
 * @param waitTimeText Optional queue/wait estimate; null reserves an empty status line.
 * @param statusText Optional current stage text.
 * @param content Optional controls rendered below the progress indicator.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ProgressDialogContent(
    title: String,
    subTitle: String,
    modifier: Modifier = Modifier,
    waitTimeText: String? = null,
    statusText: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text = title,
                style = TextStyle(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                color = AlertDialogDefaults.titleContentColor,
            )
            Text(
                modifier = Modifier.padding(top = 14.dp),
                text = subTitle,
                style = TextStyle(fontSize = 14.sp),
                color = AlertDialogDefaults.textContentColor,
            )
            ProgressDialogStatusContent(
                waitTimeText = waitTimeText,
                statusText = statusText,
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = AlertDialogDefaults.iconContentColor,
            )
            content?.invoke()
        }
    }
}

/**
 * Shows the optional queue estimate and current operation stage beneath a progress explanation.
 *
 * @param modifier Layout modifiers for the status group.
 * @param waitTimeText Optional already localized queue/wait estimate.
 * @param statusText Optional already localized stage description.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ProgressDialogStatusContent(
    modifier: Modifier = Modifier,
    waitTimeText: String?,
    statusText: String?,
) {
    Column(modifier.padding(vertical = 4.dp)) {
        Text(
            text = waitTimeText.orEmpty(),
            style = TextStyle(fontSize = 12.sp),
            color = AlertDialogDefaults.textContentColor,
        )
        Text(
            text = statusText.orEmpty(),
            style = TextStyle(fontSize = 12.sp),
            color = AlertDialogDefaults.textContentColor,
        )
    }
}

/**
 * Centers a wide cancellation action beneath an operation's progress information.
 *
 * @param cancelText Localized cancellation button label.
 * @param onClick Requests cooperative cancellation through the operation owner.
 * @param modifier Layout modifiers for the button container.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ProgressDialogCancelButtonContent(
    cancelText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedButton(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.7f),
            onClick = onClick,
        ) {
            Text(
                text = cancelText,
                color = LocalContentColor.current,
            )
        }
    }
}
