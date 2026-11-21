package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.GenerationMetadataContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies metadata context propagation retains provider failures and structured cancellation.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WithGenerationMetadataTest {
    @Test
    fun `given generation metadata, provider operation succeeds, expected captured context and unchanged result`() =
        runTest {
            // Given
            val metadata = GenerationMetadataContext("local_mnn", "CPU")
            val result = Any()
            // When
            val completed = withGenerationMetadata(metadata) {
                assertSame(metadata, currentCoroutineContext()[GenerationMetadataContext])
                result
            }
            // Then
            assertSame(result, completed)
            assertNull(currentCoroutineContext()[GenerationMetadataContext])
        }

    @Test
    fun `given generation metadata, provider throws a failure, expected original exception instance propagated`() =
        runTest {
            // Given
            val failure = IllegalStateException("Provider rejected request")
            // When
            val caught = runCatching {
                withGenerationMetadata(
                    GenerationMetadataContext(
                        "open_ai",
                        "None"
                    )
                ) { throw failure }
            }.exceptionOrNull()
            // Then
            assertSame(failure, caught)
            assertNull(currentCoroutineContext()[GenerationMetadataContext])
        }

    @Test
    fun `given generation metadata, provider cancels its operation, expected original cancellation instance propagated`() =
        runTest {
            // Given
            val failure = CancellationException("Provider cancelled")
            // When
            val caught = runCatching {
                withGenerationMetadata(
                    GenerationMetadataContext(
                        "local_mnn",
                        "CPU"
                    )
                ) { throw failure }
            }.exceptionOrNull()
            // Then
            assertSame(failure, caught)
        }

    @Test
    fun `given active generation metadata, caller cancels its job, expected provider cleanup and no successful result`() =
        runTest {
            // Given
            val hasCleanedUp = CompletableDeferred<Boolean>()
            var hasReturned = false
            val job = launch {
                withGenerationMetadata<Unit>(GenerationMetadataContext("local_mnn", "CPU")) {
                    try {
                        awaitCancellation()
                    } finally {
                        hasCleanedUp.complete(true)
                    }
                }
                hasReturned = true
            }
            runCurrent()
            // When
            job.cancel()
            job.join()
            // Then
            assertTrue(job.isCancelled)
            assertEquals(true, hasCleanedUp.await())
            assertEquals(false, hasReturned)
        }

    @Test
    fun `given nested image generation contexts, an image finishes, expected outer batch context restored`() =
        runTest {
            // Given
            val batch = GenerationMetadataContext("swarm_ui", "None", deviceName = "Pixel 7 Pro")
            val image = GenerationMetadataContext(
                batch.providerKey,
                batch.acceleration,
                deviceName = batch.deviceName
            )
            // When
            withGenerationMetadata(batch) {
                withGenerationMetadata(image) {
                    assertSame(
                        image,
                        currentCoroutineContext()[GenerationMetadataContext]
                    )
                }
                // Then
                assertSame(batch, currentCoroutineContext()[GenerationMetadataContext])
                assertEquals(batch.deviceName, image.deviceName)
            }
        }
}
