package com.shifthackz.aisdv1.presentation.components.preview.usage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.components.model.UsageItem
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory
import com.shifthackz.aisdv1.presentation.components.widget.usage.UsageScreenContent
import com.shifthackz.aisdv1.presentation.components.model.UsageScreenKind
import com.shifthackz.aisdv1.presentation.components.model.UsageState

/**
 * Storage and network category layouts with loading, zero bytes, active/disabled rows and selection.
 *
 * @author Dmitriy Moroz
 */
internal class UsagePreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("UsageScreenContent · STORAGE · Loading") {
            UsageScreenContent(screen = UsageScreenKind.STORAGE, usage = UsageState(loading = true))
        },
        ComponentPreviewCase("UsageScreenContent · STORAGE · Empty") {
            UsageScreenContent(screen = UsageScreenKind.STORAGE, usage = UsageState())
        },
        ComponentPreviewCase("UsageScreenContent · STORAGE · Data") {
            UsageScreenContent(
                screen = UsageScreenKind.STORAGE,
                usage = usageState(UsageScreenKind.STORAGE)
            )
        },
        ComponentPreviewCase("UsageScreenContent · STORAGE · Selected") {
            UsageScreenContent(
                screen = UsageScreenKind.STORAGE,
                usage = usageState(UsageScreenKind.STORAGE).let { it.copy(selectedCategory = it.items.first().category) })
        },
        ComponentPreviewCase("UsageScreenContent · NETWORK · Loading") {
            UsageScreenContent(screen = UsageScreenKind.NETWORK, usage = UsageState(loading = true))
        },
        ComponentPreviewCase("UsageScreenContent · NETWORK · Empty") {
            UsageScreenContent(screen = UsageScreenKind.NETWORK, usage = UsageState())
        },
        ComponentPreviewCase("UsageScreenContent · NETWORK · Data") {
            UsageScreenContent(
                screen = UsageScreenKind.NETWORK,
                usage = usageState(UsageScreenKind.NETWORK)
            )
        },
        ComponentPreviewCase("UsageScreenContent · NETWORK · Selected") {
            UsageScreenContent(
                screen = UsageScreenKind.NETWORK,
                usage = usageState(UsageScreenKind.NETWORK).let { it.copy(selectedCategory = it.items.first().category) })
        },
    ),
)

/**
 * Renders named usage states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 840)
@Preview(name = "Dark", widthDp = 400, heightDp = 840, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 740, fontScale = 1.5f)
@Composable
private fun UsagePreview(@PreviewParameter(UsagePreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}

/**
 * Fixed category bytes for the shared chart and full-screen category list.
 *
 * @param screen Storage or traffic category set.
 * @author Dmitriy Moroz
 */
private fun usageState(screen: UsageScreenKind): UsageState = UsageState(
    items =
        (if (screen == UsageScreenKind.STORAGE) listOf(
            UsageCategory.CACHE,
            UsageCategory.GALLERY,
            UsageCategory.MODELS_ONNX,
            UsageCategory.MODELS_MNN
        )
        else listOf(
            UsageCategory.TRAFFIC_MODELS,
            UsageCategory.TRAFFIC_CONFIGS,
            UsageCategory.TRAFFIC_INFERENCE,
            UsageCategory.TRAFFIC_CIVITAI
        ))
            .mapIndexed { index, category ->
                UsageItem(
                    category,
                    listOf(104857600L, 52428800L, 1048576L, 0L)[index]
                )
            }
)
