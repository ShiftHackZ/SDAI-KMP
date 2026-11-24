package com.shifthackz.aisdv1.presentation.blog.content

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.BlogContent
import com.shifthackz.aisdv1.presentation.blog.detail.BlogDetailScreenContent
import com.shifthackz.aisdv1.presentation.blog.detail.BlogDetailState
import com.shifthackz.aisdv1.presentation.blog.list.BlogScreenContent
import com.shifthackz.aisdv1.presentation.blog.list.BlogState
import com.shifthackz.aisdv1.presentation.blog.releasePost
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Drives the production Blog list and reader with prepared states and Compose actions.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class BlogScreenContentTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var previousLanguage: String
    private val feed = BlogContent(listOf(releasePost), isCached = false)

    @Before
    fun selectEnglish() {
        previousLanguage = Localization.currentLanguageCode()
        Localization.setLanguageCode("en")
    }

    @After
    fun restoreLanguage() {
        Localization.setLanguageCode(previousLanguage)
    }

    @Test
    fun `given blog list with pending feed, screen is rendered, expected accessible loading and no retry`() {
        // Given
        showList(BlogState.Loading)

        // When
        compose.waitForIdle()

        // Then
        compose.onNodeWithContentDescription(Localization.string("loading")).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("retry")).assertDoesNotExist()
    }

    @Test
    fun `given blog list after download failure, Retry is clicked, expected retry action without a post card`() {
        // Given
        var retries = 0
        showList(BlogState.Error, onRetry = { retries++ })

        // When
        compose.onNodeWithText(Localization.string("retry")).performClick()

        // Then
        assertEquals(1, retries)
        compose.onNodeWithText(Localization.string("blog_load_error")).assertIsDisplayed()
        compose.onNodeWithText(releasePost.title).assertDoesNotExist()
    }

    @Test
    fun `given blog list displaying a saved snapshot, Retry is clicked, expected cached notice and feed remain accessible`() {
        // Given
        var retries = 0
        showList(BlogState.Ready(feed.copy(isCached = true)), onRetry = { retries++ })

        // When
        compose.onNodeWithText(Localization.string("retry")).performClick()

        // Then
        assertEquals(1, retries)
        compose.onNodeWithText(Localization.string("blog_cached")).assertIsDisplayed()
        compose.onNodeWithText(releasePost.title).assertIsDisplayed()
    }

    @Test
    fun `given blog list with a published release, card is clicked, expected release slug is sent to reader navigation`() {
        // Given
        var openedSlug: String? = null
        showList(BlogState.Ready(feed), onOpen = { openedSlug = it })

        // When
        compose.onNodeWithText(releasePost.title).performClick()

        // Then
        assertEquals(releasePost.slug, openedSlug)
        compose.onNodeWithText(releasePost.category, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText(releasePost.summary, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `given blog list with no published articles, feed is rendered, expected no error or cached notice`() {
        // Given
        showList(BlogState.Ready(BlogContent(emptyList(), isCached = false)))

        // When
        compose.waitForIdle()

        // Then
        compose.onNodeWithText(Localization.string("blog_title")).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("retry")).assertDoesNotExist()
        compose.onNodeWithText(Localization.string("blog_cached")).assertDoesNotExist()
    }

    @Test
    fun `given blog reader with selected article, share is clicked and body is scrolled into view, expected share action and rendered Markdown`() {
        // Given
        var shares = 0
        showReader(BlogDetailState.Ready(releasePost), onShare = { shares++ })

        // When
        compose.onNodeWithContentDescription(Localization.string("blog_share_post")).performClick()
        val body = compose.onNodeWithText("Generate offline with MNN.")
        body.performScrollTo()

        // Then
        assertEquals(1, shares)
        body.assertIsDisplayed()
    }

    @Test
    fun `given blog reader for an unpublished post, Retry is clicked, expected missing message and no share action`() {
        // Given
        var retries = 0
        showReader(BlogDetailState.Missing, onRetry = { retries++ })

        // When
        compose.onNodeWithText(Localization.string("retry")).performClick()

        // Then
        assertEquals(1, retries)
        compose.onNodeWithText(Localization.string("blog_post_missing")).assertIsDisplayed()
        compose.onNodeWithContentDescription(Localization.string("blog_share_post"))
            .assertDoesNotExist()
    }

    @Test
    fun `given blog reader after fetch error, screen is rendered, expected network error message with retry and no share action`() {
        // Given
        showReader(BlogDetailState.Error)

        // When
        compose.waitForIdle()

        // Then
        compose.onNodeWithText(Localization.string("blog_load_error")).assertIsDisplayed()
        compose.onNodeWithText(Localization.string("retry")).assertIsDisplayed()
        compose.onNodeWithContentDescription(Localization.string("blog_share_post"))
            .assertDoesNotExist()
    }

    private fun showList(
        state: BlogState,
        onRetry: () -> Unit = {},
        onOpen: (String) -> Unit = {},
    ) {
        compose.setContent {
            MaterialTheme {
                BlogScreenContent(state, onRetry = onRetry, onBack = {}, onOpenPost = onOpen)
            }
        }
    }

    private fun showReader(
        state: BlogDetailState,
        onRetry: () -> Unit = {},
        onShare: () -> Unit = {},
    ) {
        compose.setContent {
            MaterialTheme {
                BlogDetailScreenContent(
                    state = state,
                    onRetry = onRetry,
                    onBack = {},
                    onShare = onShare,
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }
}
