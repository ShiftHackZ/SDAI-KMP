package com.shifthackz.aisdv1.presentation.blog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.domain.entity.BlogContent
import com.shifthackz.aisdv1.domain.usecase.blog.FetchOrGetBlogPostUseCase
import com.shifthackz.aisdv1.domain.usecase.blog.FetchOrGetBlogUseCase
import com.shifthackz.aisdv1.presentation.blog.detail.BlogDetailScreen
import com.shifthackz.aisdv1.presentation.blog.detail.share.BlogPlatformActions
import com.shifthackz.aisdv1.presentation.blog.di.blogViewModelModule
import com.shifthackz.aisdv1.presentation.blog.list.BlogScreen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises both Blog routes with the production dependency module and lifecycle bindings.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class BlogRouteIntegrationTest {
    @get:Rule
    val compose = createComposeRule()

    private val feed = mockk<FetchOrGetBlogUseCase>()
    private val article = mockk<FetchOrGetBlogPostUseCase>()
    private val sharing = mockk<BlogPlatformActions>()
    private lateinit var previousLanguage: String

    @Before
    fun startBlogGraph() {
        previousLanguage = Localization.currentLanguageCode()
        Localization.setLanguageCode("en")
        startKoin {
            modules(
                blogViewModelModule,
                module {
                    single<UiDiagnostics> { mockk(relaxed = true) }
                    single<DispatchersProvider> { mockk { every { immediate } returns Dispatchers.Main.immediate } }
                    single<FetchOrGetBlogUseCase> { feed }
                    single<FetchOrGetBlogPostUseCase> { article }
                    single<BlogPlatformActions> { sharing }
                },
            )
        }
    }

    @After
    fun stopBlogGraph() {
        stopKoin()
        Localization.setLanguageCode(previousLanguage)
    }

    @Test
    fun `given native blog routes with a published feed, post is opened and sharing fails, expected selected article and snackbar`() {
        // Given
        coEvery { feed() } returns Result.success(
            BlogContent(
                listOf(releasePost),
                isCached = false
            )
        )
        coEvery { article(releasePost.slug, false) } returns Result.success(releasePost)
        every { sharing.sharePost(any()) } returns false
        var slug by mutableStateOf<String?>(null)
        compose.setContent {
            MaterialTheme {
                val selection = slug
                if (selection == null) {
                    BlogScreen(onBack = {}, onOpenPost = { slug = it })
                } else {
                    BlogDetailScreen(slug = selection, onBack = { slug = null })
                }
            }
        }

        // When
        compose.onNodeWithText(releasePost.title).performClick()
        compose.onNodeWithContentDescription(Localization.string("blog_share_post")).performClick()

        // Then
        compose.onNodeWithText(Localization.string("blog_share_error")).assertIsDisplayed()
        coVerify(exactly = 1) { article(releasePost.slug, false) }
    }
}
