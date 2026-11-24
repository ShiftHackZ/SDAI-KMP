package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.core.dataSourceResultOf
import com.shifthackz.aisdv1.domain.datasource.BlogDataSource
import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi

/**
 * Maps the website feed to domain articles in the order supplied by the publisher.
 *
 * @param api Typed client for the public SDAI blog feed.
 * @author Dmitriy Moroz
 */
internal class BlogRemoteDataSource(private val api: SdaiAppApi) : BlogDataSource.Remote {
    override suspend fun fetch(): Result<List<BlogPost>> = dataSourceResultOf {
        api.fetchBlog().posts.map { post ->
            BlogPost(
                slug = post.slug,
                title = post.title,
                summary = post.summary,
                publishedOn = post.publishedOn,
                category = post.category,
                url = post.url,
                coverUrl = post.coverUrl,
                coverDescription = post.coverDescription,
                markdown = post.markdown,
            )
        }
    }
}
