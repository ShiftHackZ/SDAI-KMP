package com.shifthackz.aisdv1.presentation.statistics.storageusage

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory
import com.shifthackz.aisdv1.presentation.statistics.preview.StatisticsPreviewTheme

/**
 * Shows storage loading, totals, category selections and all destructive confirmation variants.
 * No preview action deletes cache, gallery entries or downloaded models.
 *
 * @param state Fixed storage snapshot and optional confirmation supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Storage usage", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Storage usage", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StorageUsageStatesPreview(
    @PreviewParameter(StorageUsagePreviewParameterProvider::class) state: StorageUsageState,
) {
    StatisticsPreviewTheme {
        StorageUsageScreenContent(state = state)
    }
}

/**
 * Checks selected gallery usage and clear controls on a narrow screen with enlarged text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Large text",
    group = "Storage usage",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun StorageUsageCompactPreview() {
    val state = StorageUsagePreviewParameterProvider().values.first {
        it.usage.selectedCategory == UsageCategory.GALLERY && it.screenModal == StorageUsageModal.None
    }
    StatisticsPreviewTheme {
        StorageUsageScreenContent(state = state)
    }
}
