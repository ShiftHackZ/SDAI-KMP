package com.shifthackz.aisdv1.presentation.components.preview.feedback

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityComposableContent
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityState
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityStatus
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityWidget
import com.shifthackz.aisdv1.presentation.components.widget.error.ErrorComposable
import com.shifthackz.aisdv1.presentation.components.widget.error.ErrorComposableContent
import androidx.compose.ui.Modifier

/**
 * Connection checks, reachable and unreachable providers, hidden indicators and inline error variants.
 *
 * @author Dmitriy Moroz
 */
internal class FeedbackPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("ConnectivityWidget · Uninitialized · true") {
            ConnectivityWidget(ConnectivityState.Uninitialized(enabled = true))
        },
        ComponentPreviewCase("ConnectivityWidget · Uninitialized · false") {
            ConnectivityWidget(ConnectivityState.Uninitialized(enabled = false))
        },
        ComponentPreviewCase("ConnectivityWidget · Connected · true") {
            ConnectivityWidget(ConnectivityState.Connected(enabled = true))
        },
        ComponentPreviewCase("ConnectivityWidget · Connected · false") {
            ConnectivityWidget(ConnectivityState.Connected(enabled = false))
        },
        ComponentPreviewCase("ConnectivityWidget · Disconnected · true") {
            ConnectivityWidget(ConnectivityState.Disconnected(enabled = true))
        },
        ComponentPreviewCase("ConnectivityWidget · Disconnected · false") {
            ConnectivityWidget(ConnectivityState.Disconnected(enabled = false))
        },
        ComponentPreviewCase("ConnectivityComposableContent · Custom") {
            ConnectivityComposableContent(
                text = "Checking a long provider endpoint status",
                status = ConnectivityStatus.Uninitialized,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                visible = true
            )
        },
        ComponentPreviewCase("ErrorComposable · None") {
            ErrorComposable(state = ErrorState.None)
        },
        ComponentPreviewCase("ErrorComposable · Generic") {
            ErrorComposable(state = ErrorState.Generic)
        },
        ComponentPreviewCase("ErrorComposable · Message") {
            ErrorComposable(state = ErrorState.WithMessage("Check your connection and retry.".asUiText()))
        },
        ComponentPreviewCase("ErrorComposableContent · Long") {
            ErrorComposableContent(
                "Unable to continue",
                "This operation could not finish. ".repeat(12)
            )
        },
    ),
)

/**
 * Renders named feedback states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 420)
@Preview(name = "Dark", widthDp = 400, heightDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun FeedbackPreview(@PreviewParameter(FeedbackPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
