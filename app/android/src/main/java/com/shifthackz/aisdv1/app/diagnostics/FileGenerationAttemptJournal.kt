package com.shifthackz.aisdv1.app.diagnostics

import android.content.Context
import android.util.AtomicFile
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.GenerationFailureContext
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bounded atomic journal outside app cache; sensitive request and native messages are excluded.
 *
 * @param context Application context used to locate private operational state.
 * @author Dmitriy Moroz
 */
internal class FileGenerationAttemptJournal(context: Context) : GenerationAttemptJournal {
    private val file = AtomicFile(File(context.filesDir, "inference/attempts.json"))
    private val lock = Any()

    /**
     * Adds a bounded attempt entry, rejecting duplicate owner keys still retained in history.
     *
     * @param job New attempt UUID.
     * @param engine Runtime identifier.
     * @param owner Stable worker/batch identity.
     * @author Dmitriy Moroz
     */
    override fun begin(job: String, engine: NativeGenerationEngine, owner: String?) =
        synchronized(lock) {
            val entries = read()
            if (owner != null && entries.any { it.optString("owner") == owner }) {
                throw NativeGenerationFailure(NativeGenerationFailure.Category.INTERRUPTED)
            }
            entries.add(
                JSONObject().put("version", 1).put("job", job).put("engine", engine.name)
                    .put("owner", owner)
                    .put("time", System.currentTimeMillis())
                    .put("stage", NativeGenerationStage.PREPARING.name).put("outcome", "running")
            )
            write(entries)
        }

    /**
     * Persists the validated child identity on the matching retained attempt, if present.
     *
     * @param job Attempt UUID.
     * @param pid Validated child PID.
     * @param incarnation Random process-instance token.
     * @author Dmitriy Moroz
     */
    override fun connected(job: String, pid: Int, incarnation: String) = synchronized(lock) {
        val entries = read()
        entries.firstOrNull { it.optString("job") == job }?.put("pid", pid)
            ?.put("incarnation", incarnation)
        write(entries)
    }

    /**
     * Advances the matching unfinished attempt to a nondecreasing lifecycle stage.
     *
     * @param job Attempt UUID.
     * @param stage Sanitized child lifecycle stage.
     * @author Dmitriy Moroz
     */
    override fun stage(job: String, stage: NativeGenerationStage) = synchronized(lock) {
        val entries = read()
        val entry =
            entries.firstOrNull { it.optString("job") == job && it.optString("outcome") == "running" }
        if (entry != null && stage.ordinal >= NativeGenerationStage.valueOf(entry.getString("stage")).ordinal) {
            entry.put("stage", stage.name)
            write(entries)
        }
    }

    /**
     * Stores the terminal outcome and optional sanitized exit evidence for the matching retained attempt.
     *
     * @param job Attempt UUID.
     * @param outcome Sanitized terminal category.
     * @param stage Last observed native lifecycle stage.
     * @param exit Best-effort PID/time-matched exit metadata.
     * @author Dmitriy Moroz
     */
    override fun finish(
        job: String,
        outcome: String,
        stage: NativeGenerationStage,
        exit: GenerationFailureContext?
    ) = synchronized(lock) {
        require(outcome.matches(Regex("[a-z_]{1,40}")))
        val entries = read()
        entries.firstOrNull { it.optString("job") == job }?.apply {
            put("outcome", outcome).put("stage", stage.name)
                .put("finished_at", System.currentTimeMillis())
            if (exit != null) {
                put("exit_time", exit.timestamp).put("exit_reason", exit.reason)
                put("exit_status", exit.status).put("has_native_trace", exit.hasNativeTrace)
            }
        }
        write(entries)
    }

    /**
     * Called only by the host before admitting attempts after process creation.
     *
     * @author Dmitriy Moroz
     */
    override fun reconcile() = synchronized(lock) {
        val entries = read()
        entries.filter { it.optString("outcome") == "running" }
            .forEach { it.put("outcome", "interrupted") }
        write(entries)
    }

    private fun read(): MutableList<JSONObject> {
        if (!file.baseFile.exists()) return mutableListOf()
        require(file.baseFile.length() <= 1024 * 1024)
        val array = file.openRead().use { JSONArray(it.reader().readText()) }
        val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return (0 until array.length()).map(array::getJSONObject)
            .filter { it.optLong("time") >= cutoff }.takeLast(20).toMutableList()
    }

    private fun write(entries: List<JSONObject>) {
        check(file.baseFile.parentFile?.mkdirs() == true || file.baseFile.parentFile?.isDirectory == true)
        val bytes = JSONArray(entries.takeLast(20)).toString().toByteArray()
        require(bytes.size <= 1024 * 1024)
        val stream = file.startWrite()
        try {
            stream.write(bytes); file.finishWrite(stream)
        } catch (failure: Throwable) {
            file.failWrite(stream); throw failure
        }
    }
}
