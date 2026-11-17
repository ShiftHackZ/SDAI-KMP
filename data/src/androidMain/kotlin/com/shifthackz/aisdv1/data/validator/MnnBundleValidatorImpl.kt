package com.shifthackz.aisdv1.data.validator

import com.shifthackz.aisdv1.domain.entity.mnn.MnnBundleFile
import com.shifthackz.aisdv1.domain.entity.mnn.MnnBundleManifest
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File
import java.security.MessageDigest
import org.json.JSONObject

/**
 * Strict reader for the versioned SD 1.5 archive contract, including complete payload hashes.
 *
 * @author Dmitriy Moroz
 */
internal class MnnBundleValidatorImpl : MnnBundleValidator {
    /**
     * Validates the root layout and pinned source metadata before any model bytes reach MNN.
     *
     * @param path App-owned staging or immutable model directory.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(path: String): MnnBundleManifest {
        val root = File(path).canonicalFile
        val manifest = File(root, "manifest.json")
        require(manifest.canonicalFile == manifest.absoluteFile)
        require(manifest.isFile && manifest.length() in 1..65_536)
        val json = JSONObject(manifest.readText())
        require(json.keys().asSequence().toSet() == MANIFEST_FIELDS)
        require(json.get("schemaVersion") is Int && json.get("width") is Int && json.get("height") is Int)
        require(json.getInt("schemaVersion") == 1)
        require(json.getString("profile") == PROFILE)
        require(json.getString("runtimeVersion") == "3.6.2")
        require(json.getString("runtimeRevision") == RUNTIME_REVISION)
        require(json.getString("prebuiltRepository") == "taobao-mnn/stable-diffusion-v1-5-mnn-general")
        require(json.getString("prebuiltRevision") == "56c93a62ac9d6b5d18605632b85e25f87281f317")
        require(json.getString("sourceRepository") == "stable-diffusion-v1-5/stable-diffusion-v1-5")
        require(json.getString("sourceRevision") == SOURCE_REVISION)
        require(json.getString("tokenizerSourceRevision") == SOURCE_REVISION)
        require(json.getString("tokenizerToolRevision") == RUNTIME_REVISION)
        require(json.getString("packagingLockSha256").matches(SHA256))
        require(json.getInt("width") == 512 && json.getInt("height") == 512)
        require(json.getDouble("guidance") == 7.5 && json.getString("scheduler") == "PNDM")
        val entries = json.getJSONArray("files")
        require(entries.length() == REQUIRED_FILES.size)
        val files = List(entries.length()) { index ->
            val entry = entries.getJSONObject(index)
            require(entry.keys().asSequence().toSet() == setOf("path", "size", "sha256"))
            require(entry.get("size") is Int || entry.get("size") is Long)
            MnnBundleFile(entry.getString("path"), entry.getLong("size"), entry.getString("sha256"))
        }
        require(files.map(MnnBundleFile::path).toSet() == REQUIRED_FILES)
        files.forEach { item ->
            require(item.size in 1..2_000_000_000 && item.sha256.matches(SHA256))
            PINNED_PAYLOADS[item.path]?.let { expected ->
                require(item.size == expected.first && item.sha256 == expected.second) { "Unsupported MNN payload: ${item.path}" }
            }
            val file = File(root, item.path)
            require(file.canonicalFile == file.absoluteFile && file.canonicalFile.parentFile == root)
            require(file.isFile && file.length() == item.size)
            val hash = MessageDigest.getInstance("SHA-256")
            file.inputStream().buffered().use { input ->
                val buffer = ByteArray(65_536)
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    hash.update(buffer, 0, count)
                }
            }
            require(hash.digest().joinToString("") { "%02x".format(it) } == item.sha256)
        }
        val activationId = MessageDigest.getInstance("SHA-256").digest(manifest.readBytes())
            .joinToString("") { "%02x".format(it) }
        return MnnBundleManifest(PROFILE, files, activationId)
    }

    private companion object {
        const val PROFILE = "sd15-512-general-int8-v1"
        const val RUNTIME_REVISION = "7e3c6fb5225d3ccf22807ee641332669df34650d"
        const val SOURCE_REVISION = "451f4fe16113bff5a5d2269ed5ad43b0592e9a14"

        // The v1 profile accepts only the measured immutable general graphs and matching tokenizer.
        val PINNED_PAYLOADS = mapOf(
            "text_encoder.mnn" to (249944L to "9fb787d566512379dcdf9e25b43d9169b7b85eda423408706cf3a56c63dff6dd"),
            "text_encoder.mnn.weight" to (238120368L to "c245ac80dd8a72279976414435801d579a8dcf83aba84e121c4e4a0b74bbed3d"),
            "tokenizer.mtok" to (1322512L to "21cc1d6f9dca099fb184a899b08e898121b6e18e9f38757ee9feede49d8c038a"),
            "unet.mnn" to (1353144L to "3895d9101b16f99660010c87cf1413407103a237636d6a112de11a674a960d4c"),
            "unet.mnn.weight" to (863650252L to "904302004049d81dfa88035dc945e18ea9823a1bfa75bdfefb8f792ade9ea8bb"),
            "vae_decoder.mnn" to (183960L to "8599ec07791cf635058f5d38981f318c7939e8d5274bb8bfe49d57d2962e3220"),
            "vae_decoder.mnn.weight" to (49639636L to "251c9025a6ae2164746ca549ebfc3ce63df418e23abab5abe41c2bc0b1f9b7ff")
        )
        val SHA256 = Regex("[0-9a-f]{64}")
        val MANIFEST_FIELDS = setOf(
            "schemaVersion",
            "profile",
            "prebuiltRepository",
            "prebuiltRevision",
            "sourceRepository",
            "sourceRevision",
            "runtimeVersion",
            "runtimeRevision",
            "tokenizerSourceRevision",
            "tokenizerToolRevision",
            "packagingLockSha256",
            "width",
            "height",
            "guidance",
            "scheduler",
            "files"
        )
        val REQUIRED_FILES = setOf(
            "text_encoder.mnn",
            "text_encoder.mnn.weight",
            "unet.mnn",
            "unet.mnn.weight",
            "vae_decoder.mnn",
            "vae_decoder.mnn.weight",
            "tokenizer.mtok",
            "LICENSE",
            "NOTICE.md"
        )
    }
}
