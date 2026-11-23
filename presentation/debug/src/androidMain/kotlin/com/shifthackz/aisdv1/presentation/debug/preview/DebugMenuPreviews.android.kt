package com.shifthackz.aisdv1.presentation.debug.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.debug.DebugMenuState

/**
 * Displays every capability and preference snapshot in light and dark palettes.
 *
 * @param scenario Named menu snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "Menu · Light", group = "Debug menu", widthDp = 360, heightDp = 800)
@Preview(
    name = "Menu · Dark", group = "Debug menu", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DebugMenuStatesPreview(
    @PreviewParameter(DebugMenuPreviewParameterProvider::class) scenario: DebugMenuPreviewCase,
) {
    DebugMenuPreview(scenario)
}

/**
 * Displays compact, landscape and enlarged-text layouts with all developer sections enabled.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Debug sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Debug sizes", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Debug sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Debug sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun DebugMenuSizesPreview() {
    DebugMenuPreview(
        DebugMenuPreviewCase(
            "All controls",
            DebugMenuState(
                isUiDiagnosticsSupported = true,
                isUiDiagnosticsCapturing = true,
                showWorkManagerSection = true,
                showLocalDiffusionSection = true,
                localDiffusionAllowCancel = true,
            ),
        )
    )
}
