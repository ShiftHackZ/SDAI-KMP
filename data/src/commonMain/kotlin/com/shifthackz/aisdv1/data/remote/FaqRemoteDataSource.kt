package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.datasource.FaqDataSource
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi

/**
 * Maps and validates the published website response before it can replace the FAQ cache.
 *
 * @param api Typed SDAI metadata client.
 * @author Dmitriy Moroz
 */
internal class FaqRemoteDataSource(
    private val api: SdaiAppApi,
) : FaqDataSource.Remote {
    override suspend fun fetch(): List<FaqCategory> {
        val categories = api.fetchFaq().categories
        require(categories.isNotEmpty() && categories.distinctBy { it.id }.size == categories.size) {
            "FAQ must contain uniquely identified categories"
        }
        return categories.map { category ->
            require(category.id.isNotBlank() && category.label.isNotBlank())
            require(category.items.isNotEmpty() && category.items.distinctBy { it.id }.size == category.items.size)
            val questions = category.items.map { item ->
                require(item.id.isNotBlank() && item.question.isNotBlank() && item.answer.any(String::isNotBlank))
                FaqQuestion(item.id, item.question, item.answer, item.tags)
            }
            FaqCategory(category.id, category.label, questions)
        }
    }
}
