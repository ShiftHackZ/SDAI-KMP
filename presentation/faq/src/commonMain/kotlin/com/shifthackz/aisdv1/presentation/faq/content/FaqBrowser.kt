package com.shifthackz.aisdv1.presentation.faq.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqContent
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsHeaderContent
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsListDefaults
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver

/**
 * Renders FAQ questions under shared Settings section headers, below the fixed platform toolbar.
 *
 * @param content Published categories and their online or cached origin.
 * @param query Current search text, including unsubmitted edits.
 * @param categoryId Selected category slug or null for all categories.
 * @param listState Scroll position shared with the fixed toolbar.
 * @param onRetry Requests updated content while viewing the saved copy.
 * @param modifier Placement of the scrolling list.
 * @author Dmitriy Moroz
 */
@Composable
internal fun FaqBrowser(
    content: FaqContent,
    query: String,
    categoryId: String?,
    listState: LazyListState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = content.categories
    val results = remember(categories, categoryId, query) {
        categories.filtered(categoryId, query)
    }
    val toolbarInset = LocalToolbarContentInset.current
    ToolbarScrollObserver(results.isNotEmpty() && listState.canScrollBackward)
    if (results.isEmpty()) {
        FaqNoResults(
            isCached = content.isCached,
            onRetry = onRetry,
            modifier = modifier.padding(top = toolbarInset),
        )
        return
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().verticalScrollbar(listState, topInset = toolbarInset),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = toolbarInset,
            end = 16.dp,
            bottom = 24.dp,
        ),
    ) {
        if (content.isCached) {
            item(key = "cached") {
                FaqCachedNotice(onRetry = onRetry, modifier = Modifier.padding(top = 16.dp))
            }
        }
        results.forEach { category ->
            item(key = "category:${category.id}", contentType = "heading") {
                SettingsHeaderContent(
                    text = category.label,
                    modifier = Modifier
                        .padding(SettingsListDefaults.headerPadding)
                        .semantics(mergeDescendants = true) { heading() },
                )
            }
            items(
                items = category.items,
                key = { "question:${category.id.length}:${category.id}:${it.id}" },
                contentType = { "question" },
            ) { question ->
                var isExpanded by rememberSaveable { mutableStateOf(false) }
                FaqQuestionCard(
                    question = question,
                    isExpanded = isExpanded,
                    onToggle = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth().padding(SettingsListDefaults.itemPadding),
                )
            }
        }
    }
}

private fun List<FaqCategory>.filtered(categoryId: String?, query: String): List<FaqCategory> {
    val terms = query.trim().split(Regex("""\s+""")).filter(String::isNotEmpty)
    return filter { categoryId == null || it.id == categoryId }.mapNotNull { category ->
        if (terms.isEmpty()) return@mapNotNull category

        val matching = category.items.filter { item ->
            val fields = listOf(category.label, item.question) + item.answer + item.tags
            terms.all { term -> fields.any { it.contains(term, ignoreCase = true) } }
        }
        category.copy(items = matching).takeIf { matching.isNotEmpty() }
    }
}
