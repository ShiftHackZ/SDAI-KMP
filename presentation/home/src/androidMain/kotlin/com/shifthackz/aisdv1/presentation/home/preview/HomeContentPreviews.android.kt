package com.shifthackz.aisdv1.presentation.home.preview

import android.content.res.Configuration
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings

/**
 * Shows all home-shell selections, drawer variants and connectivity presentations in both palettes.
 *
 * @param scenario Named shell state supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Home", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Home", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeStatesPreview(
    @PreviewParameter(HomePreviewParameterProvider::class) scenario: HomePreviewCase,
) {
    HomePreview(scenario)
}

/**
 * Shows all four bottom tabs on narrow and landscape viewports.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Home", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Home", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Home", widthDp = 800, heightDp = 360)
@Composable
private fun HomeCompactPreview() {
    HomePreview(HomePreviewCase("Compact settings", route = AppRoute.Settings))
}

/**
 * Shows drawer label wrapping with a long provider name, developer destination and enlarged text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Drawer · Large text",
    group = "Home",
    widthDp = 320,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun HomeLargeTextPreview() {
    HomePreview(
        HomePreviewCase(
            "Large text drawer", route = AppRoute.WebUi,
            settings = Settings(source = ServerSource.OPEN_AI_COMPATIBLE, developerMode = true),
            drawerValue = DrawerValue.Open,
        )
    )
}
