package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One complete article from the saved blog feed.
 *
 * @param slug Stable article identifier.
 * @param position Ordering of the article in the published feed.
 * @param title Published headline.
 * @param summary Excerpt shown in the list.
 * @param publishedOn ISO 8601 publication date.
 * @param category Publisher's category label.
 * @param url Canonical public article link.
 * @param coverUrl URL of the cover image.
 * @param coverDescription Accessible description of the cover artwork.
 * @param markdown Full article text in Markdown.
 * @author Dmitriy Moroz
 */
@Entity(tableName = "blog_posts")
data class BlogPostEntity(
    @PrimaryKey val slug: String,
    val position: Int,
    val title: String,
    val summary: String,
    val publishedOn: String,
    val category: String,
    val url: String,
    val coverUrl: String,
    val coverDescription: String,
    val markdown: String,
)
