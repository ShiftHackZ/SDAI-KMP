package com.shifthackz.aisdv1.presentation.blog

import com.shifthackz.aisdv1.domain.entity.BlogPost

/**
 * Supplies one full release article to the list and reader scenarios.
 *
 * @author Dmitriy Moroz
 */
internal val releasePost = BlogPost(
    slug = "1.3.0-release",
    title = "SDAI 1.3.0",
    summary = "Generate offline.",
    publishedOn = "2026-11-24",
    category = "Release",
    url = "https://sdai.moroz.cc/blog/1.3.0-release/",
    coverUrl = "",
    coverDescription = "Release artwork",
    markdown = "Generate offline with **MNN**.",
)
