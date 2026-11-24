package com.shifthackz.aisdv1.presentation.blog.detail.preview

import android.content.res.Configuration
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.blog.detail.BlogDetailScreenContent
import com.shifthackz.aisdv1.presentation.blog.detail.BlogDetailState
import com.shifthackz.aisdv1.presentation.blog.preview.BlogPreviewFixtures
import com.shifthackz.aisdv1.presentation.blog.preview.BlogPreviewTheme

/**
 * Pairs the article state with optional feedback from its share action.
 *
 * @param name Label shown in the IDE preview picker.
 * @param state Detail state rendered with inert actions.
 * @param shouldShowShareFailure Whether to display the share failure snackbar.
 * @author Dmitriy Moroz
 */
internal data class BlogDetailPreviewCase(
    val name: String,
    val state: BlogDetailState,
    val shouldShowShareFailure: Boolean = false,
)

/**
 * Names each reader state for the light and dark design surfaces.
 *
 * @author Dmitriy Moroz
 */
internal class BlogDetailPreviewProvider : PreviewParameterProvider<BlogDetailPreviewCase> {
    private val cases = listOf(
        BlogDetailPreviewCase("Loading", BlogDetailState.Loading),
        BlogDetailPreviewCase("Load error", BlogDetailState.Error),
        BlogDetailPreviewCase("Missing article", BlogDetailState.Missing),
        BlogDetailPreviewCase("Article", BlogPreviewFixtures.article),
        BlogDetailPreviewCase("Unavailable cover", BlogPreviewFixtures.articleWithoutCover),
        BlogDetailPreviewCase(
            "Share failure",
            BlogPreviewFixtures.article,
            shouldShowShareFailure = true
        ),
    )

    override val values: Sequence<BlogDetailPreviewCase> = cases.asSequence()

    override fun getDisplayName(index: Int): String = cases[index].name
}

/**
 * Shows the reader, loading states, and share feedback in both palettes.
 *
 * @param case Named article state and optional share feedback to render.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Blog · Detail", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Blog · Detail", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BlogDetailStatesPreview(
    @PreviewParameter(BlogDetailPreviewProvider::class) case: BlogDetailPreviewCase,
) {
    BlogDetailPreview(state = case.state, shouldShowShareFailure = case.shouldShowShareFailure)
}

/**
 * Shows the wrapping article heading and cover on a narrow phone with larger text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Large text",
    group = "Blog · Detail",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun BlogDetailCompactPreview() {
    BlogDetailPreview(BlogPreviewFixtures.article)
}

/**
 * Shows the reader toolbar and content width in landscape.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Landscape", group = "Blog · Detail", widthDp = 820, heightDp = 420)
@Composable
private fun BlogDetailLandscapePreview() {
    BlogDetailPreview(BlogPreviewFixtures.article)
}

@Composable
private fun BlogDetailPreview(state: BlogDetailState, shouldShowShareFailure: Boolean = false) {
    BlogPreviewTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val message = Localization.string("blog_share_error")
        LaunchedEffect(shouldShowShareFailure, message) {
            if (shouldShowShareFailure) {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Indefinite
                )
            }
        }
        BlogDetailScreenContent(
            state = state,
            onRetry = {},
            onBack = {},
            onShare = {},
            snackbarHostState = snackbarHostState,
        )
    }
}
