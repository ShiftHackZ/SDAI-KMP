package com.shifthackz.aisdv1.app

import android.annotation.SuppressLint
import android.app.Application
import android.app.ActivityManager
import android.os.Build
import android.os.Process
import com.shifthackz.aisdv1.app.inference.InferenceServiceDependencies
import android.database.CursorWindow
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.work.Configuration
import androidx.work.WorkManager
import com.shifthackz.aisdv1.core.common.log.FileLoggingTree
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.presentation.app.bootstrap.AndroidAppServices
import com.shifthackz.aisdv1.presentation.app.bootstrap.startAndroidApplication
import com.shifthackz.aisdv1.presentation.app.bootstrap.createNativeInferenceEngineFactory
import com.shifthackz.aisdv1.app.inference.FileGenerationFileStore
import com.shifthackz.aisdv1.app.inference.CompletePngImageValidator
import timber.log.Timber

/**
 * Composes the application host or an isolated native inference process according to its process name.
 *
 * @author Dmitriy Moroz
 */
class AiStableDiffusionClientApp : Application() {

    /**
     * Host-only access to lazily resolved worker and preference services; not initialized in :inference.
     *
     * @author Dmitriy Moroz
     */
    internal lateinit var appServices: AndroidAppServices
        private set

    private val nativeHostBindings by lazy { NativeHostBindings(this) }

    internal lateinit var inferenceDependencies: InferenceServiceDependencies
        private set


    /**
     * Creates only child inference dependencies in the native process; host startup owns UI, workers,
     * storage and file logging. Development child processes also emit common logger diagnostics.
     *
     * @author Dmitriy Moroz
     */
    override fun onCreate() {
        super.onCreate()
        val processName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) getProcessName()
        else getSystemService(ActivityManager::class.java).runningAppProcesses?.firstOrNull { it.pid == Process.myPid() }?.processName
        if (processName == "${packageName}:inference") {
            if (BuildConfig.BUILD_TYPE == "dev") {
                Timber.plant(Timber.DebugTree())
            }
            inferenceDependencies = InferenceServiceDependencies(
                engines = createNativeInferenceEngineFactory(this, BuildConfig.BUILD_TYPE == "dev"),
                files = FileGenerationFileStore(this, CompletePngImageValidator()),
            )
            return
        }
        StrictMode.setVmPolicy(VmPolicy.Builder().build())
        initializeComposeTracing(this)
        initializeApplicationServices()
        initializeLogging()
        initializeUncaughtExceptionHandler()
        initializeCursorSize()
        initializeWorkManager()
    }

    /**
     * Overrides the cursor size to prevent Room DB fail with big base64.
     *
     * Reference: https://stackoverflow.com/questions/51959944/sqliteblobtoobigexception-row-too-big-to-fit-into-cursorwindow-requiredpos-0-t
     */
    @SuppressLint("DiscouragedPrivateApi")
    private fun initializeCursorSize() {
        try {
            val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) // 100 Mb
        } catch (e: Exception) {
            errorLog(e)
        }
    }

    private fun initializeApplicationServices() {
        appServices = startAndroidApplication(
            this,
            nativeHostBindings,
            BuildConfig.UI_DIAGNOSTICS_ENABLED
        )
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(
            FileLoggingTree(
                nativeHostBindings.fileProviderDescriptor,
                nativeHostBindings.buildInfoProvider
            )
        )
    }

    private fun initializeUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { errorLog(throwable) }
            defaultHandler?.uncaughtException(thread, throwable) ?: throw throwable
        }
    }

    private fun initializeWorkManager() {
        try {
            val workerFactory = appServices.workerFactory
            val configuration = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()

            WorkManager.initialize(this, configuration)
        } catch (e: Exception) {
            errorLog(e)
        }
    }
}
