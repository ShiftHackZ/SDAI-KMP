package com.shifthackz.aisdv1.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Uses the downloader's production activation boundary to verify terminal cleanup ownership.
 *
 * @author Dmitriy Moroz
 */
class MnnDownloadActivationTest {
    @Test
    fun `given MNN download activation, parent cancels before child starts, expected archive and session cleaned without activation`() =
        runTest {
            // Given
            val parent = Job().apply { cancel() }
            var activationCount = 0
            var archiveCleanupCount = 0
            var sessionCloseCount = 0

            // When
            CoroutineScope(coroutineContext + parent).launch(start = CoroutineStart.UNDISPATCHED) {
                activateMnnDownload(
                    activate = { activationCount++; "/model" },
                    removeArchive = { archiveCleanupCount++ },
                    closeSession = { sessionCloseCount++ },
                )
            }.join()

            // Then
            assertEquals(0, activationCount)
            assertEquals(1, archiveCleanupCount)
            assertEquals(1, sessionCloseCount)
        }

    @Test
    fun `given MNN download activation, archive cleanup fails after installation, expected failure without successful path`() =
        runTest {
            // Given
            val cleanup = IllegalStateException("storage unavailable")
            var sessionCloseCount = 0

            // When
            val failure = runCatching {
                activateMnnDownload({ "/model" }, { throw cleanup }, { sessionCloseCount++ })
            }.exceptionOrNull()

            // Then
            assertSame(cleanup, failure)
            assertEquals(1, sessionCloseCount)
        }

    @Test
    fun `given MNN download activation, cancellation and archive cleanup fail, expected original cancellation with suppressed cleanup`() =
        runTest {
            // Given
            val cancellation = CancellationException("activation cancelled")
            val cleanup = IllegalStateException("archive removal failed")
            var sessionCloseCount = 0

            // When
            val failure = runCatching {
                activateMnnDownload(
                    { throw cancellation },
                    { throw cleanup },
                    { sessionCloseCount++ })
            }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
            assertEquals(listOf(cleanup), failure?.suppressedExceptions)
            assertEquals(1, sessionCloseCount)
        }
}
