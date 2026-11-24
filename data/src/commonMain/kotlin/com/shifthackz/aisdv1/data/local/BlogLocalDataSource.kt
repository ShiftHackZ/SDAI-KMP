package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.data.core.dataSourceResultOf
import com.shifthackz.aisdv1.domain.datasource.BlogDataSource
import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.storage.db.persistent.dao.BlogDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.BlogPostEntity

/**
 * Maps the blog feed to ordered Room rows and reconstructs complete articles for offline reading.
 *
 * @param dao Persistent reader and transactional snapshot writer.
 * @author Dmitriy Moroz
 */
internal class BlogLocalDataSource(private val dao: BlogDao) : BlogDataSource.Local {
    override suspend fun read(): Result<List<BlogPost>> = dataSourceResultOf {
        dao.read().map { post ->
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

    override suspend fun save(posts: List<BlogPost>): Result<Unit> = dataSourceResultOf {
        dao.replace(posts.mapIndexed { position, post ->
            BlogPostEntity(
                slug = post.slug,
                position = position,
                title = post.title,
                summary = post.summary,
                publishedOn = post.publishedOn,
                category = post.category,
                url = post.url,
                coverUrl = post.coverUrl,
                coverDescription = post.coverDescription,
                markdown = post.markdown,
            )
        })
    }
}
