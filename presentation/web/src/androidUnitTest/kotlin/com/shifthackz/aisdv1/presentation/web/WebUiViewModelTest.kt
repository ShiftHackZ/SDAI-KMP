package com.shifthackz.aisdv1.presentation.web

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.core.navigation.router.WebUiRouter
import com.shifthackz.aisdv1.presentation.web.stub.stubDispatchersProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WebUiViewModelTest {

    private val stubRouter = mockk<WebUiRouter>()
    private val stubPreferenceManager = mockk<PreferenceManager>()

    @Before
    fun initialize() {
        every {
            stubPreferenceManager.source
        } returns ServerSource.AUTOMATIC1111

        every {
            stubPreferenceManager.automatic1111ServerUrl
        } returns "https://5598.is.my.favourite.com"
    }

    @Test
    fun `given web ui, initialized, expected UI state changed with correct stub values from preference`() =
        runTest {
            // Given

            val expected = WebUiState(
                loading = false,
                source = ServerSource.AUTOMATIC1111,
                url = "https://5598.is.my.favourite.com",
            )
            // When
            val actual = createViewModel().state.value
            // Then
            Assert.assertEquals(expected, actual)

        }

    @Test
    fun `given web ui, initialized with Swarm UI source, expected UI state uses swarm URL`() =
        runTest {
            // Given

            every {
                stubPreferenceManager.source
            } returns ServerSource.SWARM_UI
            every {
                stubPreferenceManager.swarmUiServerUrl
            } returns "https://swarm.example.com"

            val expected = WebUiState(
                loading = false,
                source = ServerSource.SWARM_UI,
                url = "https://swarm.example.com",
            )
            // When
            val actual = createViewModel().state.value
            // Then
            Assert.assertEquals(expected, actual)

        }

    @Test
    fun `given web ui, received NavigateBack intent, expected router navigateBack called`() =
        runTest {
            // Given

            every {
                stubRouter.navigateBack()
            } returns Unit

            // When
            createViewModel().processIntent(WebUiIntent.NavigateBack)

            // Then
            verify {
                stubRouter.navigateBack()
            }

        }

    private fun TestScope.createViewModel() = WebUiViewModel(
        diagnostics = mockk(relaxed = true),
        dispatchersProvider = stubDispatchersProvider,
        preferenceManager = stubPreferenceManager,
        router = stubRouter,
    )
}
