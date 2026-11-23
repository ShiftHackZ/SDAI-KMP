package com.shifthackz.aisdv1.presentation.donate.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.donate.DonateState

/**
 * Displays each loading and supporter scenario in light and dark application palettes.
 *
 * @param scenario Named supporter-content snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "Donate · Light", group = "Donate states", widthDp = 360, heightDp = 800)
@Preview(
    name = "Donate · Dark", group = "Donate states", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DonateStatesPreview(
    @PreviewParameter(DonatePreviewParameterProvider::class) scenario: DonatePreviewCase,
) {
    DonatePreview(scenario)
}

/**
 * Displays the supporter list and pinned support explanation at compact, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Donate sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Donate sizes", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Donate sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Donate sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun DonateSizesPreview() {
    DonatePreview(
        DonatePreviewCase(
            "Mixed supporters",
            DonateState(loading = false, supporters = DonatePreviewFixtures.mixed),
        )
    )
}
