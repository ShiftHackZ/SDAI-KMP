package com.shifthackz.aisdv1.data.validator

import com.shifthackz.aisdv1.data.local.AndroidMnnBundleFileSystem

import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises strict manifest admission without replacing the pinned payload checks.
 * A positive real-model fixture is opt-in through SDAI_MNN_BUNDLE_DIR and is never required by CI.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class MnnBundleValidatorImplTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private val validator = MnnBundleValidatorImpl(AndroidMnnBundleFileSystem())

    @Test
    fun `given MNN manifest, unknown schema is present, expected strict rejection`() = runTest {
        // Given
        val root = temporary.newFolder()
        File(root, "manifest.json").writeText(manifest().apply { put("schemaVersion", 2) }
            .toString())

        // When
        val failure = runCatching { validator(root.path) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertEquals("Unsupported MNN manifest schema version", failure?.message)
    }

    @Test
    fun `given MNN manifest, string schema is present, expected strict rejection`() = runTest {
        // Given
        val root = temporary.newFolder()
        File(root, "manifest.json").writeText(manifest().apply { put("schemaVersion", "1") }
            .toString())

        // When
        val failure = runCatching { validator(root.path) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertEquals("MNN schema version and dimensions must be integers", failure?.message)
    }

    @Test
    fun `given MNN manifest, unknown manifest field is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply { put("extra", true) }
                .toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("MNN manifest fields do not match schema version 1", failure?.message)
        }

    @Test
    fun `given MNN manifest, missing metadata is present, expected strict rejection`() = runTest {
        // Given
        val root = temporary.newFolder()
        File(root, "manifest.json").writeText(manifest().apply { remove("sourceRevision") }
            .toString())

        // When
        val failure = runCatching { validator(root.path) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertEquals("MNN manifest fields do not match schema version 1", failure?.message)
    }

    @Test
    fun `given MNN manifest, wrong profile is present, expected strict rejection`() = runTest {
        // Given
        val root = temporary.newFolder()
        File(root, "manifest.json").writeText(manifest().apply { put("profile", "sdxl") }
            .toString())

        // When
        val failure = runCatching { validator(root.path) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertEquals("Unsupported MNN generation profile", failure?.message)
    }

    @Test
    fun `given MNN manifest, wrong runtime version is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply { put("runtimeVersion", "0.0") }
                .toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN runtime version", failure?.message)
        }

    @Test
    fun `given MNN manifest, wrong runtime revision is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                put(
                    "runtimeRevision",
                    "0".repeat(40)
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN runtime revision", failure?.message)
        }

    @Test
    fun `given MNN manifest, wrong prebuilt repository is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                put(
                    "prebuiltRepository",
                    "other/model"
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN prebuilt repository", failure?.message)
        }

    @Test
    fun `given MNN manifest, wrong prebuilt revision is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                put(
                    "prebuiltRevision",
                    "0".repeat(40)
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN prebuilt revision", failure?.message)
        }

    @Test
    fun `given MNN manifest, wrong source repository is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                put(
                    "sourceRepository",
                    "other/model"
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN source repository", failure?.message)
        }

    @Test
    fun `given MNN manifest, wrong source revision is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                put(
                    "sourceRevision",
                    "0".repeat(40)
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN source revision", failure?.message)
        }

    @Test
    fun `given MNN manifest, wrong tokenizer source is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                put(
                    "tokenizerSourceRevision",
                    "0".repeat(40)
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN tokenizer source revision", failure?.message)
        }

    @Test
    fun `given MNN manifest, wrong tokenizer tool is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                put(
                    "tokenizerToolRevision",
                    "0".repeat(40)
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN tokenizer tool revision", failure?.message)
        }

    @Test
    fun `given MNN manifest, invalid packaging digest is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                put(
                    "packagingLockSha256",
                    "INVALID"
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Invalid MNN packaging lock digest", failure?.message)
        }

    @Test
    fun `given MNN manifest, unsupported width is present, expected strict rejection`() = runTest {
        // Given
        val root = temporary.newFolder()
        File(root, "manifest.json").writeText(manifest().apply { put("width", 1024) }.toString())

        // When
        val failure = runCatching { validator(root.path) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertEquals("Unsupported MNN image dimensions", failure?.message)
    }

    @Test
    fun `given MNN manifest, unsupported height is present, expected strict rejection`() = runTest {
        // Given
        val root = temporary.newFolder()
        File(root, "manifest.json").writeText(manifest().apply { put("height", 256) }.toString())

        // When
        val failure = runCatching { validator(root.path) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertEquals("Unsupported MNN image dimensions", failure?.message)
    }

    @Test
    fun `given MNN manifest, string dimension is present, expected strict rejection`() = runTest {
        // Given
        val root = temporary.newFolder()
        File(root, "manifest.json").writeText(manifest().apply { put("width", "512") }.toString())

        // When
        val failure = runCatching { validator(root.path) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertEquals("MNN schema version and dimensions must be integers", failure?.message)
    }

    @Test
    fun `given MNN manifest, unsupported guidance is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply { put("guidance", 1.0) }
                .toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN guidance scale", failure?.message)
        }

    @Test
    fun `given MNN manifest, unsupported scheduler is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply { put("scheduler", "Euler") }
                .toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN scheduler", failure?.message)
        }

    @Test
    fun `given MNN manifest, missing payload entry is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(
                root,
                "manifest.json"
            ).writeText(manifest().apply { getJSONArray("files").remove(0) }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("MNN bundle must contain exactly nine payload entries", failure?.message)
        }

    @Test
    fun `given MNN manifest, duplicate payload entry is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().apply {
                getJSONArray("files").put(
                    0,
                    getJSONArray("files").getJSONObject(1)
                )
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals(
                "MNN payload inventory has missing duplicate or unsupported names",
                failure?.message
            )
        }

    @Test
    fun `given MNN manifest, traversal payload name is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(
                root,
                "manifest.json"
            ).writeText(manifest().apply {
                getJSONArray("files").getJSONObject(0).put("path", "../external")
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals(
                "MNN payload inventory has missing duplicate or unsupported names",
                failure?.message
            )
        }

    @Test
    fun `given MNN manifest, unknown payload entry field is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(
                root,
                "manifest.json"
            ).writeText(manifest().apply {
                getJSONArray("files").getJSONObject(0).put("extra", true)
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("MNN payload entry fields are invalid", failure?.message)
        }

    @Test
    fun `given MNN manifest, string payload size is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(
                root,
                "manifest.json"
            ).writeText(manifest().apply {
                getJSONArray("files").getJSONObject(0).put("size", "249944")
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("MNN payload size must be an integer", failure?.message)
        }

    @Test
    fun `given MNN manifest, unsupported pinned size is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(
                root,
                "manifest.json"
            ).writeText(manifest().apply { getJSONArray("files").getJSONObject(0).put("size", 1) }
                .toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN payload: text_encoder.mnn", failure?.message)
        }

    @Test
    fun `given MNN manifest, self declared arbitrary hash is present, expected strict rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(
                root,
                "manifest.json"
            ).writeText(manifest().apply {
                getJSONArray("files").getJSONObject(0).put("sha256", "0".repeat(64))
            }.toString())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("Unsupported MNN payload: text_encoder.mnn", failure?.message)
        }

    @Test
    fun `given MNN manifest, graph bytes fail pinned hash, expected integrity rejection`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().toString())
            File(root, "text_encoder.mnn").writeBytes(ByteArray(249944))

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("MNN payload hash mismatch: text_encoder.mnn", failure?.message)
        }

    @Test
    fun `given MNN manifest, manifest is a symlink, expected external target untouched and rejected`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            val target = temporary.newFile().apply { writeText(manifest().toString()) }
            Files.createSymbolicLink(File(root, "manifest.json").toPath(), target.toPath())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("MNN manifest must not be a symbolic link", failure?.message)
            assertTrue(target.isFile)
        }

    @Test
    fun `given MNN manifest, pinned graph is a symlink, expected rejection before target access`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().toString())
            val target = temporary.newFile().apply { writeBytes(ByteArray(249944)) }
            Files.createSymbolicLink(File(root, "text_encoder.mnn").toPath(), target.toPath())

            // When
            val failure = runCatching { validator(root.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals(
                "MNN payload must be an owned regular path: text_encoder.mnn",
                failure?.message
            )
            assertEquals(249944L, target.length())
        }

    @Test
    fun `given MNN validation, coroutine is cancelled before hashing, expected cancellation preserved`() =
        runTest {
            // Given
            val root = temporary.newFolder()
            File(root, "manifest.json").writeText(manifest().toString())
            File(root, "text_encoder.mnn").writeBytes(ByteArray(249944))
            var failure: Throwable? = null

            // When
            launch {
                cancel()
                failure = runCatching { validator(root.path) }.exceptionOrNull()
            }
            runCurrent()

            // Then
            assertTrue(failure is CancellationException)
        }

    @Test
    fun `given MNN validation, real pinned fixture is explicitly supplied, expected exact manifest identity`() =
        runTest {
            // Given
            val supplied = System.getenv("SDAI_MNN_BUNDLE_DIR")
            assumeTrue("Optional real pinned fixture is not configured", !supplied.isNullOrBlank())
            val root = File(requireNotNull(supplied))

            // When
            val validated = validator(root.path)

            // Then
            assertEquals("sd15-512-general-int8-v1", validated.profile)
            assertEquals(9, validated.files.size)
            assertEquals(sha256(File(root, "manifest.json").readBytes()), validated.activationId)
        }

    /**
     * Reconstructs the public fixed profile inventory; its pinned hashes cannot admit fabricated weights.
     *
     * @author Dmitriy Moroz
     */
    private fun manifest() = JSONObject(
        """
{
  "schemaVersion": 1,
  "profile": "sd15-512-general-int8-v1",
  "prebuiltRepository": "taobao-mnn/stable-diffusion-v1-5-mnn-general",
  "prebuiltRevision": "56c93a62ac9d6b5d18605632b85e25f87281f317",
  "sourceRepository": "stable-diffusion-v1-5/stable-diffusion-v1-5",
  "sourceRevision": "451f4fe16113bff5a5d2269ed5ad43b0592e9a14",
  "runtimeVersion": "3.6.2",
  "runtimeRevision": "7e3c6fb5225d3ccf22807ee641332669df34650d",
  "tokenizerSourceRevision": "451f4fe16113bff5a5d2269ed5ad43b0592e9a14",
  "tokenizerToolRevision": "7e3c6fb5225d3ccf22807ee641332669df34650d",
  "packagingLockSha256": "5c950a166d20f567e24dc2cc7c1c2e4ec7cfd6f23a2cff487f5553b9ae1b7d80",
  "width": 512,
  "height": 512,
  "guidance": 7.5,
  "scheduler": "PNDM",
  "files": [
    {
      "path": "text_encoder.mnn",
      "size": 249944,
      "sha256": "9fb787d566512379dcdf9e25b43d9169b7b85eda423408706cf3a56c63dff6dd"
    },
    {
      "path": "LICENSE",
      "size": 14385,
      "sha256": "be351ebe7ac01bcdbb018639aadcfd38f136b7dc3f2a3d4d3a24db51d1b210ef"
    },
    {
      "path": "NOTICE.md",
      "size": 545,
      "sha256": "2b6e707ff7a042728da393049130b10265f3aac1c890c0a662bd823d3a52e155"
    },
    {
      "path": "text_encoder.mnn.weight",
      "size": 238120368,
      "sha256": "c245ac80dd8a72279976414435801d579a8dcf83aba84e121c4e4a0b74bbed3d"
    },
    {
      "path": "tokenizer.mtok",
      "size": 1322512,
      "sha256": "21cc1d6f9dca099fb184a899b08e898121b6e18e9f38757ee9feede49d8c038a"
    },
    {
      "path": "unet.mnn",
      "size": 1353144,
      "sha256": "3895d9101b16f99660010c87cf1413407103a237636d6a112de11a674a960d4c"
    },
    {
      "path": "unet.mnn.weight",
      "size": 863650252,
      "sha256": "904302004049d81dfa88035dc945e18ea9823a1bfa75bdfefb8f792ade9ea8bb"
    },
    {
      "path": "vae_decoder.mnn",
      "size": 183960,
      "sha256": "8599ec07791cf635058f5d38981f318c7939e8d5274bb8bfe49d57d2962e3220"
    },
    {
      "path": "vae_decoder.mnn.weight",
      "size": 49639636,
      "sha256": "251c9025a6ae2164746ca549ebfc3ce63df418e23abab5abe41c2bc0b1f9b7ff"
    }
  ]
}
"""
    )

    /**
     * Computes identity from exact fixture manifest bytes.
     *
     * @param bytes Complete manifest bytes.
     *
     * @author Dmitriy Moroz
     */
    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
