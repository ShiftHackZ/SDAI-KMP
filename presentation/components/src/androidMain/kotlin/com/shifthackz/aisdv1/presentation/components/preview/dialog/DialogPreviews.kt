package com.shifthackz.aisdv1.presentation.components.preview.dialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialogContent
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialogContent
import com.shifthackz.aisdv1.presentation.components.widget.dialog.GeneratingProgressDialogContent
import com.shifthackz.aisdv1.presentation.components.widget.dialog.InfoDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.InfoDialogContent
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialogCancelButton
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialogCancelButtonContent
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialogContent
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialogStatusContent
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsToggleItem
import androidx.compose.ui.Modifier

/**
 * Shared dialog bodies and wrappers: information, decisions, failures, queueing and measured progress.
 *
 * @author Dmitriy Moroz
 */
internal class DialogPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("InfoDialogContent") {
            InfoDialogContent("Information", "Configuration was saved.", "OK")
        },
        ComponentPreviewCase("ErrorDialogContent") {
            ErrorDialogContent("Error", "Unable to connect. Check the address and try again.", "OK")
        },
        ComponentPreviewCase("ErrorDialogContent · Long") {
            ErrorDialogContent("Error", "Unable to finish this operation. ".repeat(12), "OK")
        },
        ComponentPreviewCase("DecisionInteractiveDialogContent") {
            DecisionInteractiveDialogContent(
                "Delete image?",
                "This image will be removed from the gallery.",
                "Delete",
                "Cancel"
            )
        },
        ComponentPreviewCase("DecisionInteractiveDialogContent · Slot") {
            DecisionInteractiveDialogContent(
                "Export",
                "Choose what to include.",
                "Export",
                "Cancel",
                content = { SettingsToggleItem("Include images".asUiText(), true, {}) })
        },
        ComponentPreviewCase("ProgressDialogContent · Indeterminate") {
            ProgressDialogContent("Preparing", "Loading model")
        },
        ComponentPreviewCase("ProgressDialogContent · Queued") {
            ProgressDialogContent(
                "Waiting",
                "Generation queued",
                waitTimeText = "About 30 seconds",
                statusText = "Position 2"
            )
        },
        ComponentPreviewCase("ProgressDialogContent · Measured") {
            ProgressDialogContent(
                "Generating",
                "Sampling",
                statusText = "Step 12 / 30",
                progress = 0.4f,
                elapsedText = "00:42"
            )
        },
        ComponentPreviewCase("ProgressDialogContent · Complete") {
            ProgressDialogContent("Generating", "Finishing", progress = 1f, elapsedText = "01:23")
        },
        ComponentPreviewCase("ProgressDialogContent · Custom") {
            ProgressDialogContent(
                "Generating",
                "Working",
                content = { Text("Custom progress details") })
        },
        ComponentPreviewCase("ProgressDialogStatusContent · Wait") {
            ProgressDialogStatusContent(waitTimeText = "About 30 seconds", statusText = null)
        },
        ComponentPreviewCase("ProgressDialogStatusContent · Status") {
            ProgressDialogStatusContent(waitTimeText = null, statusText = "Step 12 / 30")
        },
        ComponentPreviewCase("ProgressDialogStatusContent · Both") {
            ProgressDialogStatusContent(
                waitTimeText = "About 30 seconds",
                statusText = "Position 2"
            )
        },
        ComponentPreviewCase("ProgressDialogCancelButtonContent") {
            ProgressDialogCancelButtonContent("Cancel generation", {})
        },
        ComponentPreviewCase("ProgressDialogCancelButton") {
            ProgressDialogCancelButton({})
        },
        ComponentPreviewCase("GeneratingProgressDialogContent · Queue") {
            GeneratingProgressDialogContent(positionInQueue = 2, waitTimeSeconds = 30)
        },
        ComponentPreviewCase("GeneratingProgressDialogContent · Steps") {
            GeneratingProgressDialogContent(
                step = 12 to 30,
                overallPercentage = 40,
                elapsedText = "00:42"
            )
        },
        ComponentPreviewCase("InfoDialog") {
            InfoDialog("Information".asUiText(), "Saved".asUiText())
        },
        ComponentPreviewCase("ErrorDialog") {
            ErrorDialog("Unable to connect".asUiText())
        },
        ComponentPreviewCase("DecisionInteractiveDialog") {
            DecisionInteractiveDialog("Delete?".asUiText(), "Remove this image?".asUiText())
        },
        ComponentPreviewCase("ProgressDialog · Cancellable") {
            ProgressDialog(canDismiss = true, overallPercentage = 40)
        },
        ComponentPreviewCase("ProgressDialog · Blocking") {
            ProgressDialog(canDismiss = false, positionInQueue = 2)
        },
    ),
)

/**
 * Renders named dialog states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 560)
@Preview(name = "Dark", widthDp = 400, heightDp = 560, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 560, fontScale = 1.5f)
@Composable
private fun DialogPreview(@PreviewParameter(DialogPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
