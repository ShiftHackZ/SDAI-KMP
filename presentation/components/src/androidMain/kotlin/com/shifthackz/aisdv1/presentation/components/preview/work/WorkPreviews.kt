package com.shifthackz.aisdv1.presentation.components.preview.work

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.preview.rememberComponentPreviewBitmap
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.work.BackgroundGenerationProgressContent
import com.shifthackz.aisdv1.presentation.components.widget.work.BackgroundWorkState
import com.shifthackz.aisdv1.presentation.components.widget.work.BackgroundWorkWidgetContent

/**
 * Queue, model preparation, bounded progress, retained result, dismissible error and hidden background work.
 *
 * @author Dmitriy Moroz
 */
internal class WorkPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("BackgroundWorkWidget · Hidden") {
            BackgroundWorkWidgetContent(state = BackgroundWorkState())
        },
        ComponentPreviewCase("BackgroundWorkWidget · Queued") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    running = true,
                    title = "Waiting",
                    subTitle = "Position 2"
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Preparing") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    running = true,
                    title = "Loading model"
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Measured zero") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    running = true,
                    percentage = 0,
                    subTitle = "Preparing"
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Measured") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    running = true,
                    percentage = 42,
                    subTitle = "Step 12 / 30"
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Measured complete") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    running = true,
                    percentage = 100,
                    subTitle = "Finishing"
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Complete") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    title = "Generation complete",
                    dismissible = true
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Failure") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    isError = true,
                    title = "Generation failed",
                    subTitle = "Model could not be loaded",
                    dismissible = true
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Blank title fallback") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    dismissible = true
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Result image") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    title = "Generation complete",
                    image = rememberComponentPreviewBitmap(),
                    dismissible = true
                )
            )
        },
        ComponentPreviewCase("BackgroundWorkWidget · Long details") {
            BackgroundWorkWidgetContent(
                state = BackgroundWorkState(
                    visible = true,
                    isError = true,
                    title = "Generation could not finish",
                    subTitle = "Unable to finish this operation. ".repeat(8),
                    dismissible = true
                )
            )
        },
        ComponentPreviewCase("BackgroundGenerationProgressContent · 0") {
            BackgroundGenerationProgressContent(status = "Generating", percentage = 0)
        },
        ComponentPreviewCase("BackgroundGenerationProgressContent · 42") {
            BackgroundGenerationProgressContent(status = "Generating", percentage = 42)
        },
        ComponentPreviewCase("BackgroundGenerationProgressContent · 100") {
            BackgroundGenerationProgressContent(status = "Generating", percentage = 100)
        },
        ComponentPreviewCase("BackgroundGenerationProgressContent · No status") {
            BackgroundGenerationProgressContent(status = null, percentage = 42)
        },
    ),
)

/**
 * Renders named work states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 420)
@Preview(name = "Dark", widthDp = 400, heightDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun WorkPreview(@PreviewParameter(WorkPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
