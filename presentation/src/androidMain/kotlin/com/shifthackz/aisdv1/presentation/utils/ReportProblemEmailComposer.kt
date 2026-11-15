package com.shifthackz.aisdv1.presentation.utils

import android.content.Context
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.log.FileLoggingTree
import com.shifthackz.aisdv1.core.sharing.shareEmail
import java.io.File

/**
 * Opens the existing user-composed problem report with explicitly selected attachments.
 *
 * @param fileProviderDescriptor Existing app-private FileProvider paths.
 * @param buildInfoProvider Non-secret application build metadata.
 * @author Dmitriy Moroz
 */
internal class ReportProblemEmailComposer(
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val buildInfoProvider: BuildInfoProvider,
) {
    /**
     * Opens an email chooser; a stale capture is never attached by file discovery.
     *
     * @param context Context used to open the chooser.
     * @param selectedCapture Capture passed only by the user's explicit diagnostic share action.
     * @author Dmitriy Moroz
     */
    fun invoke(context: Context, selectedCapture: File? = null) {
        val logFile = File(fileProviderDescriptor.logsCacheDirPath, FileLoggingTree.LOGGER_FILENAME)
        context.shareEmail(
            email = "sdai@moroz.cc",
            subject = "SDAI - Problem report",
            body = "SDAI : $buildInfoProvider",
            file = logFile.takeIf(File::exists),
            extraFiles = listOfNotNull(selectedCapture?.takeIf(File::exists)),
            fileProviderPath = fileProviderDescriptor.providerPath,
        )
    }
}
