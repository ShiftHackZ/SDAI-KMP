package com.shifthackz.aisdv1.presentation.blog.list.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.presentation.blog.list.BlogPostCard
import com.shifthackz.aisdv1.presentation.blog.preview.BlogPreviewFixtures
import com.shifthackz.aisdv1.presentation.blog.preview.BlogPreviewTheme

/**
 * Shows wrapping headlines and excerpts on a populated article card.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Long headline", group = "Blog · Card", widthDp = 360)
@Preview(
    name = "Long headline · Dark", group = "Blog · Card", widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BlogPostCardPreview() {
    BlogCardPreview(BlogPreviewFixtures.releasePost)
}

/**
 * Shows the cover placeholder when the publisher's image cannot load.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Unavailable cover", group = "Blog · Card", widthDp = 360)
@Preview(
    name = "Unavailable cover · Dark", group = "Blog · Card", widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BlogPostCardUnavailableCoverPreview() {
    BlogCardPreview(BlogPreviewFixtures.postWithoutCover)
}

@Composable
private fun BlogCardPreview(post: BlogPost) {
    BlogPreviewTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            BlogPostCard(post = post, onOpen = {})
        }
    }
}
