package com.shifthackz.aisdv1.feature.onnx.environment

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtLoggingLevel
import ai.onnxruntime.OrtSession
import ai.onnxruntime.providers.NNAPIFlags
import android.os.Build
import com.shifthackz.aisdv1.core.common.log.infoLog
import com.shifthackz.aisdv1.feature.onnx.LocalDiffusionContract
import java.util.EnumSet

/**
 * Opens each stage using Android's compatible NNAPI devices, including its reference CPU.
 * FP16 relaxation allows supporting drivers to accelerate float graphs without changing model files.
 *
 * @param environment Child-process ORT environment.
 * @param configuration Immutable model, provider and diagnostic settings for this job.
 * @param controller Cancellation checks around noninterruptible native model loading.
 * @author Dmitriy Moroz
 */
internal class OnnxSessionFactoryImpl(
    private val environment: OrtEnvironmentProvider,
    private val configuration: OnnxRuntimeConfiguration,
    private val controller: OnnxRunController,
) : OnnxSessionFactory {
    /**
     * Opens the stage with relaxed FP16 NNAPI execution when enabled.
     * NNAPI may mix accelerator and reference CPU operations; enabling it does not guarantee a speedup.
     * Android below 8.1 (API 27) always uses ORT CPU, even if the captured configuration
     * contains an enabled NNAPI preference.
     * Development diagnostics identify the runtime and compiled providers, not actual accelerator use.
     * Failures close untransferred options and propagate without retrying generation on another backend.
     *
     * @param modelFile Relative model file within the captured directory.
     * @param isAccelerationAllowed Whether this stage supports the NNAPI preference.
     * @return Owner of both session and options.
     * @author Dmitriy Moroz
     */
    override fun create(
        modelFile: String,
        isAccelerationAllowed: Boolean,
    ): OnnxSession {
        controller.checkActive()
        val shouldUseNnapi = configuration.isNnapiEnabled && isAccelerationAllowed &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
        val options = createOptions(modelFile)
        try {
            val nativeEnvironment = environment.get()
            if (shouldUseNnapi) {
                options.addNnapi(EnumSet.of(NNAPIFlags.USE_FP16))
            }
            if (configuration.isDiagnosticsEnabled) {
                infoLog(
                    "OnnxExecution",
                    "${modelFile.substringBefore('/')}: ORT=${nativeEnvironment.version}, available providers=${OrtEnvironment.getAvailableProviders()}, NNAPI=$shouldUseNnapi, FP16 relaxation=$shouldUseNnapi, NNAPI CPU allowed",
                )
            }
            val session = nativeEnvironment.createSession(modelPath(modelFile), options)
            return OwnedOnnxSession(session, options)
        } catch (failure: Throwable) {
            closeFailedOptions(options, failure)
            throw failure
        }
    }

    /**
     * Allocates options whose ownership transfers to a session or the caller's failure cleanup.
     *
     * @param modelFile Stage-relative filename used as a diagnostic logger ID.
     * @return Open options with the captured model format and diagnostic settings.
     * @author Dmitriy Moroz
     */
    private fun createOptions(modelFile: String): OrtSession.SessionOptions {
        val options = OrtSession.SessionOptions()
        try {
            options.addConfigEntry(
                LocalDiffusionContract.ORT_KEY_MODEL_FORMAT,
                LocalDiffusionContract.ORT
            )
            options.setLoggerId(modelFile.substringBefore('/'))
            if (configuration.isDiagnosticsEnabled) {
                options.setSessionLogLevel(OrtLoggingLevel.ORT_LOGGING_LEVEL_VERBOSE)
            }
            return options
        } catch (failure: Throwable) {
            closeFailedOptions(options, failure)
            throw failure
        }
    }

    /**
     * Retains the original initialization failure if native option disposal also fails.
     *
     * @param options Untransferred options to release.
     * @param failure Original model/provider/configuration failure to preserve.
     * @author Dmitriy Moroz
     */
    private fun closeFailedOptions(options: OrtSession.SessionOptions, failure: Throwable) {
        try {
            options.close()
        } catch (closeFailure: Throwable) {
            failure.addSuppressed(closeFailure)
        }
    }

    /**
     * Resolves a stage's model file without consulting mutable application preferences.
     *
     * @param modelFile Relative file under the immutable model root.
     * @return Absolute path consumed by the native ORT loader.
     * @author Dmitriy Moroz
     */
    private fun modelPath(modelFile: String) = "${configuration.modelDirectory}/$modelFile"
}
