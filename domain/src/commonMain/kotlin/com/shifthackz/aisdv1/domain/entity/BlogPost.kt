package com.shifthackz.aisdv1.domain.entity

/**
 * One published SDAI article and the assets needed by the native reader.
 *
 * @param slug Stable identifier used in navigation and offline storage.
 * @param title Published article headline.
 * @param summary Short preview shown in the list.
 * @param publishedOn Publication date in ISO 8601 calendar format.
 * @param category Publisher's classification, such as Release.
 * @param url Canonical HTTPS link included when sharing.
 * @param coverUrl HTTPS address of the article artwork.
 * @param coverDescription Alternative text describing the artwork.
 * @param markdown Article body in GitHub-flavored Markdown.
 * @author Dmitriy Moroz
 */
data class BlogPost(
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
