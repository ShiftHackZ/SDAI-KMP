package com.shifthackz.aisdv1.presentation.blog.content

/**
 * Formats an ISO publication date using the app language and the device region when they match.
 * Keeps the original text if the date cannot be parsed.
 *
 * @param isoDate Date-only value from the blog feed.
 * @param languageCode Active app language.
 * @author Dmitriy Moroz
 */
internal expect fun formatBlogPostDate(isoDate: String, languageCode: String): String
