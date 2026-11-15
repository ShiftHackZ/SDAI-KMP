package com.shifthackz.aisdv1.presentation.modal.extras

import androidx.lifecycle.viewModelScope
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.LoRA
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.sdlora.FetchAndGetLorasUseCase
import com.shifthackz.aisdv1.presentation.model.ExtraType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Exercises delayed large catalogs through the production picker state and selection intents.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LargeLoraCatalogViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val fetchLoras = mockk<FetchAndGetLorasUseCase>()
    private val catalogs = List(2_500) { LoRA("lora_$it", "alias_$it", "models/Lora/$it.safetensors") }
    private val viewModels = mutableListOf<ExtrasViewModel>()

    @Before
    fun initialize() {
        Dispatchers.setMain(dispatcher)
        coEvery { fetchLoras() } returns catalogs
    }

    @After
    fun cleanUp() {
        viewModels.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    @Test
    fun `2500 entries retain aliases and selected prompt values`() = runTest(dispatcher) {
        val viewModel = createViewModel("portrait <lora:alias_2499:0.7>")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertEquals(2_500, state.loras.size)
        assertEquals(2_500, state.loras.map { it.key }.distinct().size)
        state.loras.forEachIndexed { index, item ->
            assertEquals("lora_$index", item.name)
            assertEquals("alias_$index", item.alias)
            assertEquals(index == 2_499, item.isApplied)
        }
        assertEquals("0.7", state.loras.last().value)
        viewModel.processIntent(ExtrasIntent.ToggleItem(state.loras.first()))
        assertTrue(viewModel.state.value.prompt.contains("<lora:alias_0:1>"))
        assertTrue(viewModel.state.value.loras.first().isApplied)
        assertTrue(viewModel.state.value.loras.last().isApplied)
    }

    @Test
    fun `delayed catalog stays loading then publishes all 2500 entries`() = runTest(dispatcher) {
        val response = CompletableDeferred<List<LoRA>>()
        coEvery { fetchLoras() } coAnswers { response.await() }
        val viewModel = createViewModel("portrait")
        runCurrent()
        assertTrue(viewModel.state.value.loading)
        assertTrue(viewModel.state.value.loras.isEmpty())

        response.complete(catalogs)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.loading)
        assertEquals(2_500, viewModel.state.value.loras.size)
        assertEquals("portrait", viewModel.state.value.prompt)
    }

    /**
     * Supplies one deterministic scheduler to the picker's lifecycle and worker dispatchers.
     *
     * @param prompt Existing generation prompt used to restore selected LoRA weights.
     * @author Dmitriy Moroz
     */
    private fun createViewModel(prompt: String): ExtrasViewModel = ExtrasViewModel(
        dispatchersProvider = object : DispatchersProvider {
            override val io = dispatcher
            override val ui = dispatcher
            override val immediate = dispatcher
        },
        fetchAndGetLorasUseCase = fetchLoras,
        fetchAndGetHyperNetworksUseCase = mockk(),
        preferenceManager = mockk<PreferenceManager> {
            every { source } returns ServerSource.AUTOMATIC1111
        },
        prompt = prompt,
        negativePrompt = "",
        type = ExtraType.Lora,
    ).also(viewModels::add)
}
