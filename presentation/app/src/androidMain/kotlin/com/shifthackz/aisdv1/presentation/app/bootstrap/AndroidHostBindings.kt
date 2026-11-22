package com.shifthackz.aisdv1.presentation.app.bootstrap

import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import com.shifthackz.aisdv1.domain.feature.sdxl.StableDiffusionCpp

/**
 * Android host collaborators resolved lazily after graph registration.
 *
 * @author Dmitriy Moroz
 */
interface AndroidHostBindings {
    /**
     * Package version, native platform and distribution flavor.
     *
     * @author Dmitriy Moroz
     */
    val buildInfoProvider: BuildInfoProvider

    /**
     * Dispatchers preserving Android main-immediate UI delivery.
     *
     * @author Dmitriy Moroz
     */
    val dispatchersProvider: DispatchersProvider

    /**
     * Native wall and monotonic clocks used by shared operations.
     *
     * @author Dmitriy Moroz
     */
    val timeProvider: TimeProvider

    /**
     * App-owned filesystem roots and the manifest file-provider authority.
     *
     * @author Dmitriy Moroz
     */
    val fileProviderDescriptor: FileProviderDescriptor

    /**
     * Creates launcher intents retaining the installed Activity identity.
     *
     * @author Dmitriy Moroz
     */
    val activityIntentProvider: ActivityIntentProvider

    /**
     * Host transport to isolated inference; resolving it never loads host JNI.
     *
     * @author Dmitriy Moroz
     */
    val nativeGenerationGateway: NativeGenerationGateway

    /**
     * Reads previous process exits for generation attempt diagnostics.
     *
     * @author Dmitriy Moroz
     */
    val processExitDiagnosticsGateway: ProcessExitDiagnosticsGateway

    /**
     * SDCPP adapter that routes native generation into the isolated child process.
     *
     * @author Dmitriy Moroz
     */
    val stableDiffusionCpp: StableDiffusionCpp
}
