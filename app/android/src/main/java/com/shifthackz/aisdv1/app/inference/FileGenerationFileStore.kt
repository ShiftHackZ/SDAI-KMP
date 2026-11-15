package com.shifthackz.aisdv1.app.inference

import android.content.Context
import android.util.AtomicFile
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.UUID
import org.json.JSONObject

/**
 * Keeps exchange files outside clearable app cache; a fixed filename cannot escape its job root.
 *
 * @param context Application context used only to locate app-private files.
 * @param validator Complete PNG and decoded-size validation.
 * @author Dmitriy Moroz
 */
internal class FileGenerationFileStore(context: Context, private val validator: PngImageValidator) :
    GenerationFileStore {
    private val root = File(context.filesDir, "inference/jobs")

    /**
     * Atomically commits a versioned request JSON file within the 4 MiB request limit.
     *
     * @param job Attempt UUID.
     * @param request Captured generation inputs.
     * @author Dmitriy Moroz
     */
    override fun writeRequest(job: String, request: NativeGenerationRequest) {
        val p = request.payload
        val json = JSONObject().put("version", 1).put("engine", request.engine.name)
            .put("model", request.modelPath).put("nnapi", request.isNnapiEnabled)
            .put("prompt", p.prompt).put("negative", p.negativePrompt)
            .put("steps", p.samplingSteps).put("cfg", p.cfgScale.toDouble())
            .put("width", p.width).put("height", p.height).put("seed", p.seed)
            .put("sampler", p.sampler).put("backend", p.sdxlBackend.name)
        write(job, "request.json", json.toString().toByteArray(), MAX_REQUEST_BYTES)
    }

    /**
     * Reads and validates the captured request, including model readability and parameter limits.
     * Missing, malformed or unsupported request data fails instead of supplying defaults.
     *
     * @param job Validated attempt UUID.
     * @author Dmitriy Moroz
     */
    override fun readRequest(job: String): NativeGenerationRequest {
        val json = JSONObject(read(job, "request.json", MAX_REQUEST_BYTES).toString(Charsets.UTF_8))
        require(json.getInt("version") == 1)
        val path = json.getString("model")
        require(path.isNotBlank() && '\u0000' !in path && File(path).canRead())
        val width = json.getInt("width")
        val height = json.getInt("height")
        require(width in 1..4096 && height in 1..4096 && width.toLong() * height <= 16_777_216L)
        val steps = json.getInt("steps")
        val cfg = json.getDouble("cfg").toFloat()
        require(steps in 1..1000 && cfg.isFinite() && cfg >= 0)
        return NativeGenerationRequest(
            engine = NativeGenerationEngine.valueOf(json.getString("engine")),
            modelPath = File(path).canonicalPath,
            isNnapiEnabled = json.getBoolean("nnapi"),
            payload = TextToImagePayload(
                prompt = json.getString("prompt"), negativePrompt = json.getString("negative"),
                samplingSteps = steps, cfgScale = cfg, width = width, height = height,
                seed = json.getString("seed"), sampler = json.getString("sampler"),
                sdxlBackend = SdxlBackend.valueOf(json.getString("backend")),
                restoreFaces = false, subSeed = "", subSeedStrength = 0f, nsfw = false,
                batchCount = 1, style = null, quality = null, openAiModel = null,
                stabilityAiClipGuidance = null, stabilityAiStylePreset = null,
            ),
        )
    }

    /**
     * Validates the complete PNG against its captured request before an atomic bounded write.
     *
     * @param job Attempt UUID.
     * @param image Complete PNG.
     * @author Dmitriy Moroz
     */
    override fun writeResult(job: String, image: ByteArray) {
        validator.validate(image, readRequest(job))
        write(job, "result.png", image, MAX_RESULT_BYTES)
    }

    /**
     * Returns the completed PNG only after bounded reading and validation against the captured request.
     *
     * @param job Attempt UUID.
     * @author Dmitriy Moroz
     */
    override fun readResult(job: String): ByteArray =
        read(job, "result.png", MAX_RESULT_BYTES).also {
            validator.validate(it, readRequest(job))
        }

    /**
     * Removes only the UUID job directory and its exchange files; model and gallery files are unaffected.
     *
     * @param job Attempt UUID.
     * @author Dmitriy Moroz
     */
    override fun delete(job: String) {
        directory(job).deleteRecursively()
    }

    /**
     * The host invokes this only while holding its admission lock before binding.
     *
     * @author Dmitriy Moroz
     */
    override fun clearAbandoned() {
        root.listFiles()?.filter(File::isDirectory)?.forEach { file ->
            if (runCatching { UUID.fromString(file.name) }.isSuccess && file.canonicalFile.parentFile == root.canonicalFile) file.deleteRecursively()
        }
    }

    private fun directory(job: String): File {
        require(UUID.fromString(job).toString() == job)
        val directory = File(root, job).canonicalFile
        require(directory.parentFile == root.canonicalFile)
        check(directory.mkdirs() || directory.isDirectory)
        return directory
    }

    private fun target(job: String, name: String): File = File(directory(job), name).also {
        require(it.canonicalFile.parentFile == directory(job))
    }

    private fun write(job: String, name: String, bytes: ByteArray, maximum: Long) {
        require(bytes.size.toLong() in 1..maximum)
        val atomic = AtomicFile(target(job, name))
        val output = atomic.startWrite()
        try {
            output.write(bytes); atomic.finishWrite(output)
        } catch (failure: Throwable) {
            atomic.failWrite(output); throw failure
        }
    }

    private fun read(job: String, name: String, maximum: Long): ByteArray {
        val file = target(job, name)
        require(file.length() in 1..maximum)
        return AtomicFile(file).openRead().use { input ->
            ByteArrayOutputStream().use { output ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= maximum)
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
        }
    }

    private companion object {
        const val MAX_REQUEST_BYTES = 4L * 1024 * 1024
        const val MAX_RESULT_BYTES = 128L * 1024 * 1024
        val PNG_SIGNATURE = byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)
    }
}
