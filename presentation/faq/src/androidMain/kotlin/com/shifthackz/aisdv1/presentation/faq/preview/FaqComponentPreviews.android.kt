package com.shifthackz.aisdv1.presentation.faq.preview

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.faq.content.FaqFilters
import com.shifthackz.aisdv1.presentation.faq.content.FaqQuestionCard

/**
 * Uses the application palette and the preview's night mode without accessing system bars.
 *
 * @param content FAQ screen, category filter, or question card to render.
 * @author Dmitriy Moroz
 */
@Composable
internal fun FaqPreviewTheme(content: @Composable () -> Unit) {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}

@Preview(name = "All categories", group = "FAQ · Filters", widthDp = 360, heightDp = 440)
@Preview(
    name = "All categories · Dark", group = "FAQ · Filters", widthDp = 360, heightDp = 440,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FaqDefaultFiltersPreview() {
    FaqFiltersPreview(categoryId = null)
}

@Preview(name = "Category selected", group = "FAQ · Filters", widthDp = 360, heightDp = 440)
@Preview(
    name = "Category selected · Dark", group = "FAQ · Filters", widthDp = 360, heightDp = 440,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FaqSelectedFiltersPreview() {
    FaqFiltersPreview(categoryId = FaqPreviewFixtures.localCategoryId)
}

@Composable
private fun FaqFiltersPreview(categoryId: String?) {
    FaqPreviewTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            FaqFilters(
                categories = FaqPreviewFixtures.categories,
                selectedCategory = categoryId,
                onCategoryChange = {},
            )
        }
    }
}

@Preview(name = "Collapsed", group = "FAQ · Question", widthDp = 360, heightDp = 112)
@Preview(
    name = "Collapsed · Dark", group = "FAQ · Question", widthDp = 360, heightDp = 112,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FaqCollapsedQuestionPreview() {
    FaqQuestionPreview(isExpanded = false)
}

@Preview(name = "Expanded", group = "FAQ · Question", widthDp = 360, heightDp = 400)
@Preview(
    name = "Expanded · Dark", group = "FAQ · Question", widthDp = 360, heightDp = 400,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FaqExpandedQuestionPreview() {
    FaqQuestionPreview(isExpanded = true)
}

@Composable
private fun FaqQuestionPreview(isExpanded: Boolean) {
    FaqPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            FaqQuestionCard(
                question = FaqPreviewFixtures.longQuestion,
                isExpanded = isExpanded,
                onToggle = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
