package com.shifthackz.aisdv1.storage.db.persistent.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.shifthackz.aisdv1.storage.db.persistent.entity.BlogPostEntity

/**
 * Stores the last successfully downloaded blog feed for offline reading.
 *
 * @author Dmitriy Moroz
 */
@Dao
abstract class BlogDao {
    /**
     * Returns complete articles in the publisher's display order.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM blog_posts ORDER BY position")
    abstract suspend fun read(): List<BlogPostEntity>

    /**
     * Updates all saved articles in one transaction, rolling back if insertion fails.
     *
     * @param posts Replacement feed including the full article bodies.
     * @author Dmitriy Moroz
     */
    @Transaction
    open suspend fun replace(posts: List<BlogPostEntity>) {
        delete()
        insert(posts)
    }

    /**
     * Clears the previous revision within the replacement transaction.
     *
     * @author Dmitriy Moroz
     */
    @Query("DELETE FROM blog_posts")
    protected abstract suspend fun delete()

    /**
     * Inserts the ordered replacement revision after clearing the prior rows.
     *
     * @param posts Ordered articles from the publisher.
     * @author Dmitriy Moroz
     */
    @Insert
    protected abstract suspend fun insert(posts: List<BlogPostEntity>)
}
