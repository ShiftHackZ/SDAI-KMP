package com.shifthackz.aisdv1.feature.mnn

import android.system.Os
import android.system.OsConstants
import android.system.StructStat
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises Android no-follow cleanup against real nested directories and symbolic links.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MnnCacheCleanerImplTest {
    @get:Rule
    val temporary = TemporaryFolder()

    /**
     * Supplies real host no-follow metadata because Robolectric's lstat shadow follows symbolic links.
     *
     * @author Dmitriy Moroz
     */
    @Before
    fun useNoFollowMetadata() {
        mockkStatic(Os::class)
        every { Os.lstat(any()) } answers {
            val path = File(firstArg<String>()).toPath()
            val mode = when {
                Files.isSymbolicLink(path) -> OsConstants.S_IFLNK
                Files.isDirectory(path, NOFOLLOW_LINKS) -> OsConstants.S_IFDIR
                else -> OsConstants.S_IFREG
            }
            StructStat(0, 0, mode, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        }
    }

    @After
    fun releaseMetadataBoundary() = unmockkStatic(Os::class)

    @Test
    fun `given cache cleaner, nested mmap files exist, expected owned tree removed`() {
        // Given
        val root = temporary.newFolder("attempt")
        File(root, "nested").mkdir()
        File(root, "nested/weights").writeText("owned")

        // When
        MnnCacheCleanerImpl().delete(root)

        // Then
        assertFalse(root.exists())
    }

    @Test
    fun `given cache cleaner, nested link targets foreign files, expected link removed without following target`() {
        // Given
        val root = temporary.newFolder("attempt")
        val target = temporary.newFolder("foreign")
        val payload = File(target, "keep").apply { writeText("keep") }
        Files.createSymbolicLink(File(root, "link").toPath(), target.toPath())

        // When
        MnnCacheCleanerImpl().delete(root)

        // Then
        assertFalse(root.exists())
        assertTrue(payload.isFile)
    }
}
