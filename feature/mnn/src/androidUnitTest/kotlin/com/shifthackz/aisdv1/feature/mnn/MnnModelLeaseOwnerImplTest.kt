package com.shifthackz.aisdv1.feature.mnn

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Uses real OS locks to verify child model ownership survives host-side lease changes.
 *
 * @author Dmitriy Moroz
 */
class MnnModelLeaseOwnerImplTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private val activation = "a".repeat(64)

    @Test
    fun `given model reader, valid catalog snapshot is opened twice, expected concurrent reader rejected`() {
        // Given
        val root = catalog()
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val lease = owner.acquire(root.path, activation)
        val failure = runCatching { owner.acquire(root.path, activation) }.exceptionOrNull()
        lease.close()

        // Then
        assertTrue(failure is IllegalStateException)
        owner.acquire(root.path, activation).close()
        assertTrue(root.exists())
    }

    @Test
    fun `given model reader, immutable custom snapshot is selected, expected owned identity admitted`() {
        // Given
        val parent = File(temporary.root, "mnn_custom").apply { mkdir() }
        val root = File(parent, activation).apply {
            mkdir(); File(
            this,
            ".complete"
        ).writeText(activation)
        }
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        owner.acquire(root.canonicalPath, activation).close()

        // Then
        assertEquals(activation, File(root, ".complete").readText())
    }

    @Test
    fun `given model reader, completion marker is absent, expected unavailable snapshot and released lock`() {
        // Given
        val root = catalog()
        File(root, ".complete").delete()
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching { owner.acquire(root.path, activation) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalStateException)
        File(root, ".delete-requested").delete()
        File(root, ".complete").writeText(activation)
        owner.acquire(root.path, activation).close()
    }

    @Test
    fun `given model reader, completion identity changed, expected unavailable snapshot and released lock`() {
        // Given
        val root = catalog()
        File(root, ".complete").writeText("b".repeat(64))
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching { owner.acquire(root.path, activation) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalStateException)
        File(root, ".delete-requested").delete()
        File(root, ".complete").writeText(activation)
        owner.acquire(root.path, activation).close()
    }

    @Test
    fun `given model reader, completion identity is truncated, expected unavailable snapshot and released lock`() {
        // Given
        val root = catalog()
        File(root, ".complete").writeText("a")
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching { owner.acquire(root.path, activation) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalStateException)
        File(root, ".delete-requested").delete()
        File(root, ".complete").writeText(activation)
        owner.acquire(root.path, activation).close()
    }

    @Test
    fun `given model reader, deletion is pending, expected unavailable snapshot and released lock`() {
        // Given
        val root = catalog()
        File(root, ".delete-requested").writeText("")
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching { owner.acquire(root.path, activation) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalStateException)
        File(root, ".delete-requested").delete()
        File(root, ".complete").writeText(activation)
        owner.acquire(root.path, activation).close()
    }

    @Test
    fun `given model reader, external folder is supplied, expected ownership rejection`() {
        // Given
        val root = catalog()
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching {
            owner.acquire(
                temporary.newFolder("external").canonicalPath,
                activation
            )
        }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertTrue(root.exists())
    }

    @Test
    fun `given model reader, parent traversal is supplied, expected ownership rejection`() {
        // Given
        val root = catalog()
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching {
            owner.acquire(
                File(root, "../mnn_catalog").path,
                activation
            )
        }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertTrue(root.exists())
    }

    @Test
    fun `given model reader, relative path is supplied, expected ownership rejection`() {
        // Given
        val root = catalog()
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching { owner.acquire("mnn_catalog", activation) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertTrue(root.exists())
    }

    @Test
    fun `given model reader, custom container is supplied, expected ownership rejection`() {
        // Given
        val root = catalog()
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching {
            owner.acquire(
                File(temporary.root, "mnn_custom").path,
                activation
            )
        }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertTrue(root.exists())
    }

    @Test
    fun `given model reader, model root is a symlink, expected target preserved and rejected`() {
        // Given
        val target = temporary.newFolder("external")
        val link = File(temporary.root, "mnn_link")
        Files.createSymbolicLink(link.toPath(), target.toPath())
        val owner = MnnModelLeaseOwnerImpl(temporary.root.canonicalFile)

        // When
        val failure = runCatching { owner.acquire(link.path, activation) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertTrue(target.exists())
    }

    @Test
    fun `given model reader, activation is malformed, expected no lock file created`() {
        // Given
        val root = catalog()
        val before = temporary.root.listFiles()!!.map { it.name }.toSet()

        // When
        assertThrows(IllegalArgumentException::class.java) {
            MnnModelLeaseOwnerImpl(temporary.root.canonicalFile).acquire(
                root.path,
                "../bad"
            )
        }

        // Then
        assertEquals(before, temporary.root.listFiles()!!.map { it.name }.toSet())
        assertFalse(File(temporary.root, "bad").exists())
    }

    /**
     * Creates an immutable catalog marker without simulating model validation or inference.
     *
     * @author Dmitriy Moroz
     */
    private fun catalog(): File = temporary.newFolder("mnn_catalog").canonicalFile.apply {
        File(this, ".complete").writeText(activation)
    }
}
