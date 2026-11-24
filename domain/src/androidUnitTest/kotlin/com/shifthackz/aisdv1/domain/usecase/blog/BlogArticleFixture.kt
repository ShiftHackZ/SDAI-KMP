package com.shifthackz.aisdv1.domain.usecase.blog

import com.shifthackz.aisdv1.domain.entity.BlogPost

/**
 * An article with a versioned publication slug and a complete reader payload.
 *
 * @author Dmitriy Moroz
 */
internal val releaseArticle = BlogPost(
    slug = "1.3.0-release",
    title = "SDAI 1.3.0",
    summary = "Generate offline.",
    publishedOn = "2026-11-24",
    category = "Release",
    url = "https://sdai.moroz.cc/blog/1.3.0-release/",
    coverUrl = "https://sdai.moroz.cc/blog/1.3.0-release/header.jpg",
    coverDescription = "Release artwork",
    markdown = "Generate offline with **MNN**.",
)
