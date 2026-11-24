package com.shifthackz.aisdv1.presentation.blog.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.presentation.blog.content.BlogCover
import com.shifthackz.aisdv1.presentation.blog.content.BlogPostMetadata
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver

/**
 * Renders the article in native Compose, resolving relative links against the published site.
 *
 * @param post Article displayed in the reader.
 * @param modifier Placement and width of the scrolling document.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogArticle(post: BlogPost, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val toolbarInset = LocalToolbarContentInset.current
    val typography = MaterialTheme.typography
    val articleTitle = typography.headlineLarge.copy(
        fontSize = 33.sp,
        lineHeight = 37.sp,
        fontWeight = FontWeight.Bold,
    )
    val articleText = typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 30.sp)
    val platformUriHandler = LocalUriHandler.current
    val uriHandler = remember(post.url, post.slug, platformUriHandler) {
        val site = post.url.removeSuffix("/blog/${post.slug}/")
        object : UriHandler {
            override fun openUri(uri: String) {
                val target = when {
                    uri.startsWith("https://", ignoreCase = true) -> uri
                    uri.startsWith("/") && !uri.startsWith("//") -> site + uri
                    uri.startsWith("#") -> post.url + uri
                    else -> return
                }
                try {
                    platformUriHandler.openUri(target)
                } catch (_: IllegalArgumentException) {
                    // A malformed link should never interrupt reading.
                }
            }
        }
    }
    ToolbarScrollObserver(scrollState.canScrollBackward)
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(start = 16.dp, top = 16.dp + toolbarInset, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BlogPostMetadata(post)
        Text(
            text = post.title,
            style = articleTitle,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        BlogCover(
            url = post.coverUrl,
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            description = post.coverDescription.takeIf(String::isNotBlank),
        )
        CompositionLocalProvider(LocalUriHandler provides uriHandler) {
            Markdown(
                content = post.markdown,
                typography = markdownTypography(
                    h1 = articleTitle,
                    h2 = typography.headlineSmall.copy(
                        fontSize = 26.sp,
                        lineHeight = 33.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    h3 = typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    text = articleText,
                    paragraph = articleText,
                    ordered = articleText,
                    bullet = articleText,
                    list = articleText,
                ),
            )
        }
    }
}
