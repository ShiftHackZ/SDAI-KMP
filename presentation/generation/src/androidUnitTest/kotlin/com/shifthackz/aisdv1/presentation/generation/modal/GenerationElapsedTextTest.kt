package com.shifthackz.aisdv1.presentation.generation.modal

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

/**
 * Exercises the real foreground dialog timer across stage changes, long elapsed intervals and completion.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp")
class GenerationElapsedTextTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `given foreground generation, queue transitions to inference and completes, expected clock retains elapsed time and fixed position then disappears`() {
        // Given
        Localization.setLanguageCode("en")
        val clock = TestTimeSource()
        val started = clock.markNow()
        val modal = mutableStateOf<GenerationModal>(GenerationModal.Communicating())
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme {
                GenerationModalRenderer(
                    screenModal = modal.value,
                    generationStartedAt = started,
                    onDismissRequest = {},
                    onCancelGeneration = {},
                    onSaveRequest = {},
                    onReportRequest = {},
                    onViewDetailRequest = {},
                    onBenchmarkRequest = {},
                    onSkipBenchmarkRequest = {},
                    onBenchmarkContinueRequest = {},
                    onBenchmarkDoNotAskRequest = {},
                )
            }
        }
        compose.mainClock.advanceTimeBy(300)
        val initialPosition =
            compose.onNodeWithText("00:00").fetchSemanticsNode().boundsInRoot.topLeft
        // When
        compose.runOnUiThread {
            clock += 3_662_000.milliseconds
            modal.value = GenerationModal.Generating(
                title = Localization.string("communicating_progress_title").asUiText(),
                status = LocalDiffusionStatus(8, 20)
            )
        }
        compose.mainClock.advanceTimeBy(300)
        // Then
        compose.onNodeWithText("01:01:02").assertIsDisplayed()
        assertEquals(
            initialPosition,
            compose.onNodeWithText("01:01:02").fetchSemanticsNode().boundsInRoot.topLeft
        )
        compose.runOnUiThread { modal.value = GenerationModal.None }
        compose.mainClock.advanceTimeBy(300)
        compose.onNodeWithText("01:01:02").assertDoesNotExist()
    }
}
