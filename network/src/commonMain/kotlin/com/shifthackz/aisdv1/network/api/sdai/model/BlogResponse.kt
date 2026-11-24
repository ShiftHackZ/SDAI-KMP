package com.shifthackz.aisdv1.network.api.sdai.model

import kotlinx.serialization.Serializable

/**
 * Public website blog feed, corresponding to the response at /blog.json.
 *
 * @param posts Published articles from newest to oldest.
 * @author Dmitriy Moroz
 */
@Serializable
data class BlogResponse(val posts: List<Post>) {
    /**
     * One article and its canonical publication metadata.
     *
     * @param slug Stable identifier from the article's website path.
     * @param title Human-readable headline.
     * @param summary Excerpt for the post list.
     * @param publishedOn ISO 8601 publication date.
     * @param category Category assigned by the publisher.
     * @param url Canonical HTTPS URL of the article.
     * @param coverUrl HTTPS URL of the article's cover image.
     * @param coverDescription Description of the cover artwork for assistive technology.
     * @param markdown Full GitHub-flavored Markdown body.
     * @author Dmitriy Moroz
     */
    @Serializable
    data class Post(
        val slug: String,
        val title: String,
        val summary: String,
        val publishedOn: String,
        val category: String,
        val url: String,
        val coverUrl: String,
        val coverDescription: String,
        val markdown: String,
    )
}
