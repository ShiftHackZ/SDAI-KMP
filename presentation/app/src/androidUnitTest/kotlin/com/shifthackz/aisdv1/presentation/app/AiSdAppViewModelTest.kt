package com.shifthackz.aisdv1.presentation.app

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies the production shell projection preserves tab owners across overlay routes.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AiSdAppViewModelTest {
    private val stores = mutableListOf<ViewModelStore>()

    @After
    fun tearDown() {
        stores.forEach { it.clear() }
        Dispatchers.resetMain()
    }

    @Test
    fun `given home shell, user visits all tabs and returns, expected unique retained tab identities`() =
        runTest {
            // Given
            val router = RootAppRouter(AppRoute.TextToImage)
            val model = createModel(router, StandardTestDispatcher(testScheduler))
            advanceUntilIdle()
            // When
            router.navigateToImageToImage()
            advanceUntilIdle()
            router.navigateToGallery()
            advanceUntilIdle()
            router.navigateToSettings()
            advanceUntilIdle()
            router.navigateToTextToImage()
            advanceUntilIdle()
            // Then
            assertEquals(
                listOf(
                    AppRoute.TextToImage,
                    AppRoute.ImageToImage,
                    AppRoute.Gallery,
                    AppRoute.Settings
                ), model.state.value.renderedHomeRoutes
            )
            assertEquals(AppRoute.TextToImage, model.state.value.homeRoute)
            assertTrue(model.state.value.shouldShowHomeChrome)
            assertEquals(1, model.state.value.backStack.size)
        }

    @Test
    fun `given home shell, standalone backup opens, expected home owner retained while chrome is hidden`() =
        runTest {
            // Given
            val router = RootAppRouter(AppRoute.Settings)
            val model = createModel(router, StandardTestDispatcher(testScheduler))
            advanceUntilIdle()
            val homeEntry = model.state.value.backStack.single()
            // When
            router.navigateToBackup()
            advanceUntilIdle()
            // Then
            assertEquals(AppRoute.Settings, model.state.value.homeRoute)
            assertEquals(listOf(AppRoute.Settings), model.state.value.renderedHomeRoutes)
            assertEquals(homeEntry, model.state.value.backStack.first())
            assertFalse(model.state.value.shouldShowHomeChrome)
            assertTrue(model.state.value.canNavigateBack)
        }

    @Test
    fun `given backup over home shell, Back is requested, expected original home entry and chrome restored`() =
        runTest {
            // Given
            val router = RootAppRouter(AppRoute.Settings)
            val model = createModel(router, StandardTestDispatcher(testScheduler))
            advanceUntilIdle()
            val home = model.state.value.backStack.single()
            router.navigateToBackup()
            advanceUntilIdle()
            // When
            model.processIntent(AiSdAppIntent.NavigateBack)
            advanceUntilIdle()
            // Then
            assertEquals(listOf(home), model.state.value.backStack)
            assertTrue(model.state.value.shouldShowHomeChrome)
            assertEquals(AppRoute.Settings, model.state.value.route)
        }

    @Test
    fun `given home shell with open drawer, Back is requested, expected drawer closes without route replacement`() =
        runTest {
            // Given
            val router = RootAppRouter(AppRoute.Settings)
            val model = createModel(router, StandardTestDispatcher(testScheduler))
            advanceUntilIdle()
            val history = model.state.value.backStack
            router.openDrawer()
            advanceUntilIdle()
            assertTrue(model.state.value.isDrawerOpen)
            // When
            model.processIntent(AiSdAppIntent.NavigateBack)
            advanceUntilIdle()
            // Then
            assertFalse(model.state.value.isDrawerOpen)
            assertEquals(history, model.state.value.backStack)
        }

    @Test
    fun `given cold start shell, initial configuration is visible, expected no speculative home owner`() =
        runTest {
            // Given
            val router = RootAppRouter()
            val model = createModel(router, StandardTestDispatcher(testScheduler))
            // When
            router.navigateToInitialConfiguration()
            advanceUntilIdle()
            // Then
            assertEquals(emptyList<AppRoute>(), model.state.value.renderedHomeRoutes)
            assertEquals(null, model.state.value.homeRoute)
            assertFalse(model.state.value.shouldShowHomeChrome)
        }

    @Test
    fun `given foreground generation, background mode becomes enabled, expected foreground dim guard released`() =
        runTest {
            // Given
            val settings = MutableStateFlow(Settings(backgroundGeneration = false))
            val generating = MutableStateFlow(true)
            val model = createModel(
                RootAppRouter(AppRoute.TextToImage),
                StandardTestDispatcher(testScheduler),
                settings,
                generating
            )
            advanceUntilIdle()
            assertTrue(model.state.value.isForegroundGenerating)
            // When
            settings.value = settings.value.copy(backgroundGeneration = true)
            advanceUntilIdle()
            // Then
            assertFalse(model.state.value.isForegroundGenerating)
        }

    @Test
    fun `given home shell, generation stops, expected foreground generating state cleared`() =
        runTest {
            // Given
            val generating = MutableStateFlow(true)
            val model = createModel(
                RootAppRouter(AppRoute.TextToImage),
                StandardTestDispatcher(testScheduler),
                generating = generating
            )
            advanceUntilIdle()
            // When
            generating.value = false
            advanceUntilIdle()
            // Then
            assertFalse(model.state.value.isForegroundGenerating)
        }

    @Test
    fun `given home shell, screen dim preference changes, expected live preference projection`() =
        runTest {
            // Given
            val settings = MutableStateFlow(Settings(shouldDimScreen = false))
            val model = createModel(
                RootAppRouter(AppRoute.Settings),
                StandardTestDispatcher(testScheduler),
                settings
            )
            advanceUntilIdle()
            // When
            settings.value = settings.value.copy(shouldDimScreen = true)
            advanceUntilIdle()
            // Then
            assertTrue(model.state.value.shouldDimScreen)
        }

    /** Creates the actual shell model with controlled collaborator streams and a real router.
     * @param router Navigation authority under test.
     * @param dispatcher Shared scheduler for model work.
     * @param settings Preference stream supplied by the settings owner.
     * @param generating Activity from retained generation forms.
     * @author Dmitriy Moroz
     */
    private fun createModel(
        router: RootAppRouter,
        dispatcher: CoroutineDispatcher,
        settings: MutableStateFlow<Settings> = MutableStateFlow(Settings()),
        generating: MutableStateFlow<Boolean> = MutableStateFlow(false),
    ): AiSdAppViewModel {
        Dispatchers.setMain(dispatcher)
        val preferences = mockk<PreferenceManager> {
            every { shouldDimScreen } returns settings.value.shouldDimScreen
            every { observe() } returns settings
        }
        val dispatchers = mockk<DispatchersProvider> { every { immediate } returns dispatcher }
        return AiSdAppViewModel(
            mockk(relaxed = true),
            dispatchers,
            preferences,
            router,
            generating
        ).also { model ->
            stores.add(ViewModelStore().apply { put("shell", model) })
        }
    }
}
