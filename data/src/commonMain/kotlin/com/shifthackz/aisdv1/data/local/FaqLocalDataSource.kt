package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.domain.datasource.FaqDataSource
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import com.shifthackz.aisdv1.storage.db.persistent.dao.FaqDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.FaqQuestionEntity

/**
 * Translates domain categories to ordered Room rows and reconstructs the saved groups on read.
 *
 * @param dao Persistent snapshot reader and transactional writer.
 * @author Dmitriy Moroz
 */
internal class FaqLocalDataSource(
    private val dao: FaqDao,
) : FaqDataSource.Local {
    override suspend fun read(): List<FaqCategory> =
        dao.read().groupBy(FaqQuestionEntity::categoryId).map { (id, rows) ->
            FaqCategory(
                id = id,
                label = rows.first().categoryLabel,
                items = rows.map { FaqQuestion(it.questionId, it.question, it.answer, it.tags) },
            )
        }

    override suspend fun save(categories: List<FaqCategory>) {
        val questions = categories.flatMapIndexed { categoryIndex, category ->
            category.items.mapIndexed { questionIndex, item ->
                FaqQuestionEntity(
                    categoryId = category.id,
                    questionId = item.id,
                    categoryLabel = category.label,
                    categoryPosition = categoryIndex,
                    questionPosition = questionIndex,
                    question = item.question,
                    answer = item.answer,
                    tags = item.tags,
                )
            }
        }
        dao.replace(questions)
    }
}
