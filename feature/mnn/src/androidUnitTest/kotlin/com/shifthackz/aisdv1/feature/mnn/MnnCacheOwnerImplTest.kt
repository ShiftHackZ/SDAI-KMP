package com.shifthackz.aisdv1.feature.mnn

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.nio.file.Files
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Checks private workspace locking, stale selection boundaries and failure cleanup using real files.
 *
 * @author Dmitriy Moroz
 */
class MnnCacheOwnerImplTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private val context = mockk<Context>()
    private val cleaner = mockk<MnnCacheCleaner>()

    @Test
    fun `given cache owner, stale UUID and foreign entries exist, expected only owned canonical attempt removed`() {
        // Given
        every { context.noBackupFilesDir } returns temporary.root
        val root = File(temporary.root, "mnn-work").apply { mkdir() }
        val stale = File(root, UUID.randomUUID().toString()).apply { mkdir() }
        val foreign = File(root, "foreign").apply { mkdir() }
        val target = temporary.newFolder("target")
        val link = File(root, UUID.randomUUID().toString())
        Files.createSymbolicLink(link.toPath(), target.toPath())
        val removed = mutableListOf<File>()
        every { cleaner.delete(any()) } answers { firstArg<File>().let { removed += it; check(it.deleteRecursively()) } }

        // When
        val lease = MnnCacheOwnerImpl(context, cleaner).acquire()
        val active = lease.directory
        assertTrue(active.isDirectory)
        lease.close()

        // Then
        assertEquals(listOf(stale.canonicalFile, active), removed)
        assertTrue(foreign.exists())
        assertTrue(target.exists())
        assertTrue(Files.isSymbolicLink(link.toPath()))
        assertFalse(active.exists())
    }

    @Test
    fun `given cache owner, active lease exists, expected second owner rejected without cleaning active files`() {
        // Given
        every { context.noBackupFilesDir } returns temporary.root
        every { cleaner.delete(any()) } answers { firstArg<File>().deleteRecursively(); Unit }
        val owner = MnnCacheOwnerImpl(context, cleaner)
        val lease = owner.acquire()

        // When
        val failure = runCatching { owner.acquire() }.exceptionOrNull()

        // Then
        assertTrue(failure != null)
        assertTrue(lease.directory.exists())
        lease.close()
        owner.acquire().close()
    }

    @Test
    fun `given cache owner, stale cleanup fails, expected original failure and ownership released`() {
        // Given
        every { context.noBackupFilesDir } returns temporary.root
        val root = File(temporary.root, "mnn-work").apply { mkdir() }
        File(root, UUID.randomUUID().toString()).mkdir()
        val failure = IllegalStateException("cannot delete")
        every { cleaner.delete(any()) } throws failure
        val owner = MnnCacheOwnerImpl(context, cleaner)

        // When
        val result = runCatching { owner.acquire() }

        // Then
        assertSame(failure, result.exceptionOrNull())
        every { cleaner.delete(any()) } answers { firstArg<File>().deleteRecursively(); Unit }
        owner.acquire().close()
    }

    @Test
    fun `given cache lease, attempt cleanup fails, expected original error and OS ownership released`() {
        // Given
        every { context.noBackupFilesDir } returns temporary.root
        val failure = IllegalStateException("cannot delete")
        val owner = MnnCacheOwnerImpl(context, cleaner)
        val lease = owner.acquire()
        every { cleaner.delete(any()) } throws failure

        // When
        val result = runCatching { lease.close() }

        // Then
        assertSame(failure, result.exceptionOrNull())
        every { cleaner.delete(any()) } answers { firstArg<File>().deleteRecursively(); Unit }
        owner.acquire().close()
    }
}
