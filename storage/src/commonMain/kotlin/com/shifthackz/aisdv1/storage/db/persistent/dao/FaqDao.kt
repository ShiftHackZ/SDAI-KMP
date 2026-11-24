package com.shifthackz.aisdv1.storage.db.persistent.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.shifthackz.aisdv1.storage.db.persistent.entity.FaqQuestionEntity

/**
 * Stores one complete FAQ revision in the persisted Room database.
 *
 * @author Dmitriy Moroz
 */
@Dao
abstract class FaqDao {
    /**
     * Returns saved questions in the publisher's category and question order.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM faq_questions ORDER BY categoryPosition, questionPosition")
    abstract suspend fun read(): List<FaqQuestionEntity>

    /**
     * Replaces all cached questions atomically and rolls back if insertion fails.
     *
     * @param questions Complete set of rows for the newly fetched revision.
     * @author Dmitriy Moroz
     */
    @Transaction
    open suspend fun replace(questions: List<FaqQuestionEntity>) {
        delete()
        insert(questions)
    }

    /**
     * Clears the previous revision inside [replace]'s transaction.
     *
     * @author Dmitriy Moroz
     */
    @Query("DELETE FROM faq_questions")
    protected abstract suspend fun delete()

    /**
     * Inserts the replacement rows inside [replace]'s transaction.
     *
     * @param questions Ordered FAQ translated to persisted rows.
     * @author Dmitriy Moroz
     */
    @Insert
    protected abstract suspend fun insert(questions: List<FaqQuestionEntity>)
}
