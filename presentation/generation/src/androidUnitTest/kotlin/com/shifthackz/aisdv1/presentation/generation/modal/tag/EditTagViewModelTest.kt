package com.shifthackz.aisdv1.presentation.generation.modal.tag

import io.mockk.mockk
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.presentation.components.model.ExtraType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test


@Ignore("ToDo: Investigate why sometimes tests fail on remote worker due to race-conditions.")
class EditTagViewModelTest {

    @Test
    fun `given edit tag, initial tag data is loaded, expected prompt and original LoRA tag are retained`() =
        runTest {
            // Given
            // The existing createViewModel fixture receives prompt <lora:tag:1>, negative prompt negative,
            // and the original positive LoRA tag.

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            val expected = EditTagState(
                prompt = "prompt <lora:tag:1>",
                negativePrompt = "negative",
                originalTag = "<lora:tag:1>",
                currentTag = "<lora:tag:1>",
                extraType = ExtraType.Lora,
                isNegative = false,
            )
            val actual = viewModel.state.value
            // Then
            Assert.assertEquals(expected, actual)

        }

    @Test
    fun `given edit tag, UpdateTag is received, expected current tag changes and original tag is retained`() =
        runTest {
            // Given

            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.processIntent(EditTagIntent.UpdateTag("<lora:tag_5598:1>"))
            advanceUntilIdle()

            val state = viewModel.state.value
            // Then
            Assert.assertEquals("<lora:tag_5598:1>", state.currentTag)
            Assert.assertEquals("<lora:tag:1>", state.originalTag)
            Assert.assertNotEquals(state.originalTag, state.currentTag)

        }

    @Test
    fun `given edit tag, tag weight increment is received, expected current weight becomes one point twenty five and original tag is retained`() =
        runTest {
            // Given

            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.processIntent(EditTagIntent.Value.Increment)
            advanceUntilIdle()

            val state = viewModel.state.value
            // Then
            Assert.assertEquals("<lora:tag:1.25>", state.currentTag)
            Assert.assertEquals("<lora:tag:1>", state.originalTag)
            Assert.assertNotEquals(state.originalTag, state.currentTag)

        }

    @Test
    fun `given edit tag, tag weight decrement is received, expected current weight becomes zero point seventy five and original tag is retained`() =
        runTest {
            // Given

            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.processIntent(EditTagIntent.Value.Decrement)
            advanceUntilIdle()

            val state = viewModel.state.value
            // Then
            Assert.assertEquals("<lora:tag:0.75>", state.currentTag)
            Assert.assertEquals("<lora:tag:1>", state.originalTag)
            Assert.assertNotEquals(state.originalTag, state.currentTag)

        }

    @Test
    fun `given edit tag, received Action Apply intent, expected ApplyPrompts effect with valid prompt delivered to effect collector`() =
        runTest {
            // Given

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.processIntent(EditTagIntent.Value.Increment)
            // When
            viewModel.processIntent(EditTagIntent.Action.Apply)
            advanceUntilIdle()

            val expected = "prompt <lora:tag:1.25>"
            val actual = (viewModel.effect.firstOrNull() as? EditTagEffect.ApplyPrompts)?.prompt
            // Then
            Assert.assertEquals(expected, actual)

        }

    @Test
    fun `given edit tag, received Action Delete intent, expected ApplyPrompts effect with prompt that does not contain tag delivered to effect collector`() =
        runTest {
            // Given

            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.processIntent(EditTagIntent.Action.Delete)
            advanceUntilIdle()

            val expected = "prompt"
            val actual = (viewModel.effect.firstOrNull() as? EditTagEffect.ApplyPrompts)?.prompt
            // Then
            Assert.assertEquals(expected, actual)

        }

    private fun TestScope.createViewModel(): EditTagViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return EditTagViewModel(
            diagnostics = mockk(relaxed = true),
            dispatchersProvider = testDispatchersProvider(dispatcher),
            prompt = "prompt <lora:tag:1>",
            negativePrompt = "negative",
            tag = "<lora:tag:1>",
            isNegative = false,
        )
    }

    private fun testDispatchersProvider(dispatcher: CoroutineDispatcher) =
        object : DispatchersProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val ui: CoroutineDispatcher = dispatcher
            override val immediate: CoroutineDispatcher = dispatcher
        }
}
