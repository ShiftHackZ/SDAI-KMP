package com.shifthackz.aisdv1.core.common.log

import android.util.Log
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Appends session metadata and Timber messages to the app-owned log cache on an IO coroutine scope.
 * Writes are serialized so concurrent log calls cannot interleave their payloads.
 *
 * @param fileProviderDescriptor Locates the app-private directory used for log export and cleanup.
 * @param buildInfoProvider Supplies the version metadata written at the start of each host session.
 * @author Dmitriy Moroz
 */
class FileLoggingTree(
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val buildInfoProvider: BuildInfoProvider,
) : Timber.Tree() {

    private val writeMutex = Mutex()


    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    private val formatDate: String
        get() = "[${SimpleDateFormat(LOGGER_TIMESTAMP_FORMAT, Locale.ROOT).format(Date())}]"


    private val formatPriority: (Int) -> String = {
        when (it) {
            Log.ASSERT -> "[A]"
            Log.DEBUG -> "[D]"
            Log.ERROR -> "[E]"
            Log.INFO -> "[I]"
            Log.VERBOSE -> "[V]"
            Log.WARN -> "[W]"
            else -> "[U]"
        }
    }


    private val formatTag: (String?) -> String = { tag ->
        tag?.let { "[$it]" } ?: LOGGER_DEFAULT_TAG
    }

    init {
        writeLine(buildString {
            appendLine("=== APP SESSION STARTED ===")
            appendLine()
            appendLine("Version : $buildInfoProvider")
            appendLine()
        })
    }

    /**
     * Enqueues one formatted Timber message and optional stack trace for serialized file append.
     * File errors are contained within the IO writer and do not fail the calling logging operation.
     *
     * @param priority Android log priority translated to the log file's one-letter severity.
     * @param tag Optional caller tag; absent tags use the application default.
     * @param message Already formatted Timber message.
     * @param t Optional failure appended as a stack trace.
     *
     * @author Dmitriy Moroz
     */
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val log = buildString {
            append(formatDate)
            append(formatPriority(priority))
            append(" ")
            append(formatTag(tag))
            append(" : ")
            append(message)
            t?.stackTraceToString()?.let { stacktrace ->
                appendLine()
                append(stacktrace)
            }
            appendLine()
        }
        writeLine(log)
    }


    private fun writeLine(message: String) {
        logScope.launch {
            writeMutex.withLock {
                runCatching {
                    val cacheDirectory = File(fileProviderDescriptor.logsCacheDirPath)
                    if (!cacheDirectory.exists()) cacheDirectory.mkdirs()
                    val outFile = File(cacheDirectory, LOGGER_FILENAME)
                    if (!outFile.exists()) outFile.createNewFile()
                    FileOutputStream(outFile, true).use { fos ->
                        val payload = message.toByteArray()
                        fos.write(payload)
                        fos.close()
                    }
                }
            }
        }
    }


    companion object {

        private const val LOGGER_TIMESTAMP_FORMAT = "dd.MM.yyyy HH:mm:SS"


        private const val LOGGER_DEFAULT_TAG = "[SDAI]"

        /**
         * Filename used by log readers and exports inside the app-owned logging cache.
         *
         * @author Dmitriy Moroz
         */
        const val LOGGER_FILENAME = "sdaiv1.log"

        /**
         * Deletes the log-cache directory synchronously, including current and exported log files.
         * Concurrent queued writes may recreate the directory; callers choose when cleanup is appropriate.
         *
         * @param fileProviderDescriptor Locates the application-owned log-cache directory.
         *
         * @author Dmitriy Moroz
         */
        fun clearLog(fileProviderDescriptor: FileProviderDescriptor) {
            val cacheDirectory = File(fileProviderDescriptor.logsCacheDirPath)
            cacheDirectory.deleteRecursively()
        }
    }
}
