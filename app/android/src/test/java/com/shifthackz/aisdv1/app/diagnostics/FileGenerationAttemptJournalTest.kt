package com.shifthackz.aisdv1.app.diagnostics

import android.app.Application
import android.content.Context
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import io.mockk.every
import io.mockk.mockk
import java.io.File
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies durable replay rejection and terminal-stage protection across host recreation.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class, sdk = [28])
class FileGenerationAttemptJournalTest {
    @get:Rule
    val temporary = TemporaryFolder()

    private val context = mockk<Context>()
    private lateinit var journal: FileGenerationAttemptJournal

    @Before
    fun initialize() {
        every { context.filesDir } returns temporary.root
        journal = FileGenerationAttemptJournal(context)
    }

    @Test
    fun `given completed worker attempt, recreated journal rejects the same owner key`() {
        journal.begin("first-job", NativeGenerationEngine.BONSAI, "worker:0")
        journal.finish("first-job", "success", NativeGenerationStage.WRITING_RESULT, null)
        val recreated = FileGenerationAttemptJournal(context)

        val failure = assertThrows(NativeGenerationFailure::class.java) {
            recreated.begin("second-job", NativeGenerationEngine.BONSAI, "worker:0")
        }

        assertEquals(NativeGenerationFailure.Category.INTERRUPTED, failure.category)
        assertEquals(1, entries().length())
    }

    @Test
    fun `given unfinished child, reconciliation marks interruption without replay`() {
        journal.begin("first-job", NativeGenerationEngine.BONSAI, "worker:0")
        journal.connected("first-job", 123, "incarnation")

        FileGenerationAttemptJournal(context).reconcile()

        val entry = entries().getJSONObject(0)
        assertEquals("interrupted", entry.getString("outcome"))
        assertEquals(123, entry.getInt("pid"))
        assertEquals("incarnation", entry.getString("incarnation"))
        assertThrows(NativeGenerationFailure::class.java) {
            journal.begin("second-job", NativeGenerationEngine.BONSAI, "worker:0")
        }
    }

    @Test
    fun `given terminal outcome, late progress cannot overwrite it`() {
        journal.begin("job", NativeGenerationEngine.BONSAI, null)
        journal.finish("job", "success", NativeGenerationStage.WRITING_RESULT, null)

        journal.stage("job", NativeGenerationStage.DENOISING)

        val entry = entries().getJSONObject(0)
        assertEquals("success", entry.getString("outcome"))
        assertEquals(NativeGenerationStage.WRITING_RESULT.name, entry.getString("stage"))
    }

    @Test
    fun `given out of order stage callbacks, journal never moves backwards`() {
        journal.begin("job", NativeGenerationEngine.BONSAI, null)
        journal.stage("job", NativeGenerationStage.DECODING)

        journal.stage("job", NativeGenerationStage.DENOISING)

        assertEquals(
            NativeGenerationStage.DECODING.name,
            entries().getJSONObject(0).getString("stage")
        )
    }

    @Test
    fun `given sequential batch identities, each image is independently admitted`() {
        journal.begin("first-job", NativeGenerationEngine.BONSAI, "worker:0")
        journal.finish("first-job", "success", NativeGenerationStage.WRITING_RESULT, null)

        journal.begin("second-job", NativeGenerationEngine.BONSAI, "worker:1")

        assertEquals(2, entries().length())
        assertEquals("worker:1", entries().getJSONObject(1).getString("owner"))
    }

    @Test
    fun `given corrupt journal, admission fails instead of erasing replay protection`() {
        val file = File(temporary.root, "inference/attempts.json")
        file.parentFile!!.mkdirs()
        file.writeText("invalid-json")

        assertThrows(Exception::class.java) {
            journal.begin(
                "job",
                NativeGenerationEngine.BONSAI,
                "worker:0"
            )
        }
        assertEquals("invalid-json", file.readText())
    }

    private fun entries() = JSONArray(File(temporary.root, "inference/attempts.json").readText())
}
