package com.shifthackz.aisdv1.presentation.components.preview.brand

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.icon.BrandIcons
import androidx.compose.ui.Modifier

/**
 * Each shared community, platform and developer vector at a visible inspection size.
 *
 * @author Dmitriy Moroz
 */
internal class BrandPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("BrandIcons · Civitai") {
            Image(
                imageVector = BrandIcons.Civitai,
                contentDescription = "Civitai",
                modifier = Modifier.size(96.dp)
            )
        },
        ComponentPreviewCase("BrandIcons · Telegram") {
            Image(
                imageVector = BrandIcons.Telegram,
                contentDescription = "Telegram",
                modifier = Modifier.size(96.dp)
            )
        },
        ComponentPreviewCase("BrandIcons · Discord") {
            Image(
                imageVector = BrandIcons.Discord,
                contentDescription = "Discord",
                modifier = Modifier.size(96.dp)
            )
        },
        ComponentPreviewCase("BrandIcons · LinkedIn") {
            Image(
                imageVector = BrandIcons.LinkedIn,
                contentDescription = "LinkedIn",
                modifier = Modifier.size(96.dp)
            )
        },
        ComponentPreviewCase("BrandIcons · Apple") {
            Image(
                imageVector = BrandIcons.Apple,
                contentDescription = "Apple",
                modifier = Modifier.size(96.dp)
            )
        },
        ComponentPreviewCase("BrandIcons · Moroz") {
            Image(
                imageVector = BrandIcons.Moroz,
                contentDescription = "Moroz",
                modifier = Modifier.size(96.dp)
            )
        },
    ),
)

/**
 * Renders named brand states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 180)
@Preview(name = "Dark", widthDp = 400, heightDp = 180, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun BrandPreview(@PreviewParameter(BrandPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
