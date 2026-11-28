package com.shifthackz.aisdv1.presentation.faq.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.input.SearchFilterToolbar
import com.shifthackz.aisdv1.presentation.components.widget.input.SearchFilterToolbarHeight
import com.shifthackz.aisdv1.presentation.components.widget.sheet.PlatformModalBottomSheet
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.faq.FaqState

/**
 * Renders supplied FAQ data, search controls, and the category sheet independently of loading services.
 *
 * @param state Loading, error, or ready content to display.
 * @param query Search text retained by the route.
 * @param categoryId Category filter retained by the route, or null for all categories.
 * @param onQueryChange Receives search edits and clearing.
 * @param onCategoryChange Receives the selected category or null when reset.
 * @param onRetry Requests a fresh download after a failure or while viewing saved answers.
 * @param onBack Returns to the previous route.
 * @param modifier Placement of the full-screen scaffold.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FaqScreenContent(
    state: FaqState,
    query: String,
    categoryId: String?,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFilterSheetOpen by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val ready = state as? FaqState.Ready
    val categories = ready?.content?.categories.orEmpty()
    val selectedCategory = categoryId?.takeIf { id -> categories.any { it.id == id } }

    PlatformScaffold(
        modifier = modifier,
        canUnderlapToolbar = true,
        topBar = {
            PlatformTopAppBar(
                title = {
                    Text(
                        text = Localization.string("faq_title"),
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                    )
                },
                navigationIcon = {
                    ToolbarBackButton(
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    )
                },
                windowInsets = persistentTopAppBarWindowInsets(),
                bottomContentHeight = if (ready == null) 0.dp else SearchFilterToolbarHeight,
                bottomContent = {
                    SearchFilterToolbar(
                        query = query,
                        searchHint = Localization.string("faq_search"),
                        clearLabel = Localization.string("action_clear"),
                        filterLabel = Localization.string("provider_action_filters"),
                        hasActiveFilters = selectedCategory != null,
                        onQueryChange = {
                            onQueryChange(it)
                            listState.requestScrollToItem(0)
                        },
                        onOpenFilters = { isFilterSheetOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        },
    ) { padding ->
        if (ready == null) ToolbarScrollObserver(false)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            val contentModifier = Modifier.fillMaxWidth()
            val statusModifier = contentModifier.padding(top = LocalToolbarContentInset.current)
            when (state) {
                FaqState.Loading -> FaqLoading(modifier = statusModifier)
                FaqState.Error -> FaqLoadError(
                    onRetry = onRetry,
                    modifier = statusModifier.fillMaxSize()
                )

                is FaqState.Ready -> FaqBrowser(
                    content = state.content,
                    query = query,
                    categoryId = selectedCategory,
                    listState = listState,
                    onRetry = onRetry,
                    modifier = contentModifier,
                )
            }
        }
    }
    if (ready != null && isFilterSheetOpen) {
        PlatformModalBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RectangleShape,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            FaqFilters(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategoryChange = {
                    onCategoryChange(it)
                    listState.requestScrollToItem(0)
                },
            )
        }
    }
}
