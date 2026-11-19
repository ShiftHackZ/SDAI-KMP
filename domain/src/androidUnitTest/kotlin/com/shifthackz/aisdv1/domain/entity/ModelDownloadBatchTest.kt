package com.shifthackz.aisdv1.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Verifies aggregate progress at model handoff, activation and empty-catalog boundaries.
 * @param snapshot Work snapshot whose visible aggregate progress is evaluated.
 * @param expected Whole-download percentage expected in the notification.
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class ModelDownloadBatchTest(private val snapshot: ModelDownloadBatch, private val expected: Int) {
    @Test
    fun `given download progress, active model or completed count changes, expected bounded equal model shares`() {
        // Given
        val state = snapshot
        // When
        val actual = state.totalProgress
        // Then
        assertEquals(expected, actual)
    }

    companion object {
        /** Supplies initial, mid-model, handoff, final and out-of-range progress boundaries. */
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun snapshots(): List<Array<Any>> {
            val batch = ModelDownloadBatch(LocalAiModel.Type.ONNX, totalModelCount = 4)
            return listOf(
                arrayOf(batch, 0),
                arrayOf(batch.copy(progress = DownloadState.Downloading(50)), 12),
                arrayOf(batch.copy(completedIds = listOf("first")), 25),
                arrayOf(
                    batch.copy(
                        completedIds = listOf("first"),
                        progress = DownloadState.Downloading(50)
                    ), 37
                ),
                arrayOf(
                    batch.copy(
                        completedIds = listOf("first"),
                        progress = DownloadState.Complete("/owned/first")
                    ), 25
                ),
                arrayOf(
                    batch.copy(completedIds = listOf("a", "b", "c", "d"), isRunning = false),
                    100
                ),
                arrayOf(batch.copy(progress = DownloadState.Downloading(-100)), 0),
                arrayOf(batch.copy(progress = DownloadState.Downloading(900)), 100),
                arrayOf(batch.copy(totalModelCount = 0), 0),
                arrayOf(batch.copy(totalModelCount = 0, isRunning = false), 100),
                arrayOf(
                    batch.copy(
                        totalModelCount = 1,
                        isBatch = false,
                        progress = DownloadState.Downloading(45)
                    ), 45
                ),
                arrayOf(
                    batch.copy(
                        completedIds = listOf("first"),
                        progress = DownloadState.Error(IllegalStateException("offline"))
                    ), 25
                ),
            )
        }
    }
}
