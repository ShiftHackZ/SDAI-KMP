package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi
import com.shifthackz.aisdv1.network.api.sdai.model.FaqResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects validation and ordered mapping at the FAQ HTTP-to-domain boundary.
 *
 * @author Dmitriy Moroz
 */
class FaqRemoteDataSourceTest {
    private val api = mockk<SdaiAppApi>()
    private val source = FaqRemoteDataSource(api)
    private val question = FaqResponse.Question(
        id = "connect",
        question = "How do I connect?",
        answer = listOf("Open Configuration.", "Enter the URL."),
        tags = listOf("server", "setup"),
    )
    private val category = FaqResponse.Category("setup", "Setup", listOf(question))

    @Test
    fun `given FAQ remote data source and published categories, feed is fetched, expected ordered domain questions`() =
        runTest {
            // Given
            val second = question.copy(id = "url", question = "What URL?")
            val otherCategory = category.copy(id = "basics", label = "Basics")
            coEvery { api.fetchFaq() } returns FaqResponse(
                listOf(
                    category.copy(
                        items = listOf(
                            question,
                            second
                        )
                    ), otherCategory
                )
            )
            val domainQuestion =
                FaqQuestion("connect", "How do I connect?", question.answer, question.tags)

            // When
            val actual = source.fetch()

            // Then
            assertEquals(
                listOf(
                    FaqCategory(
                        "setup",
                        "Setup",
                        listOf(
                            domainQuestion,
                            domainQuestion.copy(id = "url", question = "What URL?")
                        )
                    ),
                    FaqCategory("basics", "Basics", listOf(domainQuestion)),
                ),
                actual,
            )
        }

    @Test
    fun `given FAQ remote data source and invalid category structure, feed is fetched, expected every invalid revision is rejected`() =
        runTest {
            // Given
            val invalid = mapOf(
                "empty feed" to emptyList(),
                "duplicate categories" to listOf(category, category.copy(label = "Duplicate")),
                "blank category ID" to listOf(category.copy(id = " ")),
                "blank category label" to listOf(category.copy(label = " ")),
                "empty category" to listOf(category.copy(items = emptyList())),
                "duplicate question IDs" to listOf(
                    category.copy(
                        items = listOf(
                            question,
                            question.copy(question = "Duplicate")
                        )
                    )
                ),
            )

            // When
            val failures = invalid.mapValues { (_, categories) ->
                coEvery { api.fetchFaq() } returns FaqResponse(categories)
                runCatching { source.fetch() }.exceptionOrNull()
            }

            // Then
            failures.forEach { (scenario, error) ->
                assertTrue(
                    scenario,
                    error is IllegalArgumentException
                )
            }
        }

    @Test
    fun `given FAQ remote data source and incomplete question, feed is fetched, expected unanswered or unidentified questions are rejected`() =
        runTest {
            // Given
            val variants = mapOf(
                "blank question ID" to question.copy(id = " "),
                "blank question" to question.copy(question = " "),
                "empty answer" to question.copy(answer = emptyList()),
                "blank answer" to question.copy(answer = listOf(" ", "")),
            )

            // When
            val failures = variants.mapValues { (_, item) ->
                coEvery { api.fetchFaq() } returns FaqResponse(
                    listOf(
                        category.copy(
                            items = listOf(
                                item
                            )
                        )
                    )
                )
                runCatching { source.fetch() }.exceptionOrNull()
            }

            // Then
            failures.forEach { (scenario, error) ->
                assertTrue(
                    scenario,
                    error is IllegalArgumentException
                )
            }
        }

    @Test
    fun `given FAQ remote data source and API failure, feed is fetched, expected original failure propagates`() =
        runTest {
            // Given
            val failure = IllegalStateException("Transport failed")
            coEvery { api.fetchFaq() } throws failure

            // When
            val result = runCatching { source.fetch() }

            // Then
            assertSame(failure, result.exceptionOrNull())
        }
}
