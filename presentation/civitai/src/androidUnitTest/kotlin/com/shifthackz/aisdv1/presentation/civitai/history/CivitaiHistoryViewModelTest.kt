package com.shifthackz.aisdv1.presentation.civitai.history

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.domain.repository.CivitaiUploadHistoryRepository
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryMemoryBudget
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalCoroutinesApi::class)
class CivitaiHistoryViewModelTest {
    private val repository = mockk<CivitaiUploadHistoryRepository>()
    private val router = mockk<CivitaiRouter>(relaxed = true)
    private val budget = GalleryMemoryBudget(2, 1024, 1, 1024, 512, 3, 1024, 30, 5)

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given empty local history, route loads without account access, expected full empty-state data`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val dispatchers =
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
            every { repository.observeIds() } returns flowOf(emptyList())
            every { repository.observePage(30, 0) } returns flowOf(emptyList())
            val vm = CivitaiHistoryViewModel(
                repository,
                dispatchers,
                mockk(relaxed = true),
                router,
                budget
            )
            assertTrue(vm.state.value.isLoading)
            // When
            advanceUntilIdle()
            // Then
            assertFalse(vm.state.value.isLoading)
            assertFalse(vm.state.value.hasFailed)
            assertTrue(vm.state.value.itemIds.isEmpty())
            vm.onRouteDisposed()
        }

    @Test
    fun `given large history, viewport moves, expected bounded prefetched page replaces old metadata`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val dispatchers =
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
            every { repository.observeIds() } returns flowOf((1L..500L).toList())
            every { repository.observePage(30, any()) } answers {
                val start = secondArg<Int>()
                flowOf((start until start + 30).map {
                    CivitaiUpload(
                        it.toLong(),
                        7,
                        null,
                        200,
                        100,
                        "revision",
                        CivitaiUploadStatus.FAILED,
                        id = it.toLong()
                    )
                })
            }
            val vm = CivitaiHistoryViewModel(
                repository,
                dispatchers,
                mockk(relaxed = true),
                router,
                budget
            )
            advanceUntilIdle()
            // When
            vm.processIntent(CivitaiHistoryIntent.Viewport(100))
            advanceUntilIdle()
            // Then
            verify { repository.observePage(30, 95) }
            assertEquals(30, vm.state.value.uploads.size)
            assertEquals(95L, vm.state.value.uploads.first().id)
            assertEquals(500, vm.state.value.itemIds.size)
            vm.onRouteDisposed()
        }

    @Test
    fun `given failing history storage, user retries, expected error cleared and fresh IDs loaded`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val dispatchers =
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
            every { repository.observeIds() } returns flow { throw IllegalStateException("storage") }
            every { repository.observePage(any(), any()) } returns flowOf(emptyList())
            val vm = CivitaiHistoryViewModel(
                repository,
                dispatchers,
                mockk(relaxed = true),
                router,
                budget
            )
            advanceUntilIdle()
            assertTrue(vm.state.value.hasFailed)
            every { repository.observeIds() } returns flowOf(listOf(7L))
            // When
            vm.processIntent(CivitaiHistoryIntent.Retry)
            advanceUntilIdle()
            // Then
            assertFalse(vm.state.value.hasFailed)
            assertEquals(listOf(7L), vm.state.value.itemIds)
            vm.onRouteDisposed()
        }

    @Test
    fun `given active history route, route is disposed, expected storage updates no longer mutate state`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            val dispatchers =
                mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
            val ids = MutableStateFlow(listOf(1L))
            every { repository.observeIds() } returns ids
            every { repository.observePage(any(), any()) } returns flowOf(emptyList())
            val vm = CivitaiHistoryViewModel(
                repository,
                dispatchers,
                mockk(relaxed = true),
                router,
                budget
            )
            advanceUntilIdle()
            // When
            vm.onRouteDisposed()
            ids.value = listOf(2L)
            advanceUntilIdle()
            // Then
            assertEquals(listOf(1L), vm.state.value.itemIds)
        }
}
