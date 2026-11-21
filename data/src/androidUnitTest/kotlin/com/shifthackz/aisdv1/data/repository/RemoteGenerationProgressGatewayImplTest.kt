package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgress
import com.shifthackz.aisdv1.domain.entity.ServerSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies independent worker measurements and removal of terminal owners. @author Dmitriy Moroz */
class RemoteGenerationProgressGatewayImplTest {
    private val gateway = RemoteGenerationProgressGatewayImpl()
    private val first = RemoteGenerationProgress(ServerSource.SWARM_UI, "first", 10)
    private val second = RemoteGenerationProgress(ServerSource.AUTOMATIC1111, "second", 20)

    @Test
    fun `given progress gateway, no work has started, expected no active measurements`() = runTest {
        // Given
        val flow = gateway.observe()
        // When
        val result = flow.first()
        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `given progress gateway, two workers publish, expected both independent measurements retained`() =
        runTest {
            // Given
            gateway.update("a", first)
            // When
            gateway.update("b", second)
            // Then
            assertEquals(setOf(first, second), gateway.observe().first().toSet())
        }

    @Test
    fun `given progress gateway, same owner updates progress, expected one replacement without duplicate owner`() =
        runTest {
            // Given
            gateway.update("a", first)
            gateway.update("b", second)
            val updated = first.copy(percentage = 70)
            // When
            gateway.update("a", updated)
            // Then
            assertEquals(setOf(updated, second), gateway.observe().first().toSet())
        }

    @Test
    fun `given progress gateway, one worker completes, expected only its measurement removed`() =
        runTest {
            // Given
            gateway.update("a", first)
            gateway.update("b", second)
            // When
            gateway.update("a", null)
            // Then
            assertEquals(listOf(second), gateway.observe().first())
        }

    @Test
    fun `given progress gateway, stale unknown worker is removed, expected current measurements preserved`() =
        runTest {
            // Given
            gateway.update("a", first)
            // When
            gateway.update("missing", null)
            // Then
            assertEquals(listOf(first), gateway.observe().first())
        }

    @Test
    fun `given progress gateway, measurement becomes unknown while loading, expected active owner retained without stale percentage`() =
        runTest {
            // Given
            gateway.update("a", first)
            val unknown = first.copy(percentage = null)
            // When
            gateway.update("a", unknown)
            // Then
            assertEquals(listOf(unknown), gateway.observe().first())
        }
}
