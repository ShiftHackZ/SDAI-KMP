package com.shifthackz.aisdv1.data

import com.shifthackz.aisdv1.domain.entity.BlogPost

/**
 * A complete release article for exercising feed and snapshot mapping.
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
    coverUrl = "https://sdai.moroz.cc/blog/1.3.0-release/header.jpg",
    coverDescription = "Release artwork",
    markdown = "## Generate on your phone\n\nChoose **MNN**.",
)
