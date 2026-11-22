package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Covers shared iOS status transitions used by both in-app and system generation displays.
 *
 * @author Dmitriy Moroz
 */
@Suppress("INVALID_CHARACTERS_NATIVE_ERROR")
class BackgroundWorkObserverTest {
    /**
     * Given background generation, measured progress arrives, expected atomic bounded status.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given background generation, measured progress arrives, expected atomic bounded status`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl()

            // When
            observer.postProgressMessage("Generating", "Sampling", 142)
            val status = observer.observeStatus().first()

            // Then
            assertEquals(true, status.running)
            assertEquals("Generating", status.statusTitle)
            assertEquals("Sampling", status.statusSubTitle)
            assertEquals(100, status.percentage)
        }

    /**
     * Given measured generation, loading stage arrives, expected unknown percentage.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given measured generation, loading stage arrives, expected unknown percentage`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl()
            observer.postProgressMessage("Generating", "Sampling", 50)

            // When
            observer.postStatusMessage("Generating", "Decoding")

            // Then
            assertNull(observer.observeStatus().first().percentage)
            assertEquals("Decoding", observer.observeStatus().first().statusSubTitle)
        }

    /**
     * Given running generation, cancellation arrives, expected cleared status and no result.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given running generation, cancellation arrives, expected cleared status and no result`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl()
            observer.postProgressMessage("Generating", "Sampling", 50)

            // When
            observer.postCancelSignal()

            // Then
            assertFalse(observer.hasActiveTasks())
            assertNull(observer.observeStatus().first().percentage)
            assertSame(BackgroundWorkResult.None, observer.observeResult().first())
        }

    /**
     * Given running generation, persistence fails, expected retained failure and stopped progress.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given running generation, persistence fails, expected retained failure and stopped progress`() =
        runTest {
            // Given
            val observer = BackgroundWorkObserverImpl()
            val failure = IllegalStateException("Persistence failed")
            observer.postProgressMessage("Generating", "Saving", 100)

            // When
            observer.postFailedSignal(failure)

            // Then
            assertFalse(observer.hasActiveTasks())
            assertNull(observer.observeStatus().first().percentage)
            assertEquals(BackgroundWorkResult.Error(failure), observer.observeResult().first())
        }
}
