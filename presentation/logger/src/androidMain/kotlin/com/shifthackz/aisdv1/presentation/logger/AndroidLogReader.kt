package com.shifthackz.aisdv1.presentation.logger

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.log.FileLoggingTree
import java.io.File

/**
 * Reads the current Android log file from the app-owned logging cache.
 * The caller chooses background dispatch; missing files and read errors propagate.
 *
 * @param fileProviderDescriptor Locates the directory shared with the file-logging tree.
 *
 * @author Dmitriy Moroz
 */
class AndroidLogReader(
    private val fileProviderDescriptor: FileProviderDescriptor,
) : LogReader {
    /**
     * Reads the current log file as text without suppressing missing-file or filesystem errors.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun read(): String {
        val logFile = File(
            fileProviderDescriptor.logsCacheDirPath,
            FileLoggingTree.LOGGER_FILENAME,
        )
        return logFile.readText()
    }
}
