package com.shifthackz.aisdv1.presentation.blog.preview

import com.shifthackz.aisdv1.domain.entity.BlogContent
import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.presentation.blog.detail.BlogDetailState
import com.shifthackz.aisdv1.presentation.blog.list.BlogState

/**
 * Provides release and engineering posts with fixed text and local preview image responses.
 *
 * @author Dmitriy Moroz
 */
internal object BlogPreviewFixtures {
    val unavailableCoverUrl = "https://preview.invalid/blog/cover-unavailable.jpg"

    val releasePost = BlogPost(
        slug = "release-notes",
        title = "SDAI 1.3.0 brings new local generation and sharing options to mobile",
        summary = "Read about offline image generation, new providers, and publishing from the gallery.",
        publishedOn = "2026-11-24",
        category = "Release",
        url = "https://preview.invalid/blog/release-notes/",
        coverUrl = "https://preview.invalid/blog/release-notes/cover.jpg",
        coverDescription = "Sample cover for the release notes",
        markdown = """
            Create images offline with **MNN** or connect to a compatible server.

            ## What changed

            - Download a local model for offline generation.
            - Share gallery images or publish them to Civitai.
            - Back up selected parts of your setup.

            Read the [setup guide](/mnn.html) to get started.

            ### Before you update

            1. Save any settings you want to keep.
            2. Review the model storage requirements.
        """.trimIndent(),
    )

    val engineeringPost = releasePost.copy(
        slug = "offline-reading",
        title = "How SDAI keeps articles available offline",
        summary = "A look at how the reader stores the latest published articles.",
        publishedOn = "2026-11-18",
        category = "Engineering",
        url = "https://preview.invalid/blog/offline-reading/",
        coverUrl = "https://preview.invalid/blog/offline-reading/cover.jpg",
        coverDescription = "Sample cover for an engineering article",
        markdown = """
            The reader saves complete articles after a successful download.

            Your device can display the last saved copy when the website is unavailable.
        """.trimIndent(),
    )

    val postWithoutCover = engineeringPost.copy(coverUrl = unavailableCoverUrl)
    val onlineFeed =
        BlogState.Ready(BlogContent(listOf(releasePost, engineeringPost), isCached = false))
    val cachedFeed = BlogState.Ready(onlineFeed.content.copy(isCached = true))
    val feedWithoutCover = BlogState.Ready(BlogContent(listOf(postWithoutCover), isCached = false))
    val article = BlogDetailState.Ready(releasePost)
    val articleWithoutCover = BlogDetailState.Ready(postWithoutCover)
}
