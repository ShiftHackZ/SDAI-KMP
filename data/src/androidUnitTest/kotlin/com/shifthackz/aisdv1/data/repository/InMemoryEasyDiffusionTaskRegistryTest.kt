package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Verifies actual operation ownership, cancellation isolation and stale completion guards.
 * @author Dmitriy Moroz
 */
class InMemoryEasyDiffusionTaskRegistryTest {
    private val registry = InMemoryEasyDiffusionTaskRegistry()

    @Test
    fun `concurrent starts fail without replacing captured owner`() = runTest {
        val job = Job()
        registry.begin("first", job, "worker")
        assertTrue(runCatching { registry.begin("second", Job(), "other") }.isFailure)
        assertEquals("worker", registry.progress.value.ownerId)
        assertTrue(job.isActive)
        registry.finish("first")
        job.complete()
    }

    @Test
    fun `stale updates and finish cannot clear current task`() = runTest {
        val job = Job()
        registry.begin("current", job, "worker")
        registry.update("current", EasyDiffusionProgress(3, 20, true))
        registry.update("old", EasyDiffusionProgress(19, 20, true))
        registry.finish("old")
        assertEquals(EasyDiffusionProgress(3, 20, true, "worker"), registry.progress.value)
        assertEquals("published", registry.withActive("current") { "published" })
        var hasPublished = false
        assertTrue(runCatching { registry.withActive("old") { hasPublished = true } }.isFailure)
        assertFalse(hasPublished)
        registry.finish("current")
        assertEquals(EasyDiffusionProgress(), registry.progress.value)
        job.complete()
    }

    @Test
    fun `only matching background owner cancels and suppresses late publication`() = runTest {
        val job = Job()
        registry.begin("session", job, "worker")
        registry.cancel("other")
        assertTrue(job.isActive)
        registry.cancel("worker")
        assertTrue(job.isCancelled)
        val before = registry.progress.value
        registry.update("session", EasyDiffusionProgress(20, 20, true))
        assertEquals(before, registry.progress.value)
        var hasPublished = false
        assertTrue(runCatching { registry.withActive("session") { hasPublished = true } }.isFailure)
        assertFalse(hasPublished)
        registry.finish("session")
        val next = Job()
        registry.begin("next", next, null)
        assertEquals(1, registry.withActive("next") { 1 })
        registry.finish("next")
        next.complete()
    }

    @Test
    fun `background interrupt cannot cancel foreground work`() = runTest {
        val job = Job()
        registry.begin("foreground", job, null)
        registry.cancel("worker")
        assertTrue(job.isActive)
        registry.finish("foreground")
        job.complete()
    }
}
