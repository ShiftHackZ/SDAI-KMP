package com.shifthackz.aisdv1.presentation.blog.list.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.blog.list.BlogScreenContent
import com.shifthackz.aisdv1.presentation.blog.list.BlogState
import com.shifthackz.aisdv1.presentation.blog.preview.BlogPreviewFixtures
import com.shifthackz.aisdv1.presentation.blog.preview.BlogPreviewTheme

/**
 * Names each state of the blog list for the IDE's preview picker.
 *
 * @author Dmitriy Moroz
 */
internal class BlogListPreviewProvider : PreviewParameterProvider<BlogState> {
    private val cases = listOf(
        "Loading" to BlogState.Loading,
        "Load error" to BlogState.Error,
        "Latest articles" to BlogPreviewFixtures.onlineFeed,
        "Saved feed" to BlogPreviewFixtures.cachedFeed,
        "Unavailable cover" to BlogPreviewFixtures.feedWithoutCover,
    )

    override val values: Sequence<BlogState> = cases.asSequence().map { it.second }

    override fun getDisplayName(index: Int): String = cases[index].first
}

/**
 * Shows the feed, saved copy, loading, and recovery states in both palettes.
 *
 * @param state Blog list state rendered with inert actions.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Blog · List", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Blog · List", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BlogListStatesPreview(
    @PreviewParameter(BlogListPreviewProvider::class) state: BlogState,
) {
    BlogListPreview(state)
}

/**
 * Shows a saved feed and Retry action on a narrow phone with larger text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Large text",
    group = "Blog · List",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun BlogListCompactPreview() {
    BlogListPreview(BlogPreviewFixtures.cachedFeed)
}

/**
 * Shows the centered feed and article card width at a wider screen size.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Landscape", group = "Blog · List", widthDp = 820, heightDp = 420)
@Composable
private fun BlogListLandscapePreview() {
    BlogListPreview(BlogPreviewFixtures.onlineFeed)
}

@Composable
private fun BlogListPreview(state: BlogState) {
    BlogPreviewTheme {
        BlogScreenContent(state = state, onRetry = {}, onBack = {}, onOpenPost = {})
    }
}
