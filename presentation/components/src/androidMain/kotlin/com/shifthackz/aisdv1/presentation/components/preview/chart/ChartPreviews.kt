package com.shifthackz.aisdv1.presentation.components.preview.chart

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.model.UsageItem
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory
import com.shifthackz.aisdv1.presentation.components.widget.usage.UsageDonutChart
import com.shifthackz.aisdv1.presentation.components.model.UsageState

/**
 * Donut geometry for zero, single, tiny, selected and large byte categories.
 *
 * @author Dmitriy Moroz
 */
internal class ChartPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("UsageDonutChart · Empty") {
            UsageDonutChart(UsageState(), {})
        },
        ComponentPreviewCase("UsageDonutChart · Single") {
            UsageDonutChart(
                UsageState(items = listOf(UsageItem(UsageCategory.GALLERY, 1048576))),
                {})
        },
        ComponentPreviewCase("UsageDonutChart · Mixed") {
            UsageDonutChart(
                UsageState(
                    items = listOf(
                        UsageItem(UsageCategory.CACHE, 400),
                        UsageItem(UsageCategory.GALLERY, 100),
                        UsageItem(UsageCategory.MODELS_MNN, 1)
                    )
                ), {})
        },
        ComponentPreviewCase("UsageDonutChart · Selected") {
            UsageDonutChart(
                UsageState(
                    items = listOf(
                        UsageItem(UsageCategory.CACHE, 400),
                        UsageItem(UsageCategory.GALLERY, 100)
                    ), selectedCategory = UsageCategory.GALLERY
                ), {})
        },
        ComponentPreviewCase("UsageDonutChart · Huge values") {
            UsageDonutChart(
                UsageState(
                    items = listOf(
                        UsageItem(
                            UsageCategory.GALLERY,
                            1099511627776
                        )
                    )
                ), {})
        },
    ),
)

/**
 * Renders named chart states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 340)
@Preview(name = "Dark", widthDp = 400, heightDp = 340, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun ChartPreview(@PreviewParameter(ChartPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
